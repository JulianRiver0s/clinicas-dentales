package com.nelumbo.citas_api.services;

import com.nelumbo.citas_api.dto.ClinicaDto;
import com.nelumbo.citas_api.entities.Clinica;
import com.nelumbo.citas_api.entities.Usuario;
import com.nelumbo.citas_api.exception.ApiException;
import com.nelumbo.citas_api.repositories.ClinicaRepository;
import com.nelumbo.citas_api.repositories.UsuarioRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClinicaService {

    private final ClinicaRepository repo;
    private final UsuarioRepository usuarioRepo;

    @Transactional(readOnly = true)
    public List<ClinicaDto> listar() {
        return repo.findAll().stream().map(ClinicaService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ClinicaDto obtener(Long id) {
        return toDto(buscar(id));
    }

    @Transactional
    public ClinicaDto crear(ClinicaDto dto) {
        Clinica c = Clinica.builder()
                .nombre(dto.nombre())
                .direccion(dto.direccion())
                .ciudad(dto.ciudad())
                .telefono(dto.telefono())
                .creadoEn(Instant.now())
                .build();
        return toDto(repo.save(c));
    }

    @Transactional
    public ClinicaDto actualizar(Long id, ClinicaDto dto) {
        Clinica c = buscar(id);
        c.setNombre(dto.nombre());
        c.setDireccion(dto.direccion());
        c.setCiudad(dto.ciudad());
        c.setTelefono(dto.telefono());
        return toDto(repo.save(c));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw ApiException.noEncontrado("Clínica no encontrada");
        }
        repo.deleteById(id);
    }

    @Transactional
    public void asociarRecepcionista(Long clinicaId, Long usuarioId) {
        Clinica clinica = buscar(clinicaId);
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));
        if (!"RECEPCIONISTA".equals(usuario.getRol().getNombre())) {
            throw ApiException.negocio("El usuario no es un recepcionista");
        }
        usuario.getClinicas().add(clinica);
        usuarioRepo.save(usuario);
    }

    private Clinica buscar(Long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.noEncontrado("Clínica no encontrada"));
    }

    private static ClinicaDto toDto(Clinica c) {
        return new ClinicaDto(c.getId(), c.getNombre(), c.getDireccion(), c.getCiudad(), c.getTelefono());
    }
}
