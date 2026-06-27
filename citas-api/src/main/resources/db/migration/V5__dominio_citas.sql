-- Catálogo, pacientes, citas, histórico y relaciones N:M.

CREATE TABLE clinicas (
    id        BIGSERIAL PRIMARY KEY,
    nombre    VARCHAR(120) NOT NULL,
    direccion VARCHAR(200),
    creado_en TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE consultorios (
    id                   BIGSERIAL PRIMARY KEY,
    clinica_id           BIGINT       NOT NULL REFERENCES clinicas(id),
    nombre               VARCHAR(120) NOT NULL,
    capacidad_simultanea INT          NOT NULL DEFAULT 1 CHECK (capacidad_simultanea >= 1)
);

CREATE TABLE odontologos (
    id           BIGSERIAL PRIMARY KEY,
    documento    VARCHAR(12)  NOT NULL UNIQUE,
    nombre       VARCHAR(120) NOT NULL,
    especialidad VARCHAR(120)
);

CREATE TABLE procedimientos (
    id               BIGSERIAL PRIMARY KEY,
    nombre           VARCHAR(120)  NOT NULL,
    costo            NUMERIC(12,2) NOT NULL CHECK (costo >= 0),
    duracion_minutos INT           NOT NULL CHECK (duracion_minutos > 0)
);

CREATE TABLE pacientes (
    documento       VARCHAR(12)  PRIMARY KEY,
    nombre          VARCHAR(120) NOT NULL,
    inasistencias   INT          NOT NULL DEFAULT 0,
    bloqueado_hasta TIMESTAMPTZ
);

CREATE TABLE citas (
    id                  BIGSERIAL PRIMARY KEY,
    paciente_documento  VARCHAR(12)   NOT NULL REFERENCES pacientes(documento),
    consultorio_id      BIGINT        NOT NULL REFERENCES consultorios(id),
    odontologo_id       BIGINT        NOT NULL REFERENCES odontologos(id),
    procedimiento_id    BIGINT        NOT NULL REFERENCES procedimientos(id),
    clinica_id          BIGINT        NOT NULL REFERENCES clinicas(id),
    fecha_hora_cita     TIMESTAMPTZ   NOT NULL,
    fecha_hora_creacion TIMESTAMPTZ   NOT NULL DEFAULT now(),
    inicio_real         TIMESTAMPTZ,
    fin_real            TIMESTAMPTZ,
    checkin_en          TIMESTAMPTZ,
    estado              VARCHAR(24)   NOT NULL,
    costo               NUMERIC(12,2) NOT NULL,
    creado_por          BIGINT        REFERENCES usuarios(id)
);

CREATE TABLE historico_financiero (
    id                 BIGSERIAL PRIMARY KEY,
    cita_id            BIGINT        NOT NULL REFERENCES citas(id),
    paciente_documento VARCHAR(12)   NOT NULL,
    clinica_id         BIGINT        NOT NULL,
    tipo               VARCHAR(32)   NOT NULL,
    monto              NUMERIC(12,2) NOT NULL,
    fecha_hora         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    estado_cita        VARCHAR(24)   NOT NULL
);

CREATE TABLE recepcionista_clinica (
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    clinica_id BIGINT NOT NULL REFERENCES clinicas(id),
    PRIMARY KEY (usuario_id, clinica_id)
);

CREATE TABLE odontologo_clinica (
    odontologo_id BIGINT NOT NULL REFERENCES odontologos(id),
    clinica_id    BIGINT NOT NULL REFERENCES clinicas(id),
    PRIMARY KEY (odontologo_id, clinica_id)
);

-- Índices: los accesos calientes son por horario, paciente y estado de la cita.
CREATE INDEX idx_cita_fecha_hora  ON citas(fecha_hora_cita);
CREATE INDEX idx_cita_paciente    ON citas(paciente_documento);
CREATE INDEX idx_cita_estado      ON citas(estado);
CREATE INDEX idx_cita_odontologo  ON citas(odontologo_id);
CREATE INDEX idx_cita_consultorio ON citas(consultorio_id);
