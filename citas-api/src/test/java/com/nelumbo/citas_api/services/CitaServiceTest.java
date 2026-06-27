package com.nelumbo.citas_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nelumbo.citas_api.entities.EstadoCita;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

// Test puro de la lógica de agenda (sin contexto Spring): solapamiento, igualdad de día y reglas financieras.
class CitaServiceTest {

    private static Instant t(String iso) {
        return Instant.parse(iso);
    }

    @Test
    void intervalosSemiabiertosQueSeTocanNoSolapan() {
        // [10:00,10:30) y [10:30,11:00) comparten límite → NO chocan.
        assertFalse(CitaService.solapa(
                t("2026-07-01T10:00:00Z"), t("2026-07-01T10:30:00Z"),
                t("2026-07-01T10:30:00Z"), t("2026-07-01T11:00:00Z")));
    }

    @Test
    void intervalosQueSeCruzanSolapan() {
        assertTrue(CitaService.solapa(
                t("2026-07-01T10:00:00Z"), t("2026-07-01T10:30:00Z"),
                t("2026-07-01T10:15:00Z"), t("2026-07-01T10:45:00Z")));
    }

    @Test
    void mismaFechaPorDiaLocalNoPorInstante() {
        // 2026-07-01 23:00 y 2026-07-01 08:00 UTC caen el mismo día en Bogotá (UTC-5).
        assertTrue(CitaService.mismaFecha(t("2026-07-01T13:00:00Z"), t("2026-07-01T20:00:00Z")));
        assertFalse(CitaService.mismaFecha(t("2026-07-01T13:00:00Z"), t("2026-07-02T13:00:00Z")));
    }

    @Test
    void cancelacionTardiaCobraEl30Porciento() {
        BigDecimal costo = new BigDecimal("100.00");
        Instant ahora = t("2026-07-01T10:00:00Z");
        // 12 h de antelación (<24 h) → cargo del 30%.
        assertEquals(new BigDecimal("30.00"),
                CitaService.cargoPorCancelacion(costo, t("2026-07-01T22:00:00Z"), ahora));
        // La cita ya pasó → también es tardía.
        assertEquals(new BigDecimal("30.00"),
                CitaService.cargoPorCancelacion(costo, t("2026-07-01T08:00:00Z"), ahora));
    }

    @Test
    void cancelacionConAntelacionNoCobra() {
        BigDecimal costo = new BigDecimal("100.00");
        Instant ahora = t("2026-07-01T10:00:00Z");
        // Exactamente 24 h: "24 horas o más" no genera cargo.
        assertEquals(new BigDecimal("0.00"),
                CitaService.cargoPorCancelacion(costo, t("2026-07-02T10:00:00Z"), ahora));
        assertEquals(new BigDecimal("0.00"),
                CitaService.cargoPorCancelacion(costo, t("2026-07-05T10:00:00Z"), ahora));
    }

    @Test
    void bloqueaAlTercerNoShow() {
        assertFalse(CitaService.debeBloquear(2));
        assertTrue(CitaService.debeBloquear(3));
        assertTrue(CitaService.debeBloquear(4));
    }

    @Test
    void elFlagDecideElEstadoInicial() {
        assertEquals(EstadoCita.PENDIENTE_APROBACION, CitaService.estadoInicial(true));
        assertEquals(EstadoCita.AGENDADA, CitaService.estadoInicial(false));
        // Sin flag (lo normal) la cita se agenda directamente.
        assertEquals(EstadoCita.AGENDADA, CitaService.estadoInicial(null));
    }
}
