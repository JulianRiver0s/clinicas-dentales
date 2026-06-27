package com.nelumbo.citas_api.services;

import com.nelumbo.citas_api.dto.GananciasDto;
import com.nelumbo.citas_api.dto.OdontologoAtencionesDto;
import com.nelumbo.citas_api.dto.PacienteAtencionesDto;
import com.nelumbo.citas_api.dto.PrimeraVezDto;
import com.nelumbo.citas_api.dto.ProcedimientoSolicitadoDto;
import com.nelumbo.citas_api.repositories.CitaRepository;
import com.nelumbo.citas_api.repositories.HistoricoFinancieroRepository;
import com.nelumbo.citas_api.services.ScopeResolver.Scope;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IndicadorService {

    private static final int TOP_PACIENTES = 10;
    private static final int TOP_RANKING = 3;

    private final CitaRepository citaRepo;
    private final HistoricoFinancieroRepository historicoRepo;
    private final ScopeResolver scope;

    // Para el recepcionista, "red" se acota a sus clínicas asociadas; el admin ve toda la red.
    @Transactional(readOnly = true)
    public List<PacienteAtencionesDto> topPacientesRed(String email) {
        Scope s = scope.resolver(email);
        return citaRepo.topPacientes(s.filtrar(), s.clinicaIds(), PageRequest.of(0, TOP_PACIENTES));
    }

    @Transactional(readOnly = true)
    public List<PacienteAtencionesDto> topPacientesClinica(String email, Long clinicaId) {
        scope.exigirAcceso(scope.resolver(email), clinicaId);
        return citaRepo.topPacientes(true, Set.of(clinicaId), PageRequest.of(0, TOP_PACIENTES));
    }

    @Transactional(readOnly = true)
    public List<PrimeraVezDto> primeraVezHoy(String email) {
        Scope s = scope.resolver(email);
        LocalDate hoy = Periodos.hoy(Instant.now());
        return citaRepo.primeraVezHoy(s.filtrar(), s.clinicaIds(), Periodos.inicioDelDia(hoy), Periodos.finDelDia(hoy));
    }

    @Transactional(readOnly = true)
    public GananciasDto ganancias(String email, Long clinicaId) {
        scope.exigirAcceso(scope.resolver(email), clinicaId);
        Instant ahora = Instant.now();
        return new GananciasDto(clinicaId,
                historicoRepo.sumarCobros(clinicaId, Periodos.inicioDelDia(Periodos.hoy(ahora))),
                historicoRepo.sumarCobros(clinicaId, Periodos.inicioDeSemana(ahora)),
                historicoRepo.sumarCobros(clinicaId, Periodos.inicioDeMes(ahora)),
                historicoRepo.sumarCobros(clinicaId, Periodos.inicioDeAnio(ahora)));
    }

    @Transactional(readOnly = true)
    public List<OdontologoAtencionesDto> topOdontologosMes() {
        Instant ahora = Instant.now();
        return citaRepo.topOdontologosMes(Periodos.inicioDeMes(ahora), Periodos.finDeMes(ahora),
                PageRequest.of(0, TOP_RANKING));
    }

    @Transactional(readOnly = true)
    public List<ProcedimientoSolicitadoDto> topProcedimientosMes() {
        Instant ahora = Instant.now();
        return citaRepo.topProcedimientosMes(Periodos.inicioDeMes(ahora), Periodos.finDeMes(ahora),
                PageRequest.of(0, TOP_RANKING));
    }
}
