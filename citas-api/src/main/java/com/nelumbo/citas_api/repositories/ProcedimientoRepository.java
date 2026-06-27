package com.nelumbo.citas_api.repositories;

import com.nelumbo.citas_api.entities.Procedimiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcedimientoRepository extends JpaRepository<Procedimiento, Long> {
}
