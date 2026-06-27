package com.nelumbo.citas_api.dto;

import com.nelumbo.citas_api.entities.EstadoCita;
import java.time.Instant;

// Shape exacto del §4.5 para el listado del día, el listado general y la búsqueda.
public record CitaResponse(
        Long id,
        String documentoPaciente,
        String nombrePaciente,
        String odontologo,
        String procedimiento,
        Instant fechaHora,
        EstadoCita estado) {
}
