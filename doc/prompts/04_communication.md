# Prompt 04 — Comunicación (API REST)

Adjuntar: `00_contexto_comun.md` + `doc/communication_design.md` + `doc/business_logic_design.md`.

```text
Implementa src/communication/api.php (PHP 8, sin frameworks) según communication_design.md.
Solo traduce HTTP <-> lógica del negocio: require de ../business_logic/Logica.php y ni una línea de SQL.

Requisitos:
- Rutas, cuerpos y códigos EXACTAMENTE como la tabla del diseño:
  - POST /medicion: 201, 400 o 409;
  - GET /medicion/ultima[?tipo=N]: 200, 404 o 400;
  - 500 si falla la BD, también al leer: todos los errores en JSON;
  - DatosNoValidosException -> 400, MedicionRepetidaException -> 409, ErrorDeAlmacenamientoException -> 500;
  - 404 si la ruta no existe.
- Errores con el formato {"error": "..."}.
- JSON con JSON_PRESERVE_ZERO_FRACTION.
- Conexión: config.php (fuera de Git: crea config.example.php) o la variable de entorno PBIO_DSN para los tests.
- El Plesk de la UPV NO rellena PATH_INFO: saca la ruta de REQUEST_URI (lo que va tras "api.php"),
  y acepta también ?ruta=.
- Cabeceras CORS. OPTIONS responde 204.

Tests:
- src/communication/tests/ApiTest.php arranca "php -S" sobre src/ con una BD SQLite temporal
  (PBIO_DSN) y hace peticiones HTTP reales.
- Casos: 404 sin datos, 201, 400 (datos y JSON malos), filtro por tipo, tipo inexistente, ?ruta=, ruta desconocida.
- Comando: php src/communication/tests/ApiTest.php
```
