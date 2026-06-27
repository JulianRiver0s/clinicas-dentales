package com.nelumbo.citas_api.dto;

import java.time.Instant;

public record PrimeraVezDto(String documento, String nombre, Long clinicaId, String clinicaNombre, Instant fechaHora) {
}
