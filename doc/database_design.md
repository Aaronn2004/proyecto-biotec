# Base de datos — Diseño

MariaDB 10.11 en el hosting Plesk de la UPV (base de datos `biotec`). Implementación: `src/database/esquema.sql`.

## Diseño del Componente (Component Design)

```text
====================================================================================
TABLE: medicion

DESCRIPTION: Guarda cada medida (O3, temperatura o ruido) que envía un nodo
sensor, con el instante en que la recibe el servidor.

COLUMNS:

+ dispositivo | VARCHAR(16)      | NOT NULL
+ tipo        | TINYINT UNSIGNED | NOT NULL
+ valor       | DOUBLE           | NOT NULL
+ fecha       | DATETIME(3)      | NOT NULL

PRIMARY KEY: (dispositivo, fecha)

FOREIGN KEYS:

+ (ninguna)

CONSTRAINTS:

+ CHECK (tipo IN (11, 12, 13))
+ CHECK (dispositivo <> '')
====================================================================================
```

## Aclaraciones del Diseño (Design Clarifications)

- `dispositivo`: UUID del iBeacon del nodo sensor, 16 caracteres ASCII (p. ej. `AARON-GTI-PBIO-1`).
- `tipo`: `TipoMedicion = { O3 = 11, TEMPERATURA = 12, RUIDO = 13 }`, los mismos valores que viajan en el byte alto del *major* del iBeacon.
- `valor`: O3 en ppm (0.235) y temperatura en °C (21.5, puede ser negativa).
- `fecha`: hora local de Madrid (`Europe/Madrid`) **con milisegundos**. La pone el servidor, porque el nodo no tiene reloj real.
- Clave primaria compuesta `(dispositivo, fecha)`: un mismo dispositivo no puede tener dos medidas en el mismo instante. Con precisión de 1 s chocaban medidas reales (O3 y temperatura del mismo nodo llegaron en el mismo segundo); con milisegundos el choque es prácticamente imposible. Si aun así ocurre, la lógica del negocio lo rechaza.
- Índice `idx_tipo_fecha (tipo, fecha)` (no es una restricción): acelera la consulta "la última medición de un tipo".
- No se guarda el `contador` del iBeacon: solo lo usa el móvil para no reenviar un anuncio repetido. Inconveniente asumido: desde la BD no se puede saber si se ha perdido alguna medida.
- No hay claves ajenas: en el Sprint 0 no existen tablas de usuarios ni de dispositivos.

## Reglas Generales (General Rules)

- Lenguaje: SQL de MariaDB/MySQL (`CREATE TABLE ... ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`).
- El fichero `.sql` empieza con una cabecera (nombre, descripción, diseño, autor, fecha, copyright); cada columna lleva un `COMMENT` con su significado.
- Si se añaden funciones o procedimientos SQL, cada uno lleva su diseño lógico en un comentario delimitado por `--------------------` (en el Sprint 0 no hay ninguno).
- Código claro y autoexplicativo: nombres de columna cortos y en castellano.
- Tests automáticos: la misma tabla (con sus `CHECK` y su clave primaria) se crea en SQLite en memoria en `src/business_logic/tests/miniTest.php`, y los tests de la lógica del negocio comprueban la clave primaria y las restricciones.
