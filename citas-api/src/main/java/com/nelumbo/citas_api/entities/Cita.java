package com.nelumbo.citas_api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "citas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "paciente_documento", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "consultorio_id", nullable = false)
    private Consultorio consultorio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "odontologo_id", nullable = false)
    private Odontologo odontologo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "procedimiento_id", nullable = false)
    private Procedimiento procedimiento;

    // Clínica denormalizada para acotar consultas e indicadores por sede.
    @ManyToOne(optional = false)
    @JoinColumn(name = "clinica_id", nullable = false)
    private Clinica clinica;

    @Column(name = "fecha_hora_cita", nullable = false)
    private Instant fechaHoraCita;

    @Column(name = "fecha_hora_creacion", nullable = false)
    private Instant fechaHoraCreacion;

    @Column(name = "inicio_real")
    private Instant inicioReal;

    @Column(name = "fin_real")
    private Instant finReal;

    @Column(name = "checkin_en")
    private Instant checkinEn;

    @Enumerated(EnumType.STRING)
    @Column(length = 24, nullable = false)
    private EstadoCita estado;

    // Snapshot del costo del procedimiento al agendar; no se relee al cobrar.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    @ManyToOne
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;
}
