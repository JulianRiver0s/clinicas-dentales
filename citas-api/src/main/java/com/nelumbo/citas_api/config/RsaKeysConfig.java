package com.nelumbo.citas_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

// Vinculamos propiedades de configuracion con campos de la clase RsaKeysConfig
@ConfigurationProperties(prefix = "app.jwt")
public record RsaKeysConfig(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
}
