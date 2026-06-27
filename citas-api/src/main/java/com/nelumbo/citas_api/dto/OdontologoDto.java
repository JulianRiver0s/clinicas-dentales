package com.nelumbo.citas_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record OdontologoDto(
        Long id,
        @NotBlank @Size(max = 12) String documento,
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 120) String especialidad,
        Set<Long> clinicaIds) {
}
