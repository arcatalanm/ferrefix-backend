--liquibase formatted sql

--changeset ferrefix:001-create-usuarios
CREATE TABLE usuarios (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    seg_nombre VARCHAR(120),
    apellido_paterno VARCHAR(120),
    apellido_materno VARCHAR(120),
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    rut VARCHAR(12),
    telefono VARCHAR(20),
    direccion VARCHAR(250),
    region VARCHAR(120),
    comuna VARCHAR(120),
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT uk_usuarios_rut UNIQUE (rut)
);
--rollback DROP TABLE usuarios;

