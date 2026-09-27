USE huellas;

-- Datos de ejemplo. Los identificadores se obtienen de cada inserción.
INSERT INTO clientes (nombre, dni, telefono, correo)
VALUES ('Cliente de prueba', '00000000', '0000000000', 'prueba@example.invalid');
SET @cliente := LAST_INSERT_ID();

INSERT INTO mascotas (cliente_id, nombre, especie, raza, sexo, fecha_nacimiento, alergias)
VALUES (@cliente, 'Mora', 'PERRO', 'Mestiza', 'HEMBRA', '2021-05-12', 'Penicilina');
SET @mascota := LAST_INSERT_ID();

INSERT INTO veterinarios (nombre, matricula)
VALUES ('Veterinaria de prueba', 'MAT-DEMO-01');
SET @veterinaria := LAST_INSERT_ID();

INSERT INTO turnos (mascota_id, veterinario_id, inicio, motivo)
VALUES (@mascota, @veterinaria,
        CONCAT(DATE_SUB(CURRENT_DATE(), INTERVAL 7 DAY), ' 09:00:00'),
        'Control anual');
SET @turno_pasado := LAST_INSERT_ID();

INSERT INTO consultas (turno_id, motivo, diagnostico, tratamiento, peso_kg)
VALUES (@turno_pasado, 'Control anual', 'Paciente en buen estado general',
        'Continuar controles periódicos', 12.40);
SET @consulta := LAST_INSERT_ID();
UPDATE turnos SET estado = 'ATENDIDO' WHERE id = @turno_pasado;

INSERT INTO vacunas (consulta_id, nombre, aplicada_el, refuerzo_el)
VALUES (@consulta, 'Antirrábica', DATE_SUB(CURRENT_DATE(), INTERVAL 7 DAY),
        DATE_ADD(CURRENT_DATE(), INTERVAL 20 DAY));

INSERT INTO turnos (mascota_id, veterinario_id, inicio, motivo)
VALUES (@mascota, @veterinaria,
        CONCAT(DATE_ADD(CURRENT_DATE(), INTERVAL 7 DAY), ' 10:00:00'),
        'Revisión de tratamiento');
SET @turno_proximo := LAST_INSERT_ID();

-- Listado diario con nombre de paciente, responsable y profesional.
SELECT t.id, t.inicio, t.estado, m.nombre AS mascota,
       c.nombre AS cliente, v.nombre AS veterinario, t.motivo
FROM turnos t
JOIN mascotas m ON m.id = t.mascota_id
JOIN clientes c ON c.id = m.cliente_id
JOIN veterinarios v ON v.id = t.veterinario_id
WHERE t.inicio >= DATE_ADD(CURRENT_DATE(), INTERVAL 7 DAY)
  AND t.inicio < DATE_ADD(CURRENT_DATE(), INTERVAL 8 DAY)
ORDER BY t.inicio;

-- Consulta del historial clínico de Mora.
SELECT m.nombre AS mascota, t.inicio, v.nombre AS veterinario,
       co.motivo, co.diagnostico, co.tratamiento, co.peso_kg
FROM consultas co
JOIN turnos t ON t.id = co.turno_id
JOIN mascotas m ON m.id = t.mascota_id
JOIN veterinarios v ON v.id = t.veterinario_id
WHERE m.id = @mascota
ORDER BY t.inicio DESC;

-- Vacunas con refuerzo en los próximos treinta días.
SELECT m.nombre AS mascota, va.nombre AS vacuna, va.refuerzo_el,
       c.nombre AS cliente, c.telefono, c.correo
FROM vacunas va
JOIN consultas co ON co.id = va.consulta_id
JOIN turnos t ON t.id = co.turno_id
JOIN mascotas m ON m.id = t.mascota_id
JOIN clientes c ON c.id = m.cliente_id
WHERE va.refuerzo_el BETWEEN CURRENT_DATE() AND DATE_ADD(CURRENT_DATE(), INTERVAL 30 DAY)
ORDER BY va.refuerzo_el;

-- Ejemplo de baja lógica: la historia permanece disponible.
UPDATE mascotas SET activo = FALSE WHERE id = @mascota;
SELECT id, nombre, activo FROM mascotas WHERE id = @mascota;

-- Borrado de un registro de prueba independiente del historial.
INSERT INTO clientes (nombre, dni, telefono)
VALUES ('Registro de prueba', '99999999', '000');
SET @cliente_prueba := LAST_INSERT_ID();
DELETE FROM clientes WHERE id = @cliente_prueba;
SELECT COUNT(*) AS registros_restantes
FROM clientes WHERE id = @cliente_prueba;
