--liquibase formatted sql

--changeset ferrefix:001-create-maquinas-arriendos
CREATE TABLE IF NOT EXISTS maquinas (
    id VARCHAR(40) NOT NULL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    marca VARCHAR(120) NOT NULL,
    categoria VARCHAR(120) NOT NULL,
    tarifa_dia DECIMAL(12,0) NOT NULL,
    imagen VARCHAR(500),
    incluye VARCHAR(500),
    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    CONSTRAINT ck_maquina_tarifa CHECK (tarifa_dia > 0),
    CONSTRAINT ck_maquina_estado CHECK (estado IN ('DISPONIBLE', 'MANTENCION'))
);

CREATE TABLE IF NOT EXISTS maquina_especificaciones (
    id_maquina VARCHAR(40) NOT NULL,
    atributo VARCHAR(80) NOT NULL,
    valor VARCHAR(200) NOT NULL,
    PRIMARY KEY (id_maquina, atributo),
    CONSTRAINT fk_especificacion_maquina FOREIGN KEY (id_maquina) REFERENCES maquinas(id)
);

CREATE TABLE IF NOT EXISTS arriendos (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    id_maquina VARCHAR(40) NOT NULL,
    id_usuario VARCHAR(36) NOT NULL,
    nombre_usuario VARCHAR(500) NOT NULL,
    rut VARCHAR(12) NOT NULL,
    email VARCHAR(254) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    direccion VARCHAR(250) NOT NULL,
    region VARCHAR(120) NOT NULL,
    comuna VARCHAR(120) NOT NULL,
    fecha_contrato TIMESTAMP(6) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_termino DATE NOT NULL,
    tarifa_dia DECIMAL(12,0) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'VIGENTE',
    CONSTRAINT fk_arriendo_maquina FOREIGN KEY (id_maquina) REFERENCES maquinas(id),
    CONSTRAINT ck_arriendo_fechas CHECK (fecha_termino >= fecha_inicio AND DATEDIFF(fecha_termino, fecha_inicio) < 90),
    CONSTRAINT ck_arriendo_tarifa CHECK (tarifa_dia > 0),
    CONSTRAINT ck_arriendo_estado CHECK (estado IN ('VIGENTE', 'FINALIZADO')),
    INDEX idx_arriendo_usuario_fecha (id_usuario, fecha_contrato),
    INDEX idx_arriendo_maquina_estado (id_maquina, estado, fecha_termino),
    INDEX idx_arriendo_vencimiento (estado, fecha_termino)
);
--rollback DROP TABLE arriendos; DROP TABLE maquina_especificaciones; DROP TABLE maquinas;

--changeset ferrefix:002-seed-maquinas
--preconditions onFail:MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM maquinas
INSERT INTO maquinas (id, nombre, marca, categoria, tarifa_dia, imagen, incluye, estado) VALUES ('MAQ-BOSCH-01', 'Martillo Demoledor Bosch GSH 16-28 Professional 1750W', 'Bosch', 'Demolición y Perforación Pesada', 28990, '/media/maquinarias/arriendo-bosch.webp', 'Cincel de punta, cincel plano, paño de limpieza y maletín de transporte con ruedas', 'DISPONIBLE');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-BOSCH-01', 'Potencia Absorbida', '1.750 Watts');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-BOSCH-01', 'Energía de Impacto', '41 Joules');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-BOSCH-01', 'Portaherramientas', 'Hexagonal 28 mm');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-BOSCH-01', 'Peso', '17.9 kg');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-BOSCH-01', 'Rendimiento de arranque', '13 toneladas de hormigón al día');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-BOSCH-01', 'Tecnología', 'Sistema Vibration Control para reducción de fatiga en faena');
INSERT INTO maquinas (id, nombre, marca, categoria, tarifa_dia, imagen, incluye, estado) VALUES ('MAQ-BET-02', 'Betonera Trompo Eléctrica 150 Litros 0.75HP', 'Ingco Heavy', 'Hormigón y Mezcla', 18990, '/media/maquinarias/arriendo-ingco.webp', 'Cable con enchufe industrial Legrand y manubrio de volteo reforzado', 'DISPONIBLE');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-BET-02', 'Capacidad de Tambor', '150 Litros');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-BET-02', 'Motor', '0.75 HP Eléctrico 220V');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-BET-02', 'Ruedas', 'Goma maciza 10" para fácil traslado en obra');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-BET-02', 'Espesor Tambor', 'Chapa de acero 1.8 mm');
INSERT INTO maquinas (id, nombre, marca, categoria, tarifa_dia, imagen, incluye, estado) VALUES ('MAQ-GEN-03', 'Generador Eléctrico a Gasolina 6.5kVA Monofásico', 'Krafter Pro', 'Energía y Grupos Electrógenos', 24990, '/media/maquinarias/arriendo-krafter.webp', 'Kit de ruedas de transporte y horómetro digital', 'DISPONIBLE');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-GEN-03', 'Potencia Nominal', '5.500 Watts (Peak 6.500W)');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-GEN-03', 'Voltaje', '230V / 50Hz');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-GEN-03', 'Autonomía', '9.5 horas al 75% de carga');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-GEN-03', 'Capacidad Estanque', '25 Litros Gasolina 93');
INSERT INTO maquinas (id, nombre, marca, categoria, tarifa_dia, imagen, incluye, estado) VALUES ('MAQ-PLAC-04', 'Placa Compactadora Vibratoria 90kg Motor 6.5HP', 'Husqvarna Construction', 'Compactación y Pavimento', 22990, '/media/maquinarias/arriendo-husqvarna.webp', 'Tanque de agua para asfalto y ruedas rebatibles', 'DISPONIBLE');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-PLAC-04', 'Peso Operativo', '90 kg');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-PLAC-04', 'Fuerza Centrífuga', '15 kN');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-PLAC-04', 'Profundidad de Compactación', 'Hasta 30 cm');
INSERT INTO maquina_especificaciones (id_maquina, atributo, valor) VALUES ('MAQ-PLAC-04', 'Motor', '4 Tiempos 196cc OHV');
--rollback DELETE FROM maquina_especificaciones WHERE id_maquina IN ('MAQ-BOSCH-01', 'MAQ-BET-02', 'MAQ-GEN-03', 'MAQ-PLAC-04'); DELETE FROM maquinas WHERE id IN ('MAQ-BOSCH-01', 'MAQ-BET-02', 'MAQ-GEN-03', 'MAQ-PLAC-04');

--changeset ferrefix:003-descripcion-detalle
ALTER TABLE maquinas ADD COLUMN descripcion_detalle LONGTEXT NULL;
SET SESSION group_concat_max_len = 1000000;
UPDATE maquinas m
LEFT JOIN (
    SELECT id_maquina, GROUP_CONCAT(CONCAT('<li><strong>',
        REPLACE(REPLACE(REPLACE(atributo, '&', '&amp;'), '<', '&lt;'), '>', '&gt;'),
        ':</strong> ',
        REPLACE(REPLACE(REPLACE(valor, '&', '&amp;'), '<', '&lt;'), '>', '&gt;'),
        '</li>') ORDER BY atributo SEPARATOR '') AS items
    FROM maquina_especificaciones GROUP BY id_maquina
) e ON e.id_maquina = m.id
SET m.descripcion_detalle = IF(e.items IS NULL, '', CONCAT('<h3>Ficha técnica</h3><ul>', e.items, '</ul>'));
ALTER TABLE maquinas MODIFY descripcion_detalle LONGTEXT NOT NULL;
DROP TABLE maquina_especificaciones;
--rollback SELECT 1;

