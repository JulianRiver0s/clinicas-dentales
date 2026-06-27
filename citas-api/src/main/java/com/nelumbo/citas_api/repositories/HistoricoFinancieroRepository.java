package com.nelumbo.citas_api.repositories;

import com.nelumbo.citas_api.entities.EstadoCita;
import com.nelumbo.citas_api.entities.HistoricoFinanciero;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistoricoFinancieroRepository extends JpaRepository<HistoricoFinanciero, Long> {

    // Inasistencias del paciente en los últimos N días, contadas por la fecha real del evento (fechaHora del
    // histórico) y no por la fecha programada de la cita.
    long countByPacienteDocumentoAndEstadoCitaAndFechaHoraAfter(String documento, EstadoCita estadoCita, Instant desde);

    // Ganancias de una clínica desde una fecha: solo los cobros de procedimiento (no los cargos por cancelación).
    @Query("""
            SELECT COALESCE(SUM(h.monto), 0)
            FROM HistoricoFinanciero h
            WHERE h.tipo = com.nelumbo.citas_api.entities.TipoMovimiento.COBRO_PROCEDIMIENTO
              AND h.clinicaId = :clinicaId
              AND h.fechaHora >= :desde""")
    BigDecimal sumarCobros(@Param("clinicaId") Long clinicaId, @Param("desde") Instant desde);
}
