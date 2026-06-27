-- Datos demo para que los indicadores y las consultas devuelvan información coherente sin crearla a mano.
-- Añade una 2.ª clínica (no asociada al recepcionista) para evidenciar el aislamiento por rol.
-- Las fechas son relativas a now() para que el seed siga siendo "del mes" sin reescribirlo.
-- Cerca del cambio de mes/semana una cita anclada a date_trunc('week'/'month') puede caer en el
-- periodo anterior; es data de ejemplo, no afecta la lógica de los endpoints.
DO $$
DECLARE
    v_clinica1 BIGINT;
    v_cons1    BIGINT;
    v_od1      BIGINT;
    v_p1       BIGINT;  -- Limpieza dental (80000)
    v_p2       BIGINT;  -- Extracción simple (120000)
    v_clinica2 BIGINT;
    v_cons2    BIGINT;
    v_od2      BIGINT;
    v_cita     BIGINT;

    v_hoy1   TIMESTAMPTZ := date_trunc('day', now())   + interval '9 hours';
    v_hoy2   TIMESTAMPTZ := date_trunc('day', now())   + interval '11 hours';
    v_hoy3   TIMESTAMPTZ := date_trunc('day', now())   + interval '13 hours';
    v_hoy4   TIMESTAMPTZ := date_trunc('day', now())   + interval '15 hours';
    v_hoy5   TIMESTAMPTZ := date_trunc('day', now())   + interval '16 hours';
    v_sem1   TIMESTAMPTZ := date_trunc('week', now())  + interval '10 hours';
    v_sem2   TIMESTAMPTZ := date_trunc('week', now())  + interval '14 hours';
    v_mes1   TIMESTAMPTZ := date_trunc('month', now()) + interval '1 day' + interval '10 hours';
    v_mes2   TIMESTAMPTZ := date_trunc('month', now()) + interval '1 day' + interval '12 hours';
