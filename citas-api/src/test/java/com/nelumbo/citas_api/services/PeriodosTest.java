package com.nelumbo.citas_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

// Límites de periodo en zona Bogotá (UTC-5). 2026-06-27 13:00 local (= 18:00 UTC) es sábado.
class PeriodosTest {

    private static final Instant AHORA = Instant.parse("2026-06-27T18:00:00Z");

    @Test
    void rangoDelDiaEmpiezaYTerminaAMedianocheLocal() {
        LocalDate dia = LocalDate.of(2026, 6, 27);
        assertEquals(Instant.parse("2026-06-27T05:00:00Z"), Periodos.inicioDelDia(dia));
        assertEquals(Instant.parse("2026-06-28T05:00:00Z"), Periodos.finDelDia(dia));
    }

    @Test
    void semanaEmpiezaElLunes() {
        // Sábado 27: el lunes de esa semana es el 22.
        assertEquals(Instant.parse("2026-06-22T05:00:00Z"), Periodos.inicioDeSemana(AHORA));
    }

    @Test
    void mesYAnioSeAnclanAlPrimerDiaLocal() {
        assertEquals(Instant.parse("2026-06-01T05:00:00Z"), Periodos.inicioDeMes(AHORA));
        assertEquals(Instant.parse("2026-07-01T05:00:00Z"), Periodos.finDeMes(AHORA));
        assertEquals(Instant.parse("2026-01-01T05:00:00Z"), Periodos.inicioDeAnio(AHORA));
    }
}
