package com.nelumbo.citas_api.dto;

import java.math.BigDecimal;

public record GananciasDto(Long clinicaId, BigDecimal hoy, BigDecimal semana, BigDecimal mes, BigDecimal anio) {
}
