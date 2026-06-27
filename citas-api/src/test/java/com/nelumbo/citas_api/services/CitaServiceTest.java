package com.nelumbo.citas_api.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

// Test puro de la lógica de agenda (sin contexto Spring): solapamiento e igualdad de día.
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
}
