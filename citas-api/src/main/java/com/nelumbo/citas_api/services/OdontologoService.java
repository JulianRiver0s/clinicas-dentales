package com.nelumbo.citas_api.services;

import com.nelumbo.citas_api.dto.OdontologoDto;
import com.nelumbo.citas_api.entities.Clinica;
import com.nelumbo.citas_api.entities.Odontologo;
import com.nelumbo.citas_api.exception.ApiException;
import com.nelumbo.citas_api.repositories.ClinicaRepository;
import com.nelumbo.citas_api.repositories.OdontologoRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OdontologoService {

    private final OdontologoRepository repo;
    private final ClinicaRepository clinicaRepo;

    @Transactional(readOnly = true)
    public List<OdontologoDto> listar() {
        return repo.findAll().stream().map(OdontologoService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public OdontologoDto obtener(Long id) {
        return toDto(buscar(id));
    }

    @Transactional
    public OdontologoDto crear(OdontologoDto dto) {
        if (repo.existsByDocumento(dto.documento())) {
            throw ApiException.conflicto("El documento ya está registrado");
        }
        Odontologo o = Odontologo.builder()
                .documento(dto.documento())
                .nombre(dto.nombre())
                .especialidad(dto.especialidad())
                .clinicas(resolverClinicas(dto.clinicaIds()))
                .build();
        return toDto(repo.save(o));
    }

    @Transactional
    public OdontologoDto actualizar(Long id, OdontologoDto dto) {
        Odontologo o = buscar(id);
        if (repo.existsByDocumentoAndIdNot(dto.documento(), id)) {
            throw ApiException.conflicto("El documento ya está registrado");
        }
        o.setDocumento(dto.documento());
        o.setNombre(dto.nombre());
        o.setEspecialidad(dto.especialidad());
        o.setClinicas(resolverClinicas(dto.clinicaIds()));
        return toDto(repo.save(o));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw ApiException.noEncontrado("Odontólogo no encontrado");
        }
        repo.deleteById(id);
    }

    private Odontologo buscar(Long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.noEncontrado("Odontólogo no encontrado"));
    }

    private Set<Clinica> resolverClinicas(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return ids.stream()
                .map(cid -> clinicaRepo.findById(cid)
                        .orElseThrow(() -> ApiException.noEncontrado("Clínica no encontrada: " + cid)))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static OdontologoDto toDto(Odontologo o) {
        Set<Long> ids = o.getClinicas().stream().map(Clinica::getId).collect(Collectors.toSet());
        return new OdontologoDto(o.getId(), o.getDocumento(), o.getNombre(), o.getEspecialidad(), ids);
    }
}
