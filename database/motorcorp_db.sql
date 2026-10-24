-- =====================================================================
--  SISTEMA DE GESTIÓN MOTORCORP - Script de base de datos (PostgreSQL)
--  Base de datos: motorcorp_db

-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS comprobantes     CASCADE;
DROP TABLE IF EXISTS detalle_orden    CASCADE;
DROP TABLE IF EXISTS ordenes_servicio CASCADE;
DROP TABLE IF EXISTS productos        CASCADE;
DROP TABLE IF EXISTS vehiculos        CASCADE;
DROP TABLE IF EXISTS clientes         CASCADE;
DROP TABLE IF EXISTS usuarios         CASCADE;
DROP TABLE IF EXISTS roles            CASCADE;


-- ---------------------------------------------------------------------
-- 1. SEGURIDAD Y USUARIOS (RF11)
-- ---------------------------------------------------------------------
CREATE TABLE roles (
    id_rol      SERIAL       PRIMARY KEY,
    nombre_rol  VARCHAR(30)  NOT NULL UNIQUE
);

CREATE TABLE usuarios (
    id_usuario      SERIAL        PRIMARY KEY,
    nombres         VARCHAR(100)  NOT NULL,
    username        VARCHAR(30)   NOT NULL UNIQUE,
    password        VARCHAR(100)  NOT NULL,          -- "sal:hash" (SHA-256), nunca texto plano
    codigo_secreto  VARCHAR(20),
    id_rol          INTEGER       NOT NULL REFERENCES roles(id_rol)
);


-- ---------------------------------------------------------------------
-- 2. MÓDULO DE CLIENTES (RF01)
-- ---------------------------------------------------------------------
CREATE TABLE clientes (
    id_cliente  SERIAL        PRIMARY KEY,
    dni         CHAR(8)       NOT NULL UNIQUE
                CHECK (dni ~ '^[0-9]{8}$'),           -- exactamente 8 números
    nombres     VARCHAR(100)  NOT NULL,
    apellidos   VARCHAR(100)  NOT NULL,
    telefono    VARCHAR(15)
                CHECK (telefono ~ '^[0-9]{9}$'),      -- celular de 9 números
    correo      VARCHAR(100)
);

CREATE TABLE vehiculos (
    id_vehiculo  SERIAL       PRIMARY KEY,
    placa        VARCHAR(10)  NOT NULL UNIQUE,
    marca        VARCHAR(50)  NOT NULL,
    modelo       VARCHAR(50),
    id_cliente   INTEGER      NOT NULL REFERENCES clientes(id_cliente)
);


-- ---------------------------------------------------------------------
-- 3. MÓDULO DE INVENTARIO (RF05, RF06)
-- ---------------------------------------------------------------------
CREATE TABLE productos (
    id_producto      SERIAL         PRIMARY KEY,
    categoria        VARCHAR(20)    NOT NULL
                     CHECK (categoria IN ('Repuesto', 'Accesorio', 'Motocicleta')),
    nombre           VARCHAR(120)   NOT NULL,
    stock_actual     INTEGER        NOT NULL DEFAULT 0
                     CHECK (stock_actual >= 0),       -- el stock nunca es negativo
    precio_unitario  NUMERIC(10,2)  NOT NULL
                     CHECK (precio_unitario >= 0)
);


-- ---------------------------------------------------------------------
-- 4. MÓDULO DE COLA DE ESPERA Y TALLER (RF02, RF03, RF04, RF07, RF08)
-- ---------------------------------------------------------------------
CREATE TABLE ordenes_servicio (
    id_orden       SERIAL       PRIMARY KEY,
    id_vehiculo    INTEGER      NOT NULL REFERENCES vehiculos(id_vehiculo),
    id_mecanico    INTEGER      REFERENCES usuarios(id_usuario),  -- se asigna después
    diagnostico    TEXT,
    estado         VARCHAR(20)  NOT NULL DEFAULT 'En espera'
                   CHECK (estado IN ('En espera', 'En reparación', 'Finalizado', 'Entregado')),
    fecha_ingreso  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP   -- define el orden FIFO
);

CREATE TABLE detalle_orden (
    id_detalle   SERIAL         PRIMARY KEY,
    id_orden     INTEGER        NOT NULL REFERENCES ordenes_servicio(id_orden),
    id_producto  INTEGER        NOT NULL REFERENCES productos(id_producto),
    cantidad     INTEGER        NOT NULL CHECK (cantidad > 0),
    subtotal     NUMERIC(10,2)  NOT NULL CHECK (subtotal >= 0)
);


