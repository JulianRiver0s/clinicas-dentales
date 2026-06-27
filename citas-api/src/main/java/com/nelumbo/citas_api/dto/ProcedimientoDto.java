package com.nelumbo.citas_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProcedimientoDto(
        Long id,
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 300) String descripcion,
        @NotNull @Positive BigDecimal costo,
        @Positive int duracionMinutos) {
}
