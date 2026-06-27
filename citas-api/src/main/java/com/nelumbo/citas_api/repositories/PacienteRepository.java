package com.nelumbo.citas_api.repositories;

import com.nelumbo.citas_api.entities.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, String> {
}
