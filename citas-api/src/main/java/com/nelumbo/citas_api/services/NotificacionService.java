package com.nelumbo.citas_api.services;

import com.nelumbo.citas_api.dto.EnviarNotificacionRequest;
import com.nelumbo.citas_api.dto.MensajeResponse;
import com.nelumbo.citas_api.dto.NotificacionRequest;
import com.nelumbo.citas_api.entities.Clinica;
import com.nelumbo.citas_api.exception.ApiException;
import com.nelumbo.citas_api.repositories.CitaRepository;
import com.nelumbo.citas_api.repositories.ClinicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Orquesta el envío manual de notificaciones: valida que el paciente tenga cita en la clínica,
// traduce clinicaId→clinicaNombre y propaga la respuesta del microservicio.
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final CitaRepository citaRepo;
    private final ClinicaRepository clinicaRepo;
    private final NotificacionClient cliente;

    public MensajeResponse enviar(EnviarNotificacionRequest req) {
        Clinica clinica = clinicaRepo.findById(req.clinicaId())
                .orElseThrow(() -> ApiException.noEncontrado("Clínica no encontrada"));
        if (!citaRepo.existsByPacienteDocumentoAndClinicaId(req.documento(), clinica.getId())) {
            throw ApiException.negocio("El paciente no tiene citas en la clínica indicada");
        }
        return cliente.notificar(new NotificacionRequest(
                req.email(), req.documento(), req.mensaje(), clinica.getNombre()));
    }
}
