CREATE DATABASE IF NOT EXISTS huellas
  CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE huellas;

CREATE TABLE IF NOT EXISTS clientes (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  dni VARCHAR(15) NOT NULL UNIQUE,
  telefono VARCHAR(30) NOT NULL,
  correo VARCHAR(150),
  activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS mascotas (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  cliente_id BIGINT UNSIGNED NOT NULL,
  nombre VARCHAR(80) NOT NULL,
  especie ENUM('PERRO', 'GATO') NOT NULL,
  raza VARCHAR(80),
  sexo ENUM('MACHO', 'HEMBRA') NOT NULL,
  fecha_nacimiento DATE,
  alergias TEXT,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_mascota_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
  INDEX ix_mascota_cliente (cliente_id, activo),
  INDEX ix_mascota_nombre (nombre)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS veterinarios (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  matricula VARCHAR(30) NOT NULL UNIQUE,
  activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS usuarios (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre_usuario VARCHAR(60) NOT NULL UNIQUE,
  clave_hash VARCHAR(255) NOT NULL,
  rol ENUM('RECEPCION', 'VETERINARIO') NOT NULL,
  veterinario_id BIGINT UNSIGNED NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_usuario_veterinario FOREIGN KEY (veterinario_id)
    REFERENCES veterinarios(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS turnos (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  mascota_id BIGINT UNSIGNED NOT NULL,
  veterinario_id BIGINT UNSIGNED NOT NULL,
  inicio DATETIME NOT NULL,
  motivo VARCHAR(250) NOT NULL,
  estado ENUM('CONFIRMADO', 'CANCELADO', 'ATENDIDO', 'AUSENTE')
    NOT NULL DEFAULT 'CONFIRMADO',
  motivo_cancelacion VARCHAR(250),
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  slot_ocupado TINYINT GENERATED ALWAYS AS (
    CASE WHEN estado IN ('CONFIRMADO', 'ATENDIDO', 'AUSENTE') THEN 1 ELSE NULL END
  ) STORED,
  CONSTRAINT fk_turno_mascota FOREIGN KEY (mascota_id) REFERENCES mascotas(id),
  CONSTRAINT fk_turno_veterinario FOREIGN KEY (veterinario_id) REFERENCES veterinarios(id),
  CONSTRAINT ck_turno_media_hora CHECK (MINUTE(inicio) IN (0, 30) AND SECOND(inicio) = 0),
  UNIQUE KEY uq_turno_veterinario_horario (veterinario_id, inicio, slot_ocupado),
  INDEX ix_turno_mascota_fecha (mascota_id, inicio),
  INDEX ix_turno_fecha_estado (inicio, estado)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS consultas (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  turno_id BIGINT UNSIGNED NOT NULL UNIQUE,
  motivo VARCHAR(250) NOT NULL,
  diagnostico TEXT NOT NULL,
  tratamiento TEXT NOT NULL,
  peso_kg DECIMAL(6,2) NOT NULL,
  atendida_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_consulta_turno FOREIGN KEY (turno_id) REFERENCES turnos(id),
  CONSTRAINT ck_peso_positivo CHECK (peso_kg > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS vacunas (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  consulta_id BIGINT UNSIGNED NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  aplicada_el DATE NOT NULL,
  refuerzo_el DATE,
  CONSTRAINT fk_vacuna_consulta FOREIGN KEY (consulta_id) REFERENCES consultas(id),
  INDEX ix_vacuna_refuerzo (refuerzo_el)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS recordatorios (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  turno_id BIGINT UNSIGNED NULL,
  vacuna_id BIGINT UNSIGNED NULL,
  tipo ENUM('TURNO', 'VACUNA') NOT NULL,
  canal ENUM('CORREO', 'SMS') NOT NULL,
  programado_para DATETIME NOT NULL,
  enviado_en DATETIME NULL,
  estado ENUM('PENDIENTE', 'ENVIADO', 'FALLIDO') NOT NULL DEFAULT 'PENDIENTE',
  detalle_error VARCHAR(250),
  CONSTRAINT fk_recordatorio_turno FOREIGN KEY (turno_id) REFERENCES turnos(id),
  CONSTRAINT fk_recordatorio_vacuna FOREIGN KEY (vacuna_id) REFERENCES vacunas(id),
  CONSTRAINT ck_recordatorio_origen CHECK (
    (tipo = 'TURNO' AND turno_id IS NOT NULL AND vacuna_id IS NULL)
    OR (tipo = 'VACUNA' AND vacuna_id IS NOT NULL AND turno_id IS NULL)
  ),
  INDEX ix_recordatorio_pendiente (estado, programado_para)
) ENGINE=InnoDB;
