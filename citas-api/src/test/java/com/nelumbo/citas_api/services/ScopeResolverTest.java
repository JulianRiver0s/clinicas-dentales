package com.nelumbo.citas_api.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nelumbo.citas_api.exception.ApiException;
import com.nelumbo.citas_api.services.ScopeResolver.Scope;
import java.util.Set;
import org.junit.jupiter.api.Test;

// Regla de acceso por clínica (sin contexto Spring): el repo no se usa en exigirAcceso.
class ScopeResolverTest {

    private final ScopeResolver scope = new ScopeResolver(null);

    @Test
    void adminAccedeACualquierClinica() {
        assertDoesNotThrow(() -> scope.exigirAcceso(new Scope(false, Set.of(0L)), 999L));
    }

    @Test
    void recepcionistaAccedeASuClinica() {
        assertDoesNotThrow(() -> scope.exigirAcceso(new Scope(true, Set.of(1L, 2L)), 1L));
    }

    @Test
    void recepcionistaNoAccedeAClinicaAjena() {
        assertThrows(ApiException.class, () -> scope.exigirAcceso(new Scope(true, Set.of(1L)), 9L));
    }
}
