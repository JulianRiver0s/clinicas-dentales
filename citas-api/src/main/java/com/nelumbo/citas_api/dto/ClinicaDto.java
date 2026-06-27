package com.nelumbo.citas_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClinicaDto(
        Long id,
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 200) String direccion,
        @Size(max = 120) String ciudad,
        @Size(max = 20) String telefono) {
}
