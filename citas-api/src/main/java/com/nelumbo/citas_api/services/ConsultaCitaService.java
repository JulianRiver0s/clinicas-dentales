package com.nelumbo.citas_api.services;

import com.nelumbo.citas_api.dto.CitaDetalleResponse;
import com.nelumbo.citas_api.dto.CitaResponse;
import com.nelumbo.citas_api.entities.Cita;
import com.nelumbo.citas_api.entities.EstadoCita;
import com.nelumbo.citas_api.exception.ApiException;
import com.nelumbo.citas_api.repositories.CitaRepository;
import com.nelumbo.citas_api.services.ScopeResolver.Scope;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Lado de lectura de las citas: listado del día, listado/detalle y búsqueda. Las mutaciones viven en CitaService.
@Service
@RequiredArgsConstructor
public class ConsultaCitaService {

    private final CitaRepository citaRepo;
    private final ScopeResolver scope;

    @Transactional(readOnly = true)
    public List<CitaResponse> citasDelDia(String email, LocalDate fecha, Long clinicaId, Long consultorioId) {
        if (clinicaId == null && consultorioId == null) {
            throw ApiException.negocio("Debe indicar clinicaId o consultorioId");
        }
        Scope s = scope.resolver(email);
        LocalDate dia = fecha != null ? fecha : Periodos.hoy(Instant.now());
        return citaRepo.citasDelDia(Periodos.inicioDelDia(dia), Periodos.finDelDia(dia),
                        clinicaId, consultorioId, s.filtrar(), s.clinicaIds())
                .stream().map(ConsultaCitaService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> listar(String email, EstadoCita estado, Long clinicaId, LocalDate fecha) {
        Scope s = scope.resolver(email);
        Instant inicio = fecha != null ? Periodos.inicioDelDia(fecha) : null;
        Instant fin = fecha != null ? Periodos.finDelDia(fecha) : null;
        return citaRepo.listar(estado != null, estado, clinicaId != null, clinicaId,
                        fecha != null, inicio, fin, s.filtrar(), s.clinicaIds())
                .stream().map(ConsultaCitaService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> buscarPorDocumento(String email, String documento) {
        Scope s = scope.resolver(email);
        return citaRepo.buscarPorDocumento(documento, s.filtrar(), s.clinicaIds())
                .stream().map(ConsultaCitaService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CitaDetalleResponse detalle(String email, Long id) {
        Cita c = citaRepo.findById(id).orElseThrow(() -> ApiException.noEncontrado("Cita no encontrada"));
        scope.exigirAcceso(scope.resolver(email), c.getClinica().getId());
        return toDetalle(c);
    }

    static CitaResponse toResponse(Cita c) {
        return new CitaResponse(
                c.getId(),
                c.getPaciente().getDocumento(),
                c.getPaciente().getNombre(),
                c.getOdontologo().getNombre(),
                c.getProcedimiento().getNombre(),
                c.getFechaHoraCita(),
                c.getEstado());
    }

    static CitaDetalleResponse toDetalle(Cita c) {
        return new CitaDetalleResponse(
                c.getId(),
                c.getPaciente().getDocumento(),
                c.getPaciente().getNombre(),
                c.getOdontologo().getNombre(),
                c.getProcedimiento().getNombre(),
                c.getClinica().getId(),
                c.getClinica().getNombre(),
                c.getConsultorio().getNombre(),
                c.getCosto(),
                c.getFechaHoraCita(),
                c.getCheckinEn(),
                c.getInicioReal(),
                c.getFinReal(),
                c.getEstado());
    }
}
