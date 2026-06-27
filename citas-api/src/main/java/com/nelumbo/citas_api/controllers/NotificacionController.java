package com.nelumbo.citas_api.controllers;

import com.nelumbo.citas_api.dto.EnviarNotificacionRequest;
import com.nelumbo.citas_api.dto.MensajeResponse;
import com.nelumbo.citas_api.services.NotificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN','RECEPCIONISTA')")
public class NotificacionController {

    private final NotificacionService service;

    @PostMapping("/enviar")
    public MensajeResponse enviar(@Valid @RequestBody EnviarNotificacionRequest req) {
        return service.enviar(req);
    }
}
