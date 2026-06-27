package com.nelumbo.citas_api.services;

import com.nelumbo.citas_api.dto.ConsultorioDto;
import com.nelumbo.citas_api.entities.Clinica;
import com.nelumbo.citas_api.entities.Consultorio;
import com.nelumbo.citas_api.exception.ApiException;
import com.nelumbo.citas_api.repositories.ClinicaRepository;
import com.nelumbo.citas_api.repositories.ConsultorioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsultorioService {

    private final ConsultorioRepository repo;
    private final ClinicaRepository clinicaRepo;

    @Transactional(readOnly = true)
    public List<ConsultorioDto> listar() {
        return repo.findAll().stream().map(ConsultorioService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ConsultorioDto obtener(Long id) {
        return toDto(buscar(id));
    }

    @Transactional
    public ConsultorioDto crear(ConsultorioDto dto) {
        Consultorio c = Consultorio.builder()
                .clinica(clinica(dto.clinicaId()))
                .nombre(dto.nombre())
                .capacidadSimultanea(dto.capacidadSimultanea())
                .build();
        return toDto(repo.save(c));
    }

    @Transactional
    public ConsultorioDto actualizar(Long id, ConsultorioDto dto) {
        Consultorio c = buscar(id);
        c.setClinica(clinica(dto.clinicaId()));
        c.setNombre(dto.nombre());
        c.setCapacidadSimultanea(dto.capacidadSimultanea());
        return toDto(repo.save(c));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw ApiException.noEncontrado("Consultorio no encontrado");
        }
        repo.deleteById(id);
    }

    private Consultorio buscar(Long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.noEncontrado("Consultorio no encontrado"));
    }

    private Clinica clinica(Long id) {
        return clinicaRepo.findById(id).orElseThrow(() -> ApiException.noEncontrado("Clínica no encontrada"));
    }

    private static ConsultorioDto toDto(Consultorio c) {
        return new ConsultorioDto(c.getId(), c.getClinica().getId(), c.getNombre(), c.getCapacidadSimultanea());
    }
}
