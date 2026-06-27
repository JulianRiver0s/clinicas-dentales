package com.nelumbo.citas_api.services;

import com.nelumbo.citas_api.dto.ProcedimientoDto;
import com.nelumbo.citas_api.entities.Procedimiento;
import com.nelumbo.citas_api.exception.ApiException;
import com.nelumbo.citas_api.repositories.ProcedimientoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProcedimientoService {

    private final ProcedimientoRepository repo;

    @Transactional(readOnly = true)
    public List<ProcedimientoDto> listar() {
        return repo.findAll().stream().map(ProcedimientoService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ProcedimientoDto obtener(Long id) {
        return toDto(buscar(id));
    }

    @Transactional
    public ProcedimientoDto crear(ProcedimientoDto dto) {
        Procedimiento p = Procedimiento.builder()
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .costo(dto.costo())
                .duracionMinutos(dto.duracionMinutos())
                .build();
        return toDto(repo.save(p));
    }

    @Transactional
    public ProcedimientoDto actualizar(Long id, ProcedimientoDto dto) {
        Procedimiento p = buscar(id);
        p.setNombre(dto.nombre());
        p.setDescripcion(dto.descripcion());
        p.setCosto(dto.costo());
        p.setDuracionMinutos(dto.duracionMinutos());
        return toDto(repo.save(p));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw ApiException.noEncontrado("Procedimiento no encontrado");
        }
        repo.deleteById(id);
    }

    private Procedimiento buscar(Long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.noEncontrado("Procedimiento no encontrado"));
    }

    private static ProcedimientoDto toDto(Procedimiento p) {
        return new ProcedimientoDto(p.getId(), p.getNombre(), p.getDescripcion(), p.getCosto(), p.getDuracionMinutos());
    }
}