-- ---------------------------------------------------------------------
-- 5. MÓDULO DE VENTAS - POS (RF09)
-- ---------------------------------------------------------------------
CREATE TABLE comprobantes (
    id_comprobante  SERIAL         PRIMARY KEY,
    id_orden        INTEGER        NOT NULL REFERENCES ordenes_servicio(id_orden),
    id_cajero       INTEGER        NOT NULL REFERENCES usuarios(id_usuario),
    tipo            VARCHAR(10)    NOT NULL CHECK (tipo IN ('Boleta', 'Factura')),
    total_pagado    NUMERIC(10,2)  NOT NULL CHECK (total_pagado >= 0),
    fecha_emision   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- =====================================================================
--  DATOS DE PRUEBA
-- =====================================================================

-- Roles del sistema
INSERT INTO roles (nombre_rol) VALUES
    ('Administrador'),   -- id 1
    ('Recepcion'),       -- id 2
    ('Mecanico');        -- id 3

-- Usuarios (contraseñas cifradas con SHA-256 + sal)
--   admin     / Admin123
--   recepcion / Recep123
--   mecanico1 / Meca123
--   mecanico2 / Meca123
INSERT INTO usuarios (nombres, username, password, codigo_secreto, id_rol) VALUES
    ('Administrador General', 'admin',
     'V8ZjJOe98bBWyRuqiD6A3A==:0b23b1c8e3def3894c5a236ae61e7c158584773c3ef78e7d407d063e9e585ce9',
     'MOTOR2026', 1),
    ('Lucia Torres', 'recepcion',
     'AYATI3t7rpzCAJnG2slIbQ==:619e045f37812b6bb786a191c8e0c5b102ffec3c705124d2e8c08dc7974e51ac',
     NULL, 2),
    ('Pedro Quispe', 'mecanico1',
     'gXZj1GEKnX9mudhQ6JBckQ==:6008f770598588e700bf1b40859299eff43656a0fb0551465af5617fcafd6c4e',
     NULL, 3),
    ('Jorge Huaman', 'mecanico2',
     'gz9RvCLPd5Upg8oCfTFYXQ==:8d81b775e6b75a24c6daeaa211fd9390a6c2c916eafc9ede5df7a0437853fdb8',
     NULL, 3);

-- Clientes
INSERT INTO clientes (dni, nombres, apellidos, telefono, correo) VALUES
    ('12345678', 'Juan',   'Perez Rojas',    '987654321', 'juan.perez@gmail.com'),
    ('11223344', 'Mario',  'Martinez Soto',  '987654322', 'mario.martinez@gmail.com'),
    ('45678912', 'Carlos', 'Mendoza Rios',   '987111222', NULL),
    ('47852369', 'Andrea', 'Ramos Vilchez',  '945555666', 'andrea.ramos@gmail.com'),
    ('41236985', 'Rosa',   'Gomez Salazar',  '956333444', NULL);

-- Vehículos (cada uno pertenece a un cliente)
INSERT INTO vehiculos (placa, marca, modelo, id_cliente) VALUES
    ('MHS-123', 'Honda',  'CB 190R',     1),
    ('4521-AB', 'Yamaha', 'FZ 150',      2),
    ('7788-CD', 'Bajaj',  'Pulsar NS200', 3),
    ('3344-EF', 'Suzuki', 'Gixxer 150',  4),
    ('9911-GH', 'Honda',  'XR 150L',     1);

-- Productos del inventario (algunos con stock bajo para probar la alerta)
INSERT INTO productos (categoria, nombre, stock_actual, precio_unitario) VALUES
    ('Repuesto',    'Pastillas de freno delanteras', 20,   35.00),
    ('Repuesto',    'Aceite 20W-50 (1 L)',           40,   28.50),
    ('Repuesto',    'Filtro de aceite',               3,   18.00),
    ('Repuesto',    'Cadena de transmisión 428',      8,   65.00),
    ('Repuesto',    'Bujía NGK',                      2,   15.00),
    ('Repuesto',    'Llanta trasera 90/90-17',        6,  150.00),
    ('Accesorio',   'Casco integral',                10,  180.00),
    ('Accesorio',   'Guantes de motociclista',       15,   45.00),
    ('Motocicleta', 'Honda CB 190R 0 km',             2, 9500.00),
    ('Motocicleta', 'Yamaha FZ 150 0 km',             1, 8200.00);

-- Órdenes de servicio (cola de espera e historial)
INSERT INTO ordenes_servicio (id_vehiculo, id_mecanico, diagnostico, estado, fecha_ingreso) VALUES
    (1, 3,    'Cambio de aceite y filtro',        'Entregado',     CURRENT_TIMESTAMP - INTERVAL '3 days'),
    (2, 4,    'Ruido en frenos, revisar pastillas', 'Finalizado',  CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (3, 3,    'No enciende, revisar bujía',       'En reparación', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
    (4, NULL, 'Cadena suelta',                    'En espera',     CURRENT_TIMESTAMP - INTERVAL '1 hour'),
    (5, NULL, 'Mantenimiento general',            'En espera',     CURRENT_TIMESTAMP - INTERVAL '30 minutes');

-- Repuestos usados en cada orden
INSERT INTO detalle_orden (id_orden, id_producto, cantidad, subtotal) VALUES
    (1, 2, 1, 28.50),   -- orden 1: 1 aceite
    (1, 3, 1, 18.00),   -- orden 1: 1 filtro
    (2, 1, 1, 35.00);   -- orden 2: 1 juego de pastillas

-- Comprobante de la orden ya entregada
INSERT INTO comprobantes (id_orden, id_cajero, tipo, total_pagado, fecha_emision) VALUES
    (1, 2, 'Boleta', 46.50, CURRENT_TIMESTAMP - INTERVAL '3 days');


-- =====================================================================
--  VERIFICACIÓN: debe mostrar las 8 tablas con su cantidad de registros
-- =====================================================================
SELECT 'roles' AS tabla, COUNT(*) AS registros FROM roles
UNION ALL SELECT 'usuarios',         COUNT(*) FROM usuarios
UNION ALL SELECT 'clientes',         COUNT(*) FROM clientes
UNION ALL SELECT 'vehiculos',        COUNT(*) FROM vehiculos
UNION ALL SELECT 'productos',        COUNT(*) FROM productos
UNION ALL SELECT 'ordenes_servicio', COUNT(*) FROM ordenes_servicio
UNION ALL SELECT 'detalle_orden',    COUNT(*) FROM detalle_orden
UNION ALL SELECT 'comprobantes',     COUNT(*) FROM comprobantes;
