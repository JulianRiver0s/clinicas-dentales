package com.nelumbo.citas_api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pacientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {

    @Id
    @Column(length = 12)
    private String documento;

    @Column(length = 120, nullable = false)
    private String nombre;

    @Column(nullable = false)
    private int inasistencias;

    @Column(name = "bloqueado_hasta")
    private Instant bloqueadoHasta;
}
