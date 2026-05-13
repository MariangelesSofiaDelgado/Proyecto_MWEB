DROP DATABASE IF EXISTS yaku_sabores;
CREATE DATABASE yaku_sabores;
USE yaku_sabores;

-- ==========================================
-- 1. CREACIÓN DE TABLAS
-- ==========================================

CREATE TABLE roles (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE usuarios (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  email VARCHAR(120) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  rol_id INT NOT NULL,
  activo BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (rol_id) REFERENCES roles(id)
);

CREATE TABLE mesas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(10) UNIQUE NOT NULL,
  ubicacion ENUM('interior','exterior') NOT NULL,
  estado ENUM('libre','ocupada','reservada','fuera_servicio') DEFAULT 'libre'
);

CREATE TABLE categorias (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) UNIQUE NOT NULL
);

CREATE TABLE productos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(120) NOT NULL,
  descripcion TEXT,
  precio DECIMAL(10,2) NOT NULL,
  disponible BOOLEAN DEFAULT TRUE,
  categoria_id INT NOT NULL,
  FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);

-- TABLA ACTUALIZADA: Soporta Presencial y Delivery
CREATE TABLE pedidos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  mesa_id INT NULL, -- Permite NULL porque un delivery no usa mesa
  mesero_id INT NULL,
  tipo ENUM('presencial', 'delivery') NOT NULL DEFAULT 'presencial',
  direccion_delivery VARCHAR(255) NULL,
  estado ENUM('nuevo','en_preparacion','listo','entregado','facturado','cancelado') DEFAULT 'nuevo',
  total DECIMAL(10,2) DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (mesa_id) REFERENCES mesas(id),
  FOREIGN KEY (mesero_id) REFERENCES usuarios(id)
);

CREATE TABLE pedido_detalle (
  id INT AUTO_INCREMENT PRIMARY KEY,
  pedido_id INT NOT NULL,
  producto_id INT NOT NULL,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  notas VARCHAR(255),
  FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
  FOREIGN KEY (producto_id) REFERENCES productos(id)
);

CREATE TABLE facturas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  pedido_id INT NOT NULL,
  tipo ENUM('boleta','factura') NOT NULL,
  ruc VARCHAR(20),
  razon_social VARCHAR(150),
  total DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
);

-- ==========================================
-- 2. INSERCIÓN DE DATOS MAESTROS Y PRUEBA
-- ==========================================

-- ROLES
INSERT INTO roles (id, nombre) VALUES 
(1, 'Administrador'), (2, 'Cocinero'), (3, 'Mesero'), (4, 'Cliente');

-- USUARIOS (Passwords: hash123, hash456, hash789, hash321)
INSERT INTO usuarios (id, nombre, email, password_hash, rol_id, activo) VALUES
(1, 'Juan Pérez', 'juan.perez@mail.com', 'hash123', 1, TRUE),
(2, 'María López', 'maria.lopez@mail.com', 'hash456', 2, TRUE),
(3, 'Carlos Ramírez', 'carlos.ramirez@mail.com', 'hash789', 3, TRUE),
(4, 'Ana Torres', 'ana.torres@mail.com', 'hash321', 4, TRUE);

-- MESAS
INSERT INTO mesas (codigo, ubicacion, estado) VALUES
('M01', 'interior', 'libre'), ('M02', 'interior', 'ocupada'),
('M03', 'exterior', 'reservada'), ('M04', 'exterior', 'fuera_servicio');

-- CATEGORÍAS
INSERT INTO categorias (id, nombre) VALUES
(1, 'Piqueos & Entradas'), (2, 'Sándwiches'), (3, 'Platos de Fondo'),
(4, 'Postres'), (5, 'Bebidas');

-- PRODUCTOS MARINOS
INSERT INTO productos (nombre, descripcion, precio, disponible, categoria_id) VALUES
-- Piqueos (Cat 1)
('Ceviche Clásico de Pescado', 'Pesca del día con limón de Chulucanas, ají limo y camote glaseado.', 35.00, TRUE, 1),
('Ceviche Carretillero', 'Ceviche mixto con chicharrón de pota súper crocante.', 42.00, TRUE, 1),
('Leche de Tigre', 'El extracto de nuestro ceviche servido en copa con chicharrón.', 18.00, TRUE, 1),
-- Sándwiches (Cat 2)
('Pan con Pejerrey Arrebozado', 'Pan francés con pejerrey arrebozado, lechuga y sarsa criolla.', 16.00, TRUE, 2),
('Pan con Chicharrón de Pescado', 'Trozos de pesca del día fritos al panko con salsa tártara.', 18.00, TRUE, 2),
-- Platos de Fondo (Cat 3)
('Arroz con Mariscos', 'Arroz graneado al wok con base de ají panca y mixtura de mariscos.', 45.00, TRUE, 3),
('Jalea Mixta', 'Montaña de mariscos y pescado frito sobre yucas fritas.', 55.00, TRUE, 3),
('Chupe de Camarones', 'Cremoso caldo con coral de camarón, queso fresco y huevo escalfado.', 52.00, TRUE, 3),
-- Postres (Cat 4)
('Suspiro a la Limeña', 'Clásico manjar blanco coronado con merengue al oporto.', 15.00, TRUE, 4),
('Picarones (Porción)', 'Aros crujientes de zapallo y camote bañados en miel de higo.', 14.00, TRUE, 4),
-- Bebidas (Cat 5)
('Chicha Morada (Jarra 1L)', 'Nuestra chicha tradicional hervida con piña y especias.', 18.00, TRUE, 5),
('Inka Cola (1 Litro)', 'La bebida de sabor nacional.', 12.00, TRUE, 5),
('Limonada Frozen (Jarra 1L)', 'Refrescante limonada licuada con hielo.', 15.00, TRUE, 5);

-- PEDIDOS DE PRUEBA
-- Pedido 1: Presencial en Mesa 1
INSERT INTO pedidos (mesa_id, mesero_id, tipo, direccion_delivery, estado, total) VALUES
(1, 3, 'presencial', NULL, 'nuevo', 35.00);

-- Pedido 2: Delivery
INSERT INTO pedidos (mesa_id, mesero_id, tipo, direccion_delivery, estado, total) VALUES
(NULL, NULL, 'delivery', 'Av. La Marina 456, Callao', 'en_preparacion', 46.00);

-- DETALLE DE PEDIDOS
INSERT INTO pedido_detalle (pedido_id, producto_id, cantidad, precio_unitario, notas) VALUES
(1, 1, 1, 35.00, 'Sin cebolla'),
(2, 4, 1, 16.00, 'Sarsa criolla aparte'),
(2, 11, 1, 15.00, NULL),
(2, 12, 1, 15.00, 'Helada');

-- FACTURAS (De prueba)
INSERT INTO facturas (pedido_id, tipo, ruc, razon_social, total) VALUES
(1, 'boleta', NULL, 'Cliente Final', 35.00),
(2, 'factura', '20123456789', 'Empresa Naviera SAC', 46.00);
