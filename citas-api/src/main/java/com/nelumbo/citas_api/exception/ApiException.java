package com.nelumbo.citas_api.exception;

import org.springframework.http.HttpStatus;

// Excepción de negocio: lleva su propio status HTTP y el mensaje a devolver.
// Una sola clase con status en vez de una jerarquía NotFound/Conflict/Forbidden; los factory cubren los casos comunes.
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException noEncontrado(String mensaje) {
        return new ApiException(HttpStatus.NOT_FOUND, mensaje);
    }

    public static ApiException conflicto(String mensaje) {
        return new ApiException(HttpStatus.CONFLICT, mensaje);
    }

    public static ApiException negocio(String mensaje) {
        return new ApiException(HttpStatus.BAD_REQUEST, mensaje);
    }
}
