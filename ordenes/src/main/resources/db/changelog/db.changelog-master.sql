--liquibase formatted sql

--changeset ferrefix:001-create-maquinas-arriendos
CREATE TABLE maquinas (
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

CREATE TABLE maquina_especificaciones (
    id_maquina VARCHAR(40) NOT NULL,
    atributo VARCHAR(80) NOT NULL,
    valor VARCHAR(200) NOT NULL,
    PRIMARY KEY (id_maquina, atributo),
    CONSTRAINT fk_especificacion_maquina FOREIGN KEY (id_maquina) REFERENCES maquinas(id)
);

CREATE TABLE arriendos (
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


--changeset ferrefix:005-pagos-webpay
CREATE TABLE pagos_webpay (
    id_pedido VARCHAR(36) NOT NULL PRIMARY KEY,
    buy_order VARCHAR(26) NOT NULL,
    token VARCHAR(64) NOT NULL,
    session_id VARCHAR(61) NOT NULL,
    monto DECIMAL(16,0) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    codigo_respuesta INT,
    codigo_autorizacion VARCHAR(10),
    tipo_pago VARCHAR(5),
    cuotas INT,
    tarjeta_ultimos4 VARCHAR(19),
    fecha_transaccion TIMESTAMP(6) NULL,
    fecha_creacion TIMESTAMP(6) NOT NULL,
    fecha_actualizacion TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_pago_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id),
    CONSTRAINT uq_pago_buy_order UNIQUE (buy_order),
    CONSTRAINT uq_pago_token UNIQUE (token),
    CONSTRAINT ck_pago_estado CHECK (estado IN ('INICIADO','AUTORIZADO','RECHAZADO','ABORTADO','EXPIRADO')),
    INDEX idx_pago_estado_fecha (estado, fecha_creacion)
);
--rollback DROP TABLE pagos_webpay;

--changeset ferrefix:002-seed-maquinas
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

--changeset ferrefix:003-carritos-pedidos
CREATE TABLE carritos (
    propietario VARCHAR(80) NOT NULL PRIMARY KEY
);
CREATE TABLE carrito_items (
    propietario VARCHAR(80) NOT NULL,
    id_producto VARCHAR(36) NOT NULL,
    cantidad INT NOT NULL,
    PRIMARY KEY (propietario, id_producto),
    CONSTRAINT fk_carrito_item FOREIGN KEY (propietario) REFERENCES carritos(propietario),
    CONSTRAINT ck_carrito_cantidad CHECK (cantidad BETWEEN 1 AND 999)
);
CREATE TABLE pedidos (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    propietario VARCHAR(80) NOT NULL,
    id_usuario VARCHAR(36),
    fecha TIMESTAMP(6) NOT NULL,
    nombre VARCHAR(500) NOT NULL,
    rut VARCHAR(12) NOT NULL,
    email VARCHAR(254) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    direccion VARCHAR(250) NOT NULL,
    region VARCHAR(120) NOT NULL,
    comuna VARCHAR(120) NOT NULL,
    subtotal DECIMAL(16,0) NOT NULL,
    despacho DECIMAL(16,0) NOT NULL,
    total DECIMAL(16,0) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    metodo_pago VARCHAR(60) NOT NULL,
    metodo_entrega VARCHAR(20) NOT NULL,
    indicaciones VARCHAR(500),
    INDEX idx_pedido_usuario_fecha (id_usuario, fecha),
    CONSTRAINT ck_pedido_total CHECK (total = subtotal + despacho AND subtotal >= 0 AND despacho >= 0)
);
CREATE TABLE pedido_items (
    id_pedido VARCHAR(36) NOT NULL,
    id_producto VARCHAR(36) NOT NULL,
    nombre VARCHAR(160) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12,0) NOT NULL,
    PRIMARY KEY (id_pedido, id_producto),
    CONSTRAINT fk_pedido_item FOREIGN KEY (id_pedido) REFERENCES pedidos(id),
    CONSTRAINT ck_pedido_cantidad CHECK (cantidad BETWEEN 1 AND 999),
    CONSTRAINT ck_pedido_precio CHECK (precio_unitario >= 0)
);
--rollback DROP TABLE pedido_items; DROP TABLE pedidos; DROP TABLE carrito_items; DROP TABLE carritos;

--changeset ferrefix:004-cotizaciones
CREATE TABLE cotizaciones (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    id_usuario VARCHAR(36),
    fecha TIMESTAMP(6) NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    rut VARCHAR(12) NOT NULL,
    email VARCHAR(254) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    tipo_proyecto VARCHAR(120) NOT NULL,
    region VARCHAR(120) NOT NULL,
    detalle VARCHAR(4000) NOT NULL,
    despacho_obra BOOLEAN NOT NULL
);
--rollback DROP TABLE cotizaciones;
