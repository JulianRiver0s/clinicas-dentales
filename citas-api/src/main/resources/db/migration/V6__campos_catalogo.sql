-- Atributos del catálogo que faltaban en la migración inicial.
ALTER TABLE clinicas       ADD COLUMN ciudad      VARCHAR(120);
ALTER TABLE clinicas       ADD COLUMN telefono    VARCHAR(20);
ALTER TABLE procedimientos ADD COLUMN descripcion VARCHAR(300);
