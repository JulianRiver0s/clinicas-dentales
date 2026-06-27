package com.nelumbo.citas_api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConsultorioDto(
        Long id,
        @NotNull Long clinicaId,
        @NotBlank @Size(max = 120) String nombre,
        @Min(1) int capacidadSimultanea) {
}
