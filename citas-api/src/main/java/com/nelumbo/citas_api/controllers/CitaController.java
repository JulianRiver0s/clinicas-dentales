package com.nelumbo.citas_api.controllers;

import com.nelumbo.citas_api.dto.AgendarCitaRequest;
import com.nelumbo.citas_api.dto.CitaCreadaResponse;
import com.nelumbo.citas_api.dto.CitaDetalleResponse;
import com.nelumbo.citas_api.dto.CitaResponse;
import com.nelumbo.citas_api.dto.MensajeResponse;
import com.nelumbo.citas_api.dto.RegistrarAtencionRequest;
import com.nelumbo.citas_api.dto.RegistrarAtencionResponse;
import com.nelumbo.citas_api.entities.EstadoCita;
import com.nelumbo.citas_api.services.CitaService;
import com.nelumbo.citas_api.services.ConsultaCitaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/citas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN','RECEPCIONISTA')")
public class CitaController {

    private final CitaService service;
    private final ConsultaCitaService consulta;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitaCreadaResponse agendar(@Valid @RequestBody AgendarCitaRequest req, Authentication auth) {
        return service.agendar(req, auth.getName());
    }

    // Listado del día por clínica y/o consultorio. fecha opcional: por defecto hoy.
    @GetMapping("/dia")
    public List<CitaResponse> dia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Long clinicaId,
            @RequestParam(required = false) Long consultorioId,
            Authentication auth) {
        return consulta.citasDelDia(auth.getName(), fecha, clinicaId, consultorioId);
    }

    // Listado de citas agendadas y atendidas; todos los filtros opcionales.
    @GetMapping
    public List<CitaResponse> listar(
            @RequestParam(required = false) EstadoCita estado,
            @RequestParam(required = false) Long clinicaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Authentication auth) {
        return consulta.listar(auth.getName(), estado, clinicaId, fecha);
    }

    // Búsqueda por coincidencia parcial del documento del paciente.
    @GetMapping("/buscar")
    public List<CitaResponse> buscar(@RequestParam String documento, Authentication auth) {
        return consulta.buscarPorDocumento(auth.getName(), documento);
    }

    @GetMapping("/{id}")
    public CitaDetalleResponse detalle(@PathVariable Long id, Authentication auth) {
        return consulta.detalle(auth.getName(), id);
    }

    @PostMapping("/{id}/checkin")
    public MensajeResponse checkin(@PathVariable Long id) {
        return service.checkin(id);
    }

    @PostMapping("/registrar-atencion")
    public RegistrarAtencionResponse registrarAtencion(@Valid @RequestBody RegistrarAtencionRequest req) {
        return service.registrarAtencion(req);
    }

    @PostMapping("/{id}/cancelar")
    public MensajeResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    @PostMapping("/{id}/no-show")
    public MensajeResponse noShow(@PathVariable Long id) {
        return service.noShow(id);
    }

    // Aprobar/rechazar una cita pendiente de aprobación es exclusivo del administrador.
    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasAuthority('ADMIN')")
    public MensajeResponse aprobar(@PathVariable Long id) {
        return service.aprobar(id);
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasAuthority('ADMIN')")
    public MensajeResponse rechazar(@PathVariable Long id) {
        return service.rechazar(id);
    }
}
