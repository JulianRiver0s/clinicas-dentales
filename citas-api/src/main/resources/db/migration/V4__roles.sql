CREATE TABLE roles (
    id     BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(20) NOT NULL UNIQUE
);

INSERT INTO roles (nombre) VALUES ('ADMIN'), ('RECEPCIONISTA');

ALTER TABLE usuarios ADD COLUMN rol_id BIGINT REFERENCES roles(id);
UPDATE usuarios SET rol_id = r.id FROM roles r WHERE r.nombre = usuarios.rol;
ALTER TABLE usuarios ALTER COLUMN rol_id SET NOT NULL;
ALTER TABLE usuarios DROP COLUMN rol;
