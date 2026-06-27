package com.nelumbo.citas_api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

// Datos para agendar: documento y nombre del paciente, ids de consultorio/odontólogo/procedimiento y fecha-hora.
// requiereAprobacion es opcional: si viene en true, la cita nace pendiente de aprobación del administrador.
public record AgendarCitaRequest(
        @NotBlank @Pattern(regexp = "\\d{6,12}",
                message = "El documento debe tener entre 6 y 12 dígitos numéricos") String documento,
        @NotBlank @Size(max = 120) String nombrePaciente,
        @NotNull Long consultorioId,
        @NotNull Long odontologoId,
        @NotNull Long procedimientoId,
        @NotNull @Future Instant fechaHora,
        Boolean requiereAprobacion) {
}
