package com.nelumbo.citas_api.controllers;

import com.nelumbo.citas_api.dto.ConsultorioDto;
import com.nelumbo.citas_api.services.ConsultorioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consultorios")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class ConsultorioController {

    private final ConsultorioService service;

    @GetMapping
    public List<ConsultorioDto> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ConsultorioDto obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultorioDto crear(@Valid @RequestBody ConsultorioDto dto) {
        return service.crear(dto);
    }

    @PutMapping("/{id}")
    public ConsultorioDto actualizar(@PathVariable Long id, @Valid @RequestBody ConsultorioDto dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
