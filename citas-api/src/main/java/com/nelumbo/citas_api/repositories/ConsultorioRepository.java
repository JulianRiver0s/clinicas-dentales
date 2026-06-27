package com.nelumbo.citas_api.repositories;

import com.nelumbo.citas_api.entities.Consultorio;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultorioRepository extends JpaRepository<Consultorio, Long> {

    List<Consultorio> findByClinicaIdIn(Collection<Long> clinicaIds);
}
