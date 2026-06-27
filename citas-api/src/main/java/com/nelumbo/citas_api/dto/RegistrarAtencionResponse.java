package com.nelumbo.citas_api.dto;

import java.math.BigDecimal;

public record RegistrarAtencionResponse(String mensaje, BigDecimal totalCobrado) {
}
