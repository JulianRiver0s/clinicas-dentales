package com.nelumbo.notification_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// Microservicio de notificaciones simulado: registra la notificación y confirma el envío.
@RestController
public class NotificacionController {

    private static final Logger log = LoggerFactory.getLogger(NotificacionController.class);

    @PostMapping("/notificaciones")
    public Mensaje notificar(@RequestBody Notificacion req) {
        log.info("Notificación recibida: email={}, documento={}, clínica={}, mensaje={}",
                req.email(), req.documento(), req.clinicaNombre(), req.mensaje());
        return new Mensaje("Notificación Enviada");
    }
}

record Notificacion(String email, String documento, String mensaje, String clinicaNombre) {
}

record Mensaje(String mensaje) {
}
