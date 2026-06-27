package com.nelumbo.citas_api.dto;

import com.nelumbo.citas_api.entities.EstadoCita;
import java.math.BigDecimal;
import java.time.Instant;

// Detalle de una cita: los campos del listado más los datos de sede, cobro y tiempos reales.
public record CitaDetalleResponse(
        Long id,
        String documentoPaciente,
        String nombrePaciente,
        String odontologo,
        String procedimiento,
        Long clinicaId,
        String clinicaNombre,
        String consultorio,
        BigDecimal costo,
        Instant fechaHora,
        Instant checkinEn,
        Instant inicioReal,
        Instant finReal,
        EstadoCita estado) {
}
