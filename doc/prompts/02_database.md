# Prompt 02 — Base de datos

Adjuntar: `00_contexto_comun.md` + `doc/database_design.md`.

```text
Genera src/database/esquema.sql para MariaDB 10.11 (Plesk), a partir de la tabla "medicion"
del diseño adjunto (formato TABLE / COLUMNS / PRIMARY KEY / FOREIGN KEYS / CONSTRAINTS).

Requisitos:
- DROP TABLE IF EXISTS + CREATE TABLE (se ejecuta una vez desde la pestaña SQL de phpMyAdmin).
- Columnas exactamente como en el diseño:
  - dispositivo VARCHAR(16);
  - tipo TINYINT UNSIGNED;
  - valor DOUBLE;
  - fecha DATETIME(3).
- Un COMMENT por columna.
- PRIMARY KEY (dispositivo, fecha) compuesta.
- CHECK (tipo IN (11,12,13)) y CHECK (dispositivo <> '').
- Índice idx_tipo_fecha.
- ENGINE=InnoDB, utf8mb4.
- Cabecera de fichero con comentarios "--".
```
