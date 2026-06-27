package com.nelumbo.citas_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// Identifica la atención a cerrar: documento del paciente e id de la cita.
public record RegistrarAtencionRequest(
        @NotBlank @Pattern(regexp = "\\d{6,12}",
                message = "El documento debe tener entre 6 y 12 dígitos numéricos") String documento,
        @NotNull Long citaId) {
}
