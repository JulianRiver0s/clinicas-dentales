package com.nelumbo.citas_api.controllers;

import com.nelumbo.citas_api.dto.LoginRequest;
import com.nelumbo.citas_api.dto.MensajeResponse;
import com.nelumbo.citas_api.dto.RegisterRequest;
import com.nelumbo.citas_api.dto.TokenResponse;
import com.nelumbo.citas_api.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public MensajeResponse register(@Valid @RequestBody RegisterRequest req) {
        authService.registrarRecepcionista(req);
        return new MensajeResponse("Recepcionista creado");
    }

    // Alias para renovar el access token a partir de un token aún válido.
    @PostMapping("/token")
    public TokenResponse refresh(@RequestParam String refreshToken) {
        return authService.refresh(refreshToken);
    }
}
