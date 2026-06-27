-- Datos demo mínimos: 1 clínica, 1 consultorio, 1 odontólogo, 2 procedimientos y 1 recepcionista asociado.
-- Recepcionista: recepcion@mail.com / recepcion.
DO $$
DECLARE
    v_clinica_id     BIGINT;
    v_consultorio_id BIGINT;
    v_odontologo_id  BIGINT;
    v_rol_recep_id   BIGINT;
    v_usuario_id     BIGINT;
BEGIN
    INSERT INTO clinicas (nombre, direccion, ciudad, telefono)
    VALUES ('Clínica Central', 'Calle 1 #2-3', 'Bogotá', '6011234567')
    RETURNING id INTO v_clinica_id;

    INSERT INTO consultorios (clinica_id, nombre, capacidad_simultanea)
    VALUES (v_clinica_id, 'Consultorio 1', 1)
    RETURNING id INTO v_consultorio_id;

    INSERT INTO odontologos (documento, nombre, especialidad)
    VALUES ('1001', 'Ana Pérez', 'Odontología general')
    RETURNING id INTO v_odontologo_id;

    INSERT INTO odontologo_clinica (odontologo_id, clinica_id)
    VALUES (v_odontologo_id, v_clinica_id);

    INSERT INTO procedimientos (nombre, descripcion, costo, duracion_minutos) VALUES
        ('Limpieza dental',    'Profilaxis y remoción de placa',      80000, 30),
        ('Extracción simple', 'Exodoncia de pieza sin complicación', 120000, 45);

    SELECT id INTO v_rol_recep_id FROM roles WHERE nombre = 'RECEPCIONISTA';

    INSERT INTO usuarios (nombre, email, password_hash, activo, rol_id)
    VALUES ('Recepción Central', 'recepcion@mail.com', crypt('recepcion', gen_salt('bf')), TRUE, v_rol_recep_id)
    RETURNING id INTO v_usuario_id;

    INSERT INTO recepcionista_clinica (usuario_id, clinica_id)
    VALUES (v_usuario_id, v_clinica_id);
END $$;
