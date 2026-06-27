package com.nelumbo.citas_api.services;

import com.nelumbo.citas_api.dto.AgendarCitaRequest;
import com.nelumbo.citas_api.dto.CitaCreadaResponse;
import com.nelumbo.citas_api.entities.Cita;
import com.nelumbo.citas_api.entities.Consultorio;
import com.nelumbo.citas_api.entities.EstadoCita;
import com.nelumbo.citas_api.entities.Odontologo;
import com.nelumbo.citas_api.entities.Paciente;
import com.nelumbo.citas_api.entities.Procedimiento;
import com.nelumbo.citas_api.entities.Usuario;
import com.nelumbo.citas_api.exception.ApiException;
import com.nelumbo.citas_api.repositories.CitaRepository;
import com.nelumbo.citas_api.repositories.ConsultorioRepository;
import com.nelumbo.citas_api.repositories.OdontologoRepository;
import com.nelumbo.citas_api.repositories.PacienteRepository;
import com.nelumbo.citas_api.repositories.ProcedimientoRepository;
import com.nelumbo.citas_api.repositories.UsuarioRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CitaService {

    // Mensajes de error tal cual se devuelven al cliente.
    static final String DUPLICADO =
            "No se puede Agendar Cita, ya existe una cita pendiente para este paciente en esta u otra clínica el mismo día";
    static final String SIN_DISPONIBILIDAD =
            "No se puede Agendar Cita, el odontólogo o el consultorio no se encuentran disponibles en el horario solicitado";
    static final String PACIENTE_BLOQUEADO =
            "No se puede Agendar Cita, el paciente se encuentra bloqueado por inasistencias reiteradas";

    // Estados no atendidos que ocupan agenda y cuentan para el duplicado.
    private static final Set<EstadoCita> ACTIVAS = EnumSet.of(EstadoCita.AGENDADA, EstadoCita.EN_CURSO);
    // Zona de la red de clínicas para decidir si dos citas caen el mismo día.
    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final CitaRepository citaRepo;
    private final PacienteRepository pacienteRepo;
    private final ConsultorioRepository consultorioRepo;
    private final OdontologoRepository odontologoRepo;
    private final ProcedimientoRepository procedimientoRepo;
    private final UsuarioRepository usuarioRepo;

    @Transactional
    public CitaCreadaResponse agendar(AgendarCitaRequest req, String emailUsuario) {
        Consultorio consultorio = consultorioRepo.findById(req.consultorioId())
                .orElseThrow(() -> ApiException.noEncontrado("Consultorio no encontrado"));
        Odontologo odontologo = odontologoRepo.findById(req.odontologoId())
                .orElseThrow(() -> ApiException.noEncontrado("Odontólogo no encontrado"));
        Procedimiento procedimiento = procedimientoRepo.findById(req.procedimientoId())
                .orElseThrow(() -> ApiException.noEncontrado("Procedimiento no encontrado"));
        Usuario creador = usuarioRepo.findByEmail(emailUsuario)
                .orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));
        validarScope(creador, consultorio);

        Paciente existente = pacienteRepo.findById(req.documento()).orElse(null);
        validarPaciente(existente, req.documento(), req.fechaHora());

        Instant inicio = req.fechaHora();
        Instant fin = inicio.plus(Duration.ofMinutes(procedimiento.getDuracionMinutos()));
        validarDisponibilidad(odontologo.getId(), consultorio, inicio, fin);

        Paciente paciente = upsertPaciente(existente, req.documento(), req.nombrePaciente());

        Cita cita = Cita.builder()
                .paciente(paciente)
                .consultorio(consultorio)
                .odontologo(odontologo)
                .procedimiento(procedimiento)
                .clinica(consultorio.getClinica())
                .fechaHoraCita(inicio)
                .fechaHoraCreacion(Instant.now())
                .estado(EstadoCita.AGENDADA)
                .costo(procedimiento.getCosto())
                .creadoPor(creador)
                .build();
        return new CitaCreadaResponse(citaRepo.save(cita).getId());
    }

    // El recepcionista solo puede agendar en consultorios de sus clínicas; el admin no tiene esa restricción.
    private void validarScope(Usuario creador, Consultorio consultorio) {
        if (!"RECEPCIONISTA".equals(creador.getRol().getNombre())) {
            return;
        }
        boolean asociada = creador.getClinicas().stream()
                .anyMatch(c -> c.getId().equals(consultorio.getClinica().getId()));
        if (!asociada) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tiene permisos para agendar en esta clínica");
        }
    }

    // Un paciente nuevo no tiene historia: no puede estar bloqueado ni tener una cita el mismo día.
    private void validarPaciente(Paciente existente, String documento, Instant fechaHora) {
        if (existente == null) {
            return;
        }
        if (existente.getBloqueadoHasta() != null && existente.getBloqueadoHasta().isAfter(Instant.now())) {
            throw ApiException.negocio(PACIENTE_BLOQUEADO);
        }
        boolean duplicada = citaRepo.findByPacienteDocumentoAndEstadoIn(documento, ACTIVAS).stream()
                .anyMatch(c -> mismaFecha(c.getFechaHoraCita(), fechaHora));
        if (duplicada) {
            throw ApiException.negocio(DUPLICADO);
        }
    }

    // El odontólogo no puede tener citas solapadas y el consultorio no puede pasar su capacidad en el rango.
    private void validarDisponibilidad(Long odontologoId, Consultorio consultorio, Instant inicio, Instant fin) {
        boolean odontoOcupado = citaRepo.findByOdontologoIdAndEstadoIn(odontologoId, ACTIVAS).stream()
                .anyMatch(c -> solapa(inicio, fin, c.getFechaHoraCita(), finDe(c)));
        if (odontoOcupado) {
            throw ApiException.negocio(SIN_DISPONIBILIDAD);
        }
        long solapadas = citaRepo.findByConsultorioIdAndEstadoIn(consultorio.getId(), ACTIVAS).stream()
                .filter(c -> solapa(inicio, fin, c.getFechaHoraCita(), finDe(c)))
                .count();
        if (solapadas >= consultorio.getCapacidadSimultanea()) {
            throw ApiException.negocio(SIN_DISPONIBILIDAD);
        }
    }

    private Paciente upsertPaciente(Paciente existente, String documento, String nombre) {
        Paciente paciente = existente != null ? existente : Paciente.builder().documento(documento).build();
        paciente.setNombre(nombre);
        return pacienteRepo.save(paciente);
    }

    private static Instant finDe(Cita c) {
        return c.getFechaHoraCita().plus(Duration.ofMinutes(c.getProcedimiento().getDuracionMinutos()));
    }

    // Solapamiento de intervalos semiabiertos [inicio, fin): citas que se tocan en el límite NO chocan.
    static boolean solapa(Instant inicio1, Instant fin1, Instant inicio2, Instant fin2) {
        return inicio1.isBefore(fin2) && inicio2.isBefore(fin1);
    }

    static boolean mismaFecha(Instant a, Instant b) {
        return a.atZone(ZONA).toLocalDate().equals(b.atZone(ZONA).toLocalDate());
    }
}
