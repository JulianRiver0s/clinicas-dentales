package com.nelumbo.citas_api.repositories;

import com.nelumbo.citas_api.entities.TokenInvalidado;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenInvalidadoRepository extends JpaRepository<TokenInvalidado, String> {

    // Purga los ya vencidos: tras la expiración el token es inválido por sí solo y la fila sobra.
    void deleteByExpiraEnBefore(Instant momento);
}