BEGIN
    SELECT id INTO v_clinica1 FROM clinicas      WHERE nombre = 'Clínica Central' LIMIT 1;
    SELECT id INTO v_cons1    FROM consultorios  WHERE clinica_id = v_clinica1 ORDER BY id LIMIT 1;
    SELECT id INTO v_od1      FROM odontologos   WHERE documento = '1001';
    SELECT id INTO v_p1       FROM procedimientos WHERE nombre = 'Limpieza dental';
    SELECT id INTO v_p2       FROM procedimientos WHERE nombre = 'Extracción simple';

    -- 2.ª clínica con su consultorio y odontólogo (sin recepcionista asociado).
    INSERT INTO clinicas (nombre, direccion, ciudad, telefono)
    VALUES ('Clínica Norte', 'Av 5 #6-7', 'Bogotá', '6017654321')
    RETURNING id INTO v_clinica2;

    INSERT INTO consultorios (clinica_id, nombre, capacidad_simultanea)
    VALUES (v_clinica2, 'Consultorio Norte', 1)
    RETURNING id INTO v_cons2;

    INSERT INTO odontologos (documento, nombre, especialidad)
    VALUES ('1002', 'Luis Romero', 'Ortodoncia')
    RETURNING id INTO v_od2;

    INSERT INTO odontologo_clinica (odontologo_id, clinica_id) VALUES (v_od2, v_clinica2);

    INSERT INTO pacientes (documento, nombre) VALUES
        ('1010', 'Carlos Gómez'),
        ('2020', 'María López'),
        ('3030', 'Pedro Ruiz'),
        ('4040', 'Lucía Díaz'),
        ('5050', 'Jorge Sánchez'),
        ('6060', 'Sofía Castro');

    -- ===== Clínica Central =====
    -- Carlos: 3 atenciones (top paciente). Hoy, esta semana y este mes.
    INSERT INTO citas (paciente_documento, consultorio_id, odontologo_id, procedimiento_id, clinica_id,
                       fecha_hora_cita, checkin_en, inicio_real, fin_real, estado, costo)
    VALUES ('1010', v_cons1, v_od1, v_p1, v_clinica1, v_hoy1, v_hoy1, v_hoy1, v_hoy1 + interval '30 min', 'ATENDIDA', 80000)
    RETURNING id INTO v_cita;
    INSERT INTO historico_financiero (cita_id, paciente_documento, clinica_id, tipo, monto, fecha_hora, estado_cita)
    VALUES (v_cita, '1010', v_clinica1, 'COBRO_PROCEDIMIENTO', 80000, v_hoy1, 'ATENDIDA');

    INSERT INTO citas (paciente_documento, consultorio_id, odontologo_id, procedimiento_id, clinica_id,
                       fecha_hora_cita, checkin_en, inicio_real, fin_real, estado, costo)
    VALUES ('1010', v_cons1, v_od1, v_p2, v_clinica1, v_sem1, v_sem1, v_sem1, v_sem1 + interval '45 min', 'ATENDIDA', 120000)
    RETURNING id INTO v_cita;
    INSERT INTO historico_financiero (cita_id, paciente_documento, clinica_id, tipo, monto, fecha_hora, estado_cita)
    VALUES (v_cita, '1010', v_clinica1, 'COBRO_PROCEDIMIENTO', 120000, v_sem1, 'ATENDIDA');

    INSERT INTO citas (paciente_documento, consultorio_id, odontologo_id, procedimiento_id, clinica_id,
                       fecha_hora_cita, checkin_en, inicio_real, fin_real, estado, costo)
    VALUES ('1010', v_cons1, v_od1, v_p1, v_clinica1, v_mes1, v_mes1, v_mes1, v_mes1 + interval '30 min', 'ATENDIDA', 80000)
    RETURNING id INTO v_cita;
    INSERT INTO historico_financiero (cita_id, paciente_documento, clinica_id, tipo, monto, fecha_hora, estado_cita)
    VALUES (v_cita, '1010', v_clinica1, 'COBRO_PROCEDIMIENTO', 80000, v_mes1, 'ATENDIDA');

    -- María: 2 atenciones.
    INSERT INTO citas (paciente_documento, consultorio_id, odontologo_id, procedimiento_id, clinica_id,
                       fecha_hora_cita, checkin_en, inicio_real, fin_real, estado, costo)
    VALUES ('2020', v_cons1, v_od1, v_p1, v_clinica1, v_hoy2, v_hoy2, v_hoy2, v_hoy2 + interval '30 min', 'ATENDIDA', 80000)
    RETURNING id INTO v_cita;
    INSERT INTO historico_financiero (cita_id, paciente_documento, clinica_id, tipo, monto, fecha_hora, estado_cita)
    VALUES (v_cita, '2020', v_clinica1, 'COBRO_PROCEDIMIENTO', 80000, v_hoy2, 'ATENDIDA');

    INSERT INTO citas (paciente_documento, consultorio_id, odontologo_id, procedimiento_id, clinica_id,
                       fecha_hora_cita, checkin_en, inicio_real, fin_real, estado, costo)
    VALUES ('2020', v_cons1, v_od1, v_p2, v_clinica1, v_sem2, v_sem2, v_sem2, v_sem2 + interval '45 min', 'ATENDIDA', 120000)
    RETURNING id INTO v_cita;
    INSERT INTO historico_financiero (cita_id, paciente_documento, clinica_id, tipo, monto, fecha_hora, estado_cita)
    VALUES (v_cita, '2020', v_clinica1, 'COBRO_PROCEDIMIENTO', 120000, v_sem2, 'ATENDIDA');

    -- Pedro: 1 atención, 1 cancelación tardía (cargo 30%, no cuenta como ganancia) y 1 inasistencia.
    INSERT INTO citas (paciente_documento, consultorio_id, odontologo_id, procedimiento_id, clinica_id,
                       fecha_hora_cita, checkin_en, inicio_real, fin_real, estado, costo)
    VALUES ('3030', v_cons1, v_od1, v_p1, v_clinica1, v_mes2, v_mes2, v_mes2, v_mes2 + interval '30 min', 'ATENDIDA', 80000)
    RETURNING id INTO v_cita;
    INSERT INTO historico_financiero (cita_id, paciente_documento, clinica_id, tipo, monto, fecha_hora, estado_cita)
    VALUES (v_cita, '3030', v_clinica1, 'COBRO_PROCEDIMIENTO', 80000, v_mes2, 'ATENDIDA');

    INSERT INTO citas (paciente_documento, consultorio_id, odontologo_id, procedimiento_id, clinica_id,
                       fecha_hora_cita, estado, costo)
    VALUES ('3030', v_cons1, v_od1, v_p2, v_clinica1, v_hoy3, 'CANCELADA', 120000)
    RETURNING id INTO v_cita;
    INSERT INTO historico_financiero (cita_id, paciente_documento, clinica_id, tipo, monto, fecha_hora, estado_cita)
    VALUES (v_cita, '3030', v_clinica1, 'CARGO_POR_CANCELACION_TARDIA', 36000, v_hoy3, 'CANCELADA');

    INSERT INTO citas (paciente_documento, consultorio_id, odontologo_id, procedimiento_id, clinica_id,
                       fecha_hora_cita, estado, costo)
    VALUES ('3030', v_cons1, v_od1, v_p1, v_clinica1, v_sem1 + interval '1 hour', 'INASISTENCIA', 80000)
    RETURNING id INTO v_cita;
    INSERT INTO historico_financiero (cita_id, paciente_documento, clinica_id, tipo, monto, fecha_hora, estado_cita)
    VALUES (v_cita, '3030', v_clinica1, 'SIN_CARGO', 0, v_sem1 + interval '1 hour', 'INASISTENCIA');

    -- Agendadas de hoy: Lucía es primera vez en la clínica; Carlos no (ya tiene historia).
    INSERT INTO citas (paciente_documento, consultorio_id, odontologo_id, procedimiento_id, clinica_id,
                       fecha_hora_cita, estado, costo)
    VALUES ('4040', v_cons1, v_od1, v_p1, v_clinica1, v_hoy4, 'AGENDADA', 80000);

    INSERT INTO citas (paciente_documento, consultorio_id, odontologo_id, procedimiento_id, clinica_id,
                       fecha_hora_cita, estado, costo)
    VALUES ('1010', v_cons1, v_od1, v_p1, v_clinica1, v_hoy5, 'AGENDADA', 80000);

    -- ===== Clínica Norte (el recepcionista de la Central no debe ver esto) =====
    INSERT INTO citas (paciente_documento, consultorio_id, odontologo_id, procedimiento_id, clinica_id,
                       fecha_hora_cita, checkin_en, inicio_real, fin_real, estado, costo)
    VALUES ('5050', v_cons2, v_od2, v_p1, v_clinica2, v_hoy1, v_hoy1, v_hoy1, v_hoy1 + interval '30 min', 'ATENDIDA', 80000)
    RETURNING id INTO v_cita;
    INSERT INTO historico_financiero (cita_id, paciente_documento, clinica_id, tipo, monto, fecha_hora, estado_cita)
    VALUES (v_cita, '5050', v_clinica2, 'COBRO_PROCEDIMIENTO', 80000, v_hoy1, 'ATENDIDA');

    INSERT INTO citas (paciente_documento, consultorio_id, odontologo_id, procedimiento_id, clinica_id,
                       fecha_hora_cita, estado, costo)
    VALUES ('6060', v_cons2, v_od2, v_p1, v_clinica2, v_hoy2, 'AGENDADA', 80000);
END $$;
