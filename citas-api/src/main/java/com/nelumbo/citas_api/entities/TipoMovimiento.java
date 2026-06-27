package com.nelumbo.citas_api.entities;

public enum TipoMovimiento {
    COBRO_PROCEDIMIENTO,
    CARGO_POR_CANCELACION_TARDIA,
    // Movimiento sin valor monetario: cancelación con antelación o inasistencia, que igual deben quedar en el histórico.
    SIN_CARGO
}
