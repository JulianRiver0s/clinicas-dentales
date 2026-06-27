package com.nelumbo.citas_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

// errores solo viaja cuando hay errores de validación por campo.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(int status, String mensaje, Map<String, String> errores) {
}
