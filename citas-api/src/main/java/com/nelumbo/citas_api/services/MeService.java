package com.nelumbo.citas_api.services;

import com.nelumbo.citas_api.dto.ClinicaDto;
import com.nelumbo.citas_api.dto.ConsultorioDto;
import com.nelumbo.citas_api.entities.Clinica;
import com.nelumbo.citas_api.entities.Consultorio;
import com.nelumbo.citas_api.entities.Usuario;
import com.nelumbo.citas_api.exception.ApiException;
import com.nelumbo.citas_api.repositories.ConsultorioRepository;
import com.nelumbo.citas_api.repositories.UsuarioRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Cada usuario consulta solo las clínicas y consultorios que tiene asociados.
@Service
@RequiredArgsConstructor
public class MeService {

    private final UsuarioRepository usuarioRepo;
    private final ConsultorioRepository consultorioRepo;

    @Transactional(readOnly = true)
    public List<ClinicaDto> misClinicas(String email) {
        return usuario(email).getClinicas().stream()
                .map(c -> new ClinicaDto(c.getId(), c.getNombre(), c.getDireccion(), c.getCiudad(), c.getTelefono()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultorioDto> misConsultorios(String email) {
        Set<Long> clinicaIds = usuario(email).getClinicas().stream().map(Clinica::getId).collect(Collectors.toSet());
        if (clinicaIds.isEmpty()) {
            return List.of();
        }
        return consultorioRepo.findByClinicaIdIn(clinicaIds).stream()
                .map(MeService::toConsultorioDto)
                .toList();
    }

    private Usuario usuario(String email) {
        return usuarioRepo.findByEmail(email).orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));
    }

    private static ConsultorioDto toConsultorioDto(Consultorio c) {
        return new ConsultorioDto(c.getId(), c.getClinica().getId(), c.getNombre(), c.getCapacidadSimultanea());
    }
}
