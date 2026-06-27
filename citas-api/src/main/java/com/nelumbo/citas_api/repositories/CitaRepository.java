package com.nelumbo.citas_api.repositories;

import com.nelumbo.citas_api.entities.Cita;
import com.nelumbo.citas_api.entities.EstadoCita;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByPacienteDocumentoAndEstadoIn(String documento, Collection<EstadoCita> estados);

    List<Cita> findByOdontologoIdAndEstadoIn(Long odontologoId, Collection<EstadoCita> estados);

    List<Cita> findByConsultorioIdAndEstadoIn(Long consultorioId, Collection<EstadoCita> estados);
}
