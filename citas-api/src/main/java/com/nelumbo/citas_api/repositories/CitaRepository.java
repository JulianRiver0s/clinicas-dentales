package com.nelumbo.citas_api.repositories;

import com.nelumbo.citas_api.dto.OdontologoAtencionesDto;
import com.nelumbo.citas_api.dto.PacienteAtencionesDto;
import com.nelumbo.citas_api.dto.PrimeraVezDto;
import com.nelumbo.citas_api.dto.ProcedimientoSolicitadoDto;
import com.nelumbo.citas_api.entities.Cita;
import com.nelumbo.citas_api.entities.EstadoCita;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByPacienteDocumentoAndEstadoIn(String documento, Collection<EstadoCita> estados);

    List<Cita> findByOdontologoIdAndEstadoIn(Long odontologoId, Collection<EstadoCita> estados);

    List<Cita> findByConsultorioIdAndEstadoIn(Long consultorioId, Collection<EstadoCita> estados);

    boolean existsByPacienteDocumentoAndClinicaIdAndEstado(String documento, Long clinicaId, EstadoCita estado);

    // El predicado (:filtrar = false OR c.clinica.id IN :clinicaIds) acota al recepcionista; el admin pasa filtrar=false.

    // Top pacientes por atenciones (ATENDIDA). Sirve a "red" (filtrar según rol) y a "clínica" (filtrar=true, un solo id).
    @Query("""
            SELECT new com.nelumbo.citas_api.dto.PacienteAtencionesDto(
                c.paciente.documento, c.paciente.nombre, COUNT(c))
            FROM Cita c
            WHERE c.estado = com.nelumbo.citas_api.entities.EstadoCita.ATENDIDA
              AND (:filtrar = false OR c.clinica.id IN :clinicaIds)
            GROUP BY c.paciente.documento, c.paciente.nombre
            ORDER BY COUNT(c) DESC""")
    List<PacienteAtencionesDto> topPacientes(
            @Param("filtrar") boolean filtrar, @Param("clinicaIds") Collection<Long> clinicaIds, Pageable pageable);

    // De las citas de hoy, las de pacientes sin ninguna cita previa en esa misma clínica.
    @Query("""
            SELECT new com.nelumbo.citas_api.dto.PrimeraVezDto(
                c.paciente.documento, c.paciente.nombre, c.clinica.id, c.clinica.nombre, c.fechaHoraCita)
            FROM Cita c
            WHERE c.fechaHoraCita >= :inicioHoy AND c.fechaHoraCita < :finHoy
              AND (:filtrar = false OR c.clinica.id IN :clinicaIds)
              AND NOT EXISTS (
                SELECT 1 FROM Cita p
                WHERE p.paciente.documento = c.paciente.documento
                  AND p.clinica.id = c.clinica.id
                  AND p.fechaHoraCita < :inicioHoy)
            ORDER BY c.fechaHoraCita""")
    List<PrimeraVezDto> primeraVezHoy(
            @Param("filtrar") boolean filtrar, @Param("clinicaIds") Collection<Long> clinicaIds,
            @Param("inicioHoy") Instant inicioHoy, @Param("finHoy") Instant finHoy);

    // Top odontólogos por atenciones del mes (a nivel de red; el endpoint es solo ADMIN).
    @Query("""
            SELECT new com.nelumbo.citas_api.dto.OdontologoAtencionesDto(
                c.odontologo.id, c.odontologo.nombre, COUNT(c))
            FROM Cita c
            WHERE c.estado = com.nelumbo.citas_api.entities.EstadoCita.ATENDIDA
              AND c.fechaHoraCita >= :inicioMes AND c.fechaHoraCita < :finMes
            GROUP BY c.odontologo.id, c.odontologo.nombre
            ORDER BY COUNT(c) DESC""")
    List<OdontologoAtencionesDto> topOdontologosMes(
            @Param("inicioMes") Instant inicioMes, @Param("finMes") Instant finMes, Pageable pageable);

    // Top procedimientos más solicitados del mes (cualquier estado; a nivel de red, endpoint solo ADMIN).
    @Query("""
            SELECT new com.nelumbo.citas_api.dto.ProcedimientoSolicitadoDto(
                c.procedimiento.id, c.procedimiento.nombre, COUNT(c))
            FROM Cita c
            WHERE c.fechaHoraCita >= :inicioMes AND c.fechaHoraCita < :finMes
            GROUP BY c.procedimiento.id, c.procedimiento.nombre
            ORDER BY COUNT(c) DESC""")
    List<ProcedimientoSolicitadoDto> topProcedimientosMes(
            @Param("inicioMes") Instant inicioMes, @Param("finMes") Instant finMes, Pageable pageable);

    // Citas del día por clínica y/o consultorio. JOIN FETCH para mapear sin N+1.
    @Query("""
            SELECT c FROM Cita c
            JOIN FETCH c.paciente
            JOIN FETCH c.odontologo
            JOIN FETCH c.procedimiento
            JOIN FETCH c.clinica
            JOIN FETCH c.consultorio
            WHERE c.fechaHoraCita >= :inicioDia AND c.fechaHoraCita < :finDia
              AND (:clinicaId IS NULL OR c.clinica.id = :clinicaId)
              AND (:consultorioId IS NULL OR c.consultorio.id = :consultorioId)
              AND (:filtrar = false OR c.clinica.id IN :clinicaIds)
            ORDER BY c.fechaHoraCita""")
    List<Cita> citasDelDia(
            @Param("inicioDia") Instant inicioDia, @Param("finDia") Instant finDia,
            @Param("clinicaId") Long clinicaId, @Param("consultorioId") Long consultorioId,
            @Param("filtrar") boolean filtrar, @Param("clinicaIds") Collection<Long> clinicaIds);

    // Listado de citas (agendadas y atendidas); todos los filtros son opcionales.
    // Cada filtro va con su bandera (:filtrarX = false ⇒ no aplica): así un parámetro nulo solo aparece junto a
    // su columna y nunca en un "IS NULL" suelto, que Postgres no puede tipar para timestamp/enum.
    @Query("""
            SELECT c FROM Cita c
            JOIN FETCH c.paciente
            JOIN FETCH c.odontologo
            JOIN FETCH c.procedimiento
            JOIN FETCH c.clinica
            JOIN FETCH c.consultorio
            WHERE (:filtrarEstado = false OR c.estado = :estado)
              AND (:filtrarClinica = false OR c.clinica.id = :clinicaId)
              AND (:filtrarFecha = false OR (c.fechaHoraCita >= :inicioDia AND c.fechaHoraCita < :finDia))
              AND (:filtrar = false OR c.clinica.id IN :clinicaIds)
            ORDER BY c.fechaHoraCita DESC""")
    List<Cita> listar(
            @Param("filtrarEstado") boolean filtrarEstado, @Param("estado") EstadoCita estado,
            @Param("filtrarClinica") boolean filtrarClinica, @Param("clinicaId") Long clinicaId,
            @Param("filtrarFecha") boolean filtrarFecha,
            @Param("inicioDia") Instant inicioDia, @Param("finDia") Instant finDia,
            @Param("filtrar") boolean filtrar, @Param("clinicaIds") Collection<Long> clinicaIds);

    // Búsqueda por coincidencia parcial del documento del paciente.
    @Query("""
            SELECT c FROM Cita c
            JOIN FETCH c.paciente
            JOIN FETCH c.odontologo
            JOIN FETCH c.procedimiento
            JOIN FETCH c.clinica
            JOIN FETCH c.consultorio
            WHERE c.paciente.documento LIKE CONCAT('%', :documento, '%')
              AND (:filtrar = false OR c.clinica.id IN :clinicaIds)
            ORDER BY c.fechaHoraCita DESC""")
    List<Cita> buscarPorDocumento(
            @Param("documento") String documento,
            @Param("filtrar") boolean filtrar, @Param("clinicaIds") Collection<Long> clinicaIds);
}
