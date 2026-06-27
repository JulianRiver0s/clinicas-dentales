package com.nelumbo.citas_api.dto;

// Cuerpo que recibe el microservicio de notificaciones (ya con el nombre de la clínica resuelto).
public record NotificacionRequest(String email, String documento, String mensaje, String clinicaNombre) {
}
