package com.nelumbo.citas_api.controllers;

import com.nelumbo.citas_api.dto.ClinicaDto;
import com.nelumbo.citas_api.dto.MensajeResponse;
import com.nelumbo.citas_api.services.ClinicaService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clinicas")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class ClinicaController {

    private final ClinicaService service;

    @GetMapping
    public List<ClinicaDto> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ClinicaDto obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClinicaDto crear(@Valid @RequestBody ClinicaDto dto) {
        return service.crear(dto);
    }

    @PutMapping("/{id}")
    public ClinicaDto actualizar(@PathVariable Long id, @Valid @RequestBody ClinicaDto dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    @PostMapping("/{id}/recepcionistas")
    @ResponseStatus(HttpStatus.CREATED)
    public MensajeResponse asociarRecepcionista(@PathVariable Long id, @RequestParam Long usuarioId) {
        service.asociarRecepcionista(id, usuarioId);
        return new MensajeResponse("Recepcionista asociado");
    }
}
