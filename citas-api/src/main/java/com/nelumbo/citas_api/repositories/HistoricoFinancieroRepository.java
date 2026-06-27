package com.nelumbo.citas_api.repositories;

import com.nelumbo.citas_api.entities.EstadoCita;
import com.nelumbo.citas_api.entities.HistoricoFinanciero;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoFinancieroRepository extends JpaRepository<HistoricoFinanciero, Long> {

    // Inasistencias del paciente en los últimos N días, contadas por la fecha real del evento (fechaHora del
    // histórico) y no por la fecha programada de la cita.
    long countByPacienteDocumentoAndEstadoCitaAndFechaHoraAfter(String documento, EstadoCita estadoCita, Instant desde);
}
