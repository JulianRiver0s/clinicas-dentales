package com.nelumbo.citas_api.controllers;

import com.nelumbo.citas_api.dto.ProcedimientoDto;
import com.nelumbo.citas_api.services.ProcedimientoService;
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
@RequestMapping("/procedimientos")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class ProcedimientoController {

    private final ProcedimientoService service;

    @GetMapping
    public List<ProcedimientoDto> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ProcedimientoDto obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProcedimientoDto crear(@Valid @RequestBody ProcedimientoDto dto) {
        return service.crear(dto);
    }

    @PutMapping("/{id}")
    public ProcedimientoDto actualizar(@PathVariable Long id, @Valid @RequestBody ProcedimientoDto dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
