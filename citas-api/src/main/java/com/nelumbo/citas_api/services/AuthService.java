package com.nelumbo.citas_api.services;

import com.nelumbo.citas_api.dto.LoginRequest;
import com.nelumbo.citas_api.dto.RegisterRequest;
import com.nelumbo.citas_api.dto.TokenResponse;
import com.nelumbo.citas_api.entities.Rol;
import com.nelumbo.citas_api.entities.TokenInvalidado;
import com.nelumbo.citas_api.entities.Usuario;
import com.nelumbo.citas_api.exception.ApiException;
import com.nelumbo.citas_api.repositories.RolRepository;
import com.nelumbo.citas_api.repositories.TokenInvalidadoRepository;
import com.nelumbo.citas_api.repositories.UsuarioRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final TokenService tokenService;
    private final TokenInvalidadoRepository tokenInvalidadoRepository;

    public TokenResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        return new TokenResponse(tokenService.emitir(auth.getName(), auth.getAuthorities()));
    }

    // Renueva el access token a partir de uno aún válido.
    public TokenResponse refresh(String refreshToken) {
        Jwt decoded = tokenService.decodificar(refreshToken);
        UserDetails user = userDetailsService.loadUserByUsername(decoded.getSubject());
        return new TokenResponse(tokenService.emitir(decoded.getSubject(), user.getAuthorities()));
    }

    // Revoca el token actual hasta su expiración. De paso purga los ya vencidos para que la tabla no crezca.
    @Transactional
    public void logout(String token, Instant expira) {
        tokenInvalidadoRepository.deleteByExpiraEnBefore(Instant.now());
        tokenInvalidadoRepository.save(TokenInvalidado.de(token, expira));
    }

    // Solo ADMIN llega aquí (lo exige @PreAuthorize en el controller). Crea un RECEPCIONISTA.
    public void registrarRecepcionista(RegisterRequest req) {
        if (usuarioRepository.existsByEmail(req.email())) {
            throw ApiException.conflicto("El email ya está registrado");
        }
        Rol rol = rolRepository.findByNombre("RECEPCIONISTA")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Rol RECEPCIONISTA no configurado"));

        usuarioRepository.save(Usuario.builder()
                .nombre(req.nombre())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .rol(rol)
                .activo(true)
                .build());
    }
}
