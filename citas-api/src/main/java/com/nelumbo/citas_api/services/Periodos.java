package com.nelumbo.citas_api.services;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

// Límites de los periodos (hoy/semana/mes/año) en la zona de la red de clínicas.
// La semana empieza el lunes (ISO). CitaService define su propia ZONA para sus reglas; aquí se mantiene la misma.
public final class Periodos {

    public static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private Periodos() {
    }

    public static LocalDate hoy(Instant ahora) {
        return ahora.atZone(ZONA).toLocalDate();
    }

    public static Instant inicioDelDia(LocalDate fecha) {
        return fecha.atStartOfDay(ZONA).toInstant();
    }

    public static Instant finDelDia(LocalDate fecha) {
        return fecha.plusDays(1).atStartOfDay(ZONA).toInstant();
    }

    public static Instant inicioDeSemana(Instant ahora) {
        return hoy(ahora).with(DayOfWeek.MONDAY).atStartOfDay(ZONA).toInstant();
    }

    public static Instant inicioDeMes(Instant ahora) {
        return hoy(ahora).withDayOfMonth(1).atStartOfDay(ZONA).toInstant();
    }

    public static Instant finDeMes(Instant ahora) {
        return hoy(ahora).withDayOfMonth(1).plusMonths(1).atStartOfDay(ZONA).toInstant();
    }

    public static Instant inicioDeAnio(Instant ahora) {
        return hoy(ahora).withDayOfYear(1).atStartOfDay(ZONA).toInstant();
    }
}
