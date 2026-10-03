-- ==============================================================
-- Esquema de la base de datos (SQLite) - Sprint 0
-- ==============================================================
CREATE TABLE IF NOT EXISTS medicion (
  id        INTEGER PRIMARY KEY AUTOINCREMENT,
  tipo      INTEGER NOT NULL,   -- 11 = O3, 12 = temperatura, 13 = ruido
  valor     REAL    NOT NULL,   -- O3 en ppm
  contador  INTEGER NOT NULL,   -- nº de muestra emitido por la placa (0..255)
  fecha     TEXT    NOT NULL    -- ISO 8601 (UTC), la pone el servidor
);
