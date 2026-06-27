CREATE EXTENSION IF NOT EXISTS pgcrypto;
INSERT INTO usuarios (nombre, email, password_hash, rol, activo)
VALUES ('Admin', 'admin@mail.com', crypt('admin', gen_salt('bf')), 'ADMIN', TRUE);
