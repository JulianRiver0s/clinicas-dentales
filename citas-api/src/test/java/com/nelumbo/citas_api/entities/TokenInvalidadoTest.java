package com.nelumbo.citas_api.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TokenInvalidadoTest {

    @Test
    void hashEsDeterministaYDe64HexEnMinuscula() {
        String h = TokenInvalidado.hashDe("un.jwt.cualquiera");
        assertEquals(h, TokenInvalidado.hashDe("un.jwt.cualquiera"));
        assertEquals(64, h.length());
        assertTrue(h.matches("[0-9a-f]{64}"), h);
    }

    @Test
    void tokensDistintosDanHashDistinto() {
        assertNotEquals(TokenInvalidado.hashDe("token.a"), TokenInvalidado.hashDe("token.b"));
    }
}
