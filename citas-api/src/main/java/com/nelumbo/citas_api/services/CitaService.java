package com.nelumbo.citas_api.services;

import com.nelumbo.citas_api.dto.AgendarCitaRequest;
import com.nelumbo.citas_api.dto.CitaCreadaResponse;
import com.nelumbo.citas_api.dto.MensajeResponse;
import com.nelumbo.citas_api.dto.NotificacionRequest;
import com.nelumbo.citas_api.dto.RegistrarAtencionRequest;
import com.nelumbo.citas_api.dto.RegistrarAtencionResponse;
import com.nelumbo.citas_api.entities.Cita;
import com.nelumbo.citas_api.entities.Consultorio;
import com.nelumbo.citas_api.entities.EstadoCita;
import com.nelumbo.citas_api.entities.HistoricoFinanciero;
import com.nelumbo.citas_api.entities.Odontologo;
import com.nelumbo.citas_api.entities.Paciente;
import com.nelumbo.citas_api.entities.Procedimiento;
import com.nelumbo.citas_api.entities.TipoMovimiento;
import com.nelumbo.citas_api.entities.Usuario;
import com.nelumbo.citas_api.exception.ApiException;
import com.nelumbo.citas_api.repositories.CitaRepository;
import com.nelumbo.citas_api.repositories.ConsultorioRepository;
import com.nelumbo.citas_api.repositories.HistoricoFinancieroRepository;
import com.nelumbo.citas_api.repositories.OdontologoRepository;
import com.nelumbo.citas_api.repositories.PacienteRepository;
import com.nelumbo.citas_api.repositories.ProcedimientoRepository;
import com.nelumbo.citas_api.repositories.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final String MENSAJE_BLOQUEO =
            "Su cuenta ha sido bloqueada por 30 días debido a inasistencias reiteradas";

    // Estados no atendidos que ocupan agenda y cuentan para el duplicado.
    private static final Set<EstadoCita> ACTIVAS = EnumSet.of(EstadoCita.AGENDADA, EstadoCita.EN_CURSO);
    // Zona de la red de clínicas para decidir si dos citas caen el mismo día.
    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    // Reglas de cancelación e inasistencia.
    private static final long CANCELACION_TARDIA_HORAS = 24;
    private static final BigDecimal PORCENTAJE_CARGO = new BigDecimal("0.30");
    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2);
    static final int UMBRAL_INASISTENCIAS = 3;
    private static final int VENTANA_DIAS = 90;
    private static final int BLOQUEO_DIAS = 30;

    private static final Logger log = LoggerFactory.getLogger(CitaService.class);

    private final CitaRepository citaRepo;
    private final PacienteRepository pacienteRepo;
    private final ConsultorioRepository consultorioRepo;
    private final OdontologoRepository odontologoRepo;
    private final ProcedimientoRepository procedimientoRepo;
    private final UsuarioRepository usuarioRepo;
    private final HistoricoFinancieroRepository historicoRepo;
    private final NotificacionClient notificacionClient;

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
                .estado(estadoInicial(req.requiereAprobacion()))
                .costo(procedimiento.getCosto())
                .creadoPor(creador)
                .build();
        return new CitaCreadaResponse(citaRepo.save(cita).getId());
    }

    // Check-in: el paciente llegó a la clínica. Solo tiene sentido sobre una cita agendada y la pone EN_CURSO.
    @Transactional
    public MensajeResponse checkin(Long citaId) {
        Cita cita = citaRepo.findById(citaId)
                .orElseThrow(() -> ApiException.noEncontrado("Cita no encontrada"));
        if (cita.getEstado() != EstadoCita.AGENDADA) {
            throw ApiException.negocio("Solo se puede registrar check-in de una cita agendada");
        }
        cita.setCheckinEn(Instant.now());
        cita.setEstado(EstadoCita.EN_CURSO);
        return new MensajeResponse("Check-in registrado");
    }

    // Cierre de la atención: confirma el cobro (snapshot) en el histórico y deja la cita ATENDIDA.
    @Transactional
    public RegistrarAtencionResponse registrarAtencion(RegistrarAtencionRequest req) {
        Cita cita = citaRepo.findById(req.citaId())
                .orElseThrow(() -> ApiException.noEncontrado("Cita no encontrada"));
        if (!cita.getPaciente().getDocumento().equals(req.documento())) {
            throw ApiException.negocio("La cita indicada no pertenece a este paciente");
        }
        if (!ACTIVAS.contains(cita.getEstado())) {
            throw ApiException.negocio("La cita ya no está activa, no se puede registrar la atención");
        }
        if (cita.getCheckinEn() == null) {
            throw ApiException.negocio("El paciente aún no ha hecho check-in en esta cita");
        }
        Instant ahora = Instant.now();
        cita.setInicioReal(cita.getCheckinEn());
        cita.setFinReal(ahora);
        cita.setEstado(EstadoCita.ATENDIDA);
        registrarHistorico(cita, TipoMovimiento.COBRO_PROCEDIMIENTO, cita.getCosto(), EstadoCita.ATENDIDA, ahora);
        return new RegistrarAtencionResponse("Atención registrada", cita.getCosto());
    }

    // Cancelar una cita activa. Con menos de 24 h de antelación cobra el 30%; al dejar los estados activos libera la agenda.
    @Transactional
    public MensajeResponse cancelar(Long citaId) {
        Cita cita = citaRepo.findById(citaId)
                .orElseThrow(() -> ApiException.noEncontrado("Cita no encontrada"));
        if (!ACTIVAS.contains(cita.getEstado())) {
            throw ApiException.negocio("Solo se puede cancelar una cita activa");
        }
        Instant ahora = Instant.now();
        BigDecimal cargo = cargoPorCancelacion(cita.getCosto(), cita.getFechaHoraCita(), ahora);
        boolean tardia = cargo.signum() > 0;
        cita.setEstado(EstadoCita.CANCELADA);
        registrarHistorico(cita, tardia ? TipoMovimiento.CARGO_POR_CANCELACION_TARDIA : TipoMovimiento.SIN_CARGO,
                cargo, EstadoCita.CANCELADA, ahora);
        if (tardia) {
            log.info("Cancelación tardía de la cita {}: cargo {}", citaId, cargo);
            return new MensajeResponse("Cita cancelada con cargo por cancelación tardía");
        }
        return new MensajeResponse("Cita cancelada");
    }

    // Marcar inasistencia: libera la agenda, suma al contador y, al tercer no-show en 90 días, bloquea 30 días y notifica.
    // El conteo es leer-modificar-escribir sin bloqueo por paciente; suficiente a este volumen. Si dos recepcionistas
    // marcaran no-show del mismo paciente a la vez, habría que añadir un bloqueo optimista o pesimista sobre el paciente.
    @Transactional
    public MensajeResponse noShow(Long citaId) {
        Cita cita = citaRepo.findById(citaId)
                .orElseThrow(() -> ApiException.noEncontrado("Cita no encontrada"));
        if (!ACTIVAS.contains(cita.getEstado())) {
            throw ApiException.negocio("Solo se puede registrar inasistencia de una cita activa");
        }
        Instant ahora = Instant.now();
        Paciente paciente = cita.getPaciente();

        cita.setEstado(EstadoCita.INASISTENCIA);
        paciente.setInasistencias(paciente.getInasistencias() + 1); // contador acumulado del paciente; la regla de bloqueo no lo usa
        registrarHistorico(cita, TipoMovimiento.SIN_CARGO, CERO, EstadoCita.INASISTENCIA, ahora);

        // El bloqueo se decide sobre el histórico por la fecha real del evento; el guardado anterior ya insertó esta inasistencia.
        Instant desde = ahora.atZone(ZONA).minusDays(VENTANA_DIAS).toInstant();
        long inasistencias = historicoRepo.countByPacienteDocumentoAndEstadoCitaAndFechaHoraAfter(
                paciente.getDocumento(), EstadoCita.INASISTENCIA, desde);

        if (!debeBloquear(inasistencias)) {
            return new MensajeResponse("Inasistencia registrada");
        }
        paciente.setBloqueadoHasta(ahora.atZone(ZONA).plusDays(BLOQUEO_DIAS).toInstant());
        log.warn("Paciente {} bloqueado por {} inasistencias en {} días",
                paciente.getDocumento(), inasistencias, VENTANA_DIAS);
        // El paciente no tiene email en el modelo, así que va vacío; el microservicio identifica por documento.
        notificacionClient.enviar(new NotificacionRequest(
                "", paciente.getDocumento(), MENSAJE_BLOQUEO, cita.getClinica().getNombre()));
        return new MensajeResponse("Inasistencia registrada. Paciente bloqueado por inasistencias reiteradas");
    }

    // Aprobar una cita pendiente: pasa a AGENDADA. Una cita pendiente no reserva el horario, así que aquí se
    // vuelve a comprobar la disponibilidad por si el cupo se ocupó entre el agendamiento y la aprobación.
    @Transactional
    public MensajeResponse aprobar(Long citaId) {
        Cita cita = citaRepo.findById(citaId)
                .orElseThrow(() -> ApiException.noEncontrado("Cita no encontrada"));
        if (cita.getEstado() != EstadoCita.PENDIENTE_APROBACION) {
            throw ApiException.negocio("Solo se puede aprobar una cita pendiente de aprobación");
        }
        validarDisponibilidad(cita.getOdontologo().getId(), cita.getConsultorio(),
                cita.getFechaHoraCita(), finDe(cita));
        cita.setEstado(EstadoCita.AGENDADA);
        return new MensajeResponse("Cita aprobada");
    }

    // Rechazar una cita pendiente: pasa a RECHAZADA y queda fuera de la agenda.
    @Transactional
    public MensajeResponse rechazar(Long citaId) {
        Cita cita = citaRepo.findById(citaId)
                .orElseThrow(() -> ApiException.noEncontrado("Cita no encontrada"));
        if (cita.getEstado() != EstadoCita.PENDIENTE_APROBACION) {
            throw ApiException.negocio("Solo se puede rechazar una cita pendiente de aprobación");
        }
        cita.setEstado(EstadoCita.RECHAZADA);
        return new MensajeResponse("Cita rechazada");
    }

    private void registrarHistorico(Cita cita, TipoMovimiento tipo, BigDecimal monto, EstadoCita estado, Instant cuando) {
        historicoRepo.save(HistoricoFinanciero.builder()
                .cita(cita)
                .pacienteDocumento(cita.getPaciente().getDocumento())
                .clinicaId(cita.getClinica().getId())
                .tipo(tipo)
                .monto(monto)
                .fechaHora(cuando)
                .estadoCita(estado)
                .build());
    }

    // Cancelar con <24 h de antelación cobra el 30% del costo; con 24 h o más, sin cargo.
    static BigDecimal cargoPorCancelacion(BigDecimal costo, Instant fechaCita, Instant ahora) {
        boolean tardia = Duration.between(ahora, fechaCita).compareTo(Duration.ofHours(CANCELACION_TARDIA_HORAS)) < 0;
        BigDecimal cargo = tardia ? costo.multiply(PORCENTAJE_CARGO) : BigDecimal.ZERO;
        return cargo.setScale(2, RoundingMode.HALF_UP);
    }

    static boolean debeBloquear(long inasistenciasEnVentana) {
        return inasistenciasEnVentana >= UMBRAL_INASISTENCIAS;
    }

    // Sin flag (o en false) la cita se agenda directo; con el flag en true queda pendiente de aprobación.
    static EstadoCita estadoInicial(Boolean requiereAprobacion) {
        return Boolean.TRUE.equals(requiereAprobacion) ? EstadoCita.PENDIENTE_APROBACION : EstadoCita.AGENDADA;
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
