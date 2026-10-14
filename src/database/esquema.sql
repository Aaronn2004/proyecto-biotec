-- ==============================================================
-- Fichero:     esquema.sql  (MariaDB / MySQL - Plesk)
-- Descripción: Crea la tabla medicion. Se ejecuta UNA vez desde
--              phpMyAdmin (pestaña "SQL").
--              OJO: el DROP borra la tabla anterior y todas sus filas.
-- Diseño:      doc/database_design.md
-- Autor:       Aaron Blasco Blay
-- Fecha:       14/10/2026
-- Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
-- ==============================================================
DROP TABLE IF EXISTS medicion;

CREATE TABLE medicion (
  dispositivo  VARCHAR(16)      NOT NULL COMMENT 'UUID del iBeacon, p. ej. AARON-GTI-PBIO-1',
  tipo         TINYINT UNSIGNED NOT NULL COMMENT '11 = O3, 12 = temperatura, 13 = ruido',
  valor        DOUBLE           NOT NULL COMMENT 'O3 en ppm, temperatura en grados C',
  fecha        DATETIME(3)      NOT NULL COMMENT 'hora local de Madrid con milisegundos, la pone el servidor',
  PRIMARY KEY (dispositivo, fecha),
  KEY idx_tipo_fecha (tipo, fecha),
  CONSTRAINT tipo_valido CHECK (tipo IN (11, 12, 13)),
  CONSTRAINT dispositivo_no_vacio CHECK (dispositivo <> '')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
