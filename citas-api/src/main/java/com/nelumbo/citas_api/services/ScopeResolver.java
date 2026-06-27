package com.nelumbo.citas_api.services;

import com.nelumbo.citas_api.entities.Clinica;
import com.nelumbo.citas_api.entities.Usuario;
import com.nelumbo.citas_api.exception.ApiException;
import com.nelumbo.citas_api.repositories.UsuarioRepository;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Resuelve el alcance por rol para consultas e indicadores: el ADMIN ve toda la red; el RECEPCIONISTA
// solo sus clínicas asociadas.
@Component
@RequiredArgsConstructor
public class ScopeResolver {

    private final UsuarioRepository usuarioRepo;

    // filtrar=false ⇒ ADMIN (sin restricción); clinicaIds nunca va vacío para no romper el IN del query.
    public record Scope(boolean filtrar, Set<Long> clinicaIds) {
    }

    @Transactional(readOnly = true)
    public Scope resolver(String email) {
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));
        if (!"RECEPCIONISTA".equals(usuario.getRol().getNombre())) {
            return new Scope(false, Set.of(0L));
        }
        Set<Long> ids = usuario.getClinicas().stream().map(Clinica::getId).collect(Collectors.toSet());
        // Recepcionista sin clínicas: que no vea nada en vez de fallar el query con un IN vacío.
        return new Scope(true, ids.isEmpty() ? Set.of(0L) : ids);
    }

    public void exigirAcceso(Scope scope, Long clinicaId) {
        if (scope.filtrar() && !scope.clinicaIds().contains(clinicaId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tiene acceso a los datos de esta clínica");
        }
    }
}
