-- ==============================================================
-- esquema.sql  (MariaDB / MySQL - Plesk)
-- Se ejecuta UNA vez desde phpMyAdmin (pestaña "SQL").
-- OJO: el DROP borra la tabla anterior y todas sus filas.
-- ==============================================================
DROP TABLE IF EXISTS medicion;

CREATE TABLE medicion (
  dispositivo  VARCHAR(16)      NOT NULL COMMENT 'UUID del iBeacon, p. ej. AARON-GTI-PBIO-1',
  tipo         TINYINT UNSIGNED NOT NULL COMMENT '11 = O3, 12 = temperatura, 13 = ruido',
  valor        DOUBLE           NOT NULL COMMENT 'O3 en ppm, temperatura en grados C',
  fecha        DATETIME(3)      NOT NULL COMMENT 'hora local de Madrid con milisegundos, la pone el servidor',
  PRIMARY KEY (dispositivo, fecha),
  KEY idx_tipo_fecha (tipo, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
