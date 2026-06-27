package com.nelumbo.citas_api.services;

import com.nelumbo.citas_api.dto.NotificacionRequest;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// Cliente del microservicio de notificaciones. La URL llega por env (app.notificaciones.base-url).
@Component
public class NotificacionClient {

    private static final Logger log = LoggerFactory.getLogger(NotificacionClient.class);

    private final RestClient http;

    public NotificacionClient(@Value("${app.notificaciones.base-url}") String baseUrl) {
        // Timeouts cortos: el envío ocurre dentro de la transacción del no-show, así un microservicio lento no la retiene.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        this.http = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    // Si el microservicio falla, se registra y se sigue; el bloqueo del paciente no depende del envío.
    // Se atrapa Exception (no solo RestClientException) para que ningún fallo de notificación revierta la transacción.
    public void enviar(NotificacionRequest req) {
        try {
            http.post().uri("/notificaciones")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(req)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("No se pudo notificar al documento {}: {}", req.documento(), e.getMessage());
        }
    }
}
