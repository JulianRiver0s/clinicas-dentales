package com.nelumbo.citas_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// Cuerpo de /notificaciones/enviar: el cliente manda clinicaId y citas-api lo traduce a clinicaNombre.
public record EnviarNotificacionRequest(
        String email,
        @NotBlank @Pattern(regexp = "\\d{6,12}",
                message = "El documento debe tener entre 6 y 12 dígitos numéricos") String documento,
        @NotBlank String mensaje,
        @NotNull Long clinicaId) {
}
