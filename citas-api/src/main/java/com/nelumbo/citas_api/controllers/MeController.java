package com.nelumbo.citas_api.controllers;

import com.nelumbo.citas_api.dto.ClinicaDto;
import com.nelumbo.citas_api.dto.ConsultorioDto;
import com.nelumbo.citas_api.services.MeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Cualquier usuario autenticado ve sus propias asociaciones; el admin no tiene, así que recibe listas vacías.
@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class MeController {

    private final MeService service;

    @GetMapping("/clinicas")
    public List<ClinicaDto> clinicas(Authentication auth) {
        return service.misClinicas(auth.getName());
    }

    @GetMapping("/consultorios")
    public List<ConsultorioDto> consultorios(Authentication auth) {
        return service.misConsultorios(auth.getName());
    }
}
