package com.nelumbo.citas_api.controllers;

import com.nelumbo.citas_api.dto.GananciasDto;
import com.nelumbo.citas_api.dto.OdontologoAtencionesDto;
import com.nelumbo.citas_api.dto.PacienteAtencionesDto;
import com.nelumbo.citas_api.dto.PrimeraVezDto;
import com.nelumbo.citas_api.dto.ProcedimientoSolicitadoDto;
import com.nelumbo.citas_api.services.IndicadorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/indicadores")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN','RECEPCIONISTA')")
public class IndicadorController {

    private final IndicadorService service;

    @GetMapping("/top-pacientes-red")
    public List<PacienteAtencionesDto> topPacientesRed(Authentication auth) {
        return service.topPacientesRed(auth.getName());
    }

    @GetMapping("/top-pacientes-clinica/{clinicaId}")
    public List<PacienteAtencionesDto> topPacientesClinica(@PathVariable Long clinicaId, Authentication auth) {
        return service.topPacientesClinica(auth.getName(), clinicaId);
    }

    @GetMapping("/primera-vez-hoy")
    public List<PrimeraVezDto> primeraVezHoy(Authentication auth) {
        return service.primeraVezHoy(auth.getName());
    }

    @GetMapping("/ganancias/{clinicaId}")
    public GananciasDto ganancias(@PathVariable Long clinicaId, Authentication auth) {
        return service.ganancias(auth.getName(), clinicaId);
    }

    // El @PreAuthorize del método restringe a ADMIN por encima del de la clase.
    @GetMapping("/top-odontologos-mes")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<OdontologoAtencionesDto> topOdontologosMes() {
        return service.topOdontologosMes();
    }

    @GetMapping("/top-procedimientos-mes")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<ProcedimientoSolicitadoDto> topProcedimientosMes() {
        return service.topProcedimientosMes();
    }
}
