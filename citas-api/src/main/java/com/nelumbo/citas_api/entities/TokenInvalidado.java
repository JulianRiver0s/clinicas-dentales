package com.nelumbo.citas_api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Token revocado por logout. No se guarda el token en claro: solo su hash SHA-256 y la expiración (para purgar).
@Entity
@Table(name = "tokens_invalidados")
@Getter
@NoArgsConstructor
public class TokenInvalidado {

    @Id
    @Column(name = "token_hash", length = 64)
    private String tokenHash;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;

    private TokenInvalidado(String tokenHash, Instant expiraEn) {
        this.tokenHash = tokenHash;
        this.expiraEn = expiraEn;
    }

    public static TokenInvalidado de(String token, Instant expiraEn) {
        return new TokenInvalidado(hashDe(token), expiraEn);
    }

    public static String hashDe(String token) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e); // SHA-256 siempre existe
        }
    }
}
