package com.nelumbo.citas_api.controllers;

import com.nelumbo.citas_api.dto.AgendarCitaRequest;
import com.nelumbo.citas_api.dto.CitaCreadaResponse;
import com.nelumbo.citas_api.dto.MensajeResponse;
import com.nelumbo.citas_api.dto.RegistrarAtencionRequest;
import com.nelumbo.citas_api.dto.RegistrarAtencionResponse;
import com.nelumbo.citas_api.services.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/citas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN','RECEPCIONISTA')")
public class CitaController {

    private final CitaService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitaCreadaResponse agendar(@Valid @RequestBody AgendarCitaRequest req, Authentication auth) {
        return service.agendar(req, auth.getName());
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
}
