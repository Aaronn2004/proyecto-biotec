# Lógica del negocio — Diseño

Implementación: `src/business_logic/Logica.php` (PHP 8). Es el **único** componente que accede a la base de datos: la tabla `medicion` de `database_design.md`.

## Diseño del Componente (Component Design)

Tipos:

```text
TipoMedicion  = { O3 = 11, TEMPERATURA = 12, RUIDO = 13 }

MedicionNueva = ( dispositivo: Text, tipo: N, valor: R )
Medicion      = ( dispositivo: Text, tipo: N, valor: R, fecha: Text )
                // fecha: ISO 8601 con milisegundos y diferencia con UTC, "2026-10-14T10:45:12.345+02:00"

ConexionBD    = conexión abierta con la base de datos de database_design.md
Reloj         = ahora() --> Text            // "2026-10-14 10:45:12.345", hora de Madrid
```

Errores del dominio (excepciones):

```text
DatosNoValidos         // la MedicionNueva no es válida
MedicionRepetida       // ya existe (dispositivo, fecha) en medicion
ErrorDeAlmacenamiento  // la base de datos ha fallado al guardar o leer
```

Relación con la base de datos (`database_design.md`):

```text
MedicionNueva.dispositivo --> medicion.dispositivo
MedicionNueva.tipo        --> medicion.tipo
MedicionNueva.valor       --> medicion.valor
reloj.ahora()             --> medicion.fecha        (DATETIME(3), hora de Madrid)
```

```text
                 --------- Logica --------------------------------------------------
                 |
                 | laBD: ConexionBD
                 | reloj: Reloj
                 | ZONA_HORARIA: Text = "Europe/Madrid"
                 | TIPOS_VALIDOS: [N] = [ 11, 12, 13 ]
                 | LONGITUD_MAXIMA_DISPOSITIVO: N = 16
                 |
                 | m: MedicionNueva --> comprobarMedicionNueva() --x   // lanza DatosNoValidos
                 |
                 | fechaBD: Text --> fechaIso() --x     // "2026-10-14 10:45:12.345" -> "2026-10-14T10:45:12.345+02:00"
                 |          Text <--
                 |
                 |
laBD: ConexionBD,
reloj: Reloj | null --> Logica() -->        // sin reloj: la hora actual de Madrid
                 |
                 |
m: MedicionNueva --> guardarMedicion() -->  // INSERT INTO medicion (dispositivo, tipo, valor, fecha)
      Medicion   <--                        // lanza DatosNoValidos, MedicionRepetida o ErrorDeAlmacenamiento
                 |
                 |
tipo: N | null --> leerUltimaMedicion() <-- // SELECT ... FROM medicion [WHERE tipo = ?] ORDER BY fecha DESC LIMIT 1
Medicion | null  <--                        // tipo null: la última de cualquier tipo; lanza ErrorDeAlmacenamiento
                 |
                 ---------------------------------------------------------------------
```

## Aclaraciones del Diseño (Design Clarifications)

- Una `MedicionNueva` es válida si:
  - `dispositivo` es un texto de 1 a 16 caracteres;
  - `tipo` es un `TipoMedicion` (11, 12 o 13), igual que el `CHECK` de la tabla;
  - `valor` es un número finito.
- La `fecha` no la manda el cliente: la pone `guardarMedicion()` con `reloj.ahora()` en hora local de Madrid. En verano es UTC+2 y en invierno UTC+1; cambia sola. Al devolverla se añade la diferencia con UTC para que no sea ambigua.
- Errores:
  - si se repite la clave primaria `(dispositivo, fecha)`, se lanza `MedicionRepetida`;
  - cualquier otro fallo de la BD se lanza como `ErrorDeAlmacenamiento`;
  - nunca sale un error propio de la base de datos.
- "La última" se decide por `fecha`. Inconveniente asumido: en la hora que se repite al pasar a invierno el orden podría salir cambiado.
- La lógica es independiente de cómo se acceda a ella: sus firmas solo usan tipos del dominio y sus errores son excepciones del dominio.
- Recibe la `ConexionBD` ya creada (inyección de dependencias), así que se usa igual con MariaDB en producción que con SQLite en memoria en los tests.
- El `reloj` solo se pasa en los tests, para fijar la hora y probar fechas y claves repetidas.
- Extensión de notación: `T | null` significa "un valor de tipo T o ninguno". En `leerUltimaMedicion()`, `tipo` es opcional.
- En PHP las excepciones se llaman `DatosNoValidosException`, `MedicionRepetidaException` y `ErrorDeAlmacenamientoException`. `ConexionBD` es un objeto `PDO`.

## Reglas Generales (General Rules)

- Lenguaje: PHP 8 con PDO.
- Cada función o método lleva su diseño lógico en un comentario delimitado por `--------------------`.
- Cada fichero empieza con una cabecera: nombre, descripción, diseño, autor, fecha y copyright.
- Código claro y autoexplicativo.
- `src/business_logic/` no importa ni llama a ningún otro componente del servidor: solo usa PDO para la base de datos.
- **Tests automáticos para todos los métodos públicos:** `php src/business_logic/tests/LogicaTest.php`.
  - Usan SQLite en memoria y una mini librería de tests (`tests/miniTest.php`), sin instalar nada.
  - Cada caso se describe con el nombre del `test('...')`; las funciones auxiliares llevan su diseño entre guiones.
  - `Funcion` = una función sin parámetros que se pasa a `test()`. `Valor` = cualquier valor comparable.
  - Casos probados:
    - guardar y leer;
    - filtro por tipo;
    - hora de Madrid en verano e invierno;
    - clave primaria repetida;
    - datos no válidos;
    - fallo de la base de datos.
