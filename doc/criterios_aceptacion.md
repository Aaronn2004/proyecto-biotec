# Criterios de aceptación — Sprint 0

Cada criterio es reproducible siguiendo el README. Los marcados **(auto)** son tests automáticos.

| # | Entregable | Criterio | Cómo se comprueba |
|---|------------|----------|-------------------|
| 1 | Arduino | La placa emite, alternando cada 2 s, un iBeacon de O3 (major `(11<<8)+contador`, minor = `VALOR_O3_FICTICIO`) y otro de temperatura (major `(12<<8)+contador`, minor = `VALOR_TEMPERATURA_FICTICIO`), con UUID `AARON-GTI-PBIO-1` | Monitor serie (115200) + app *nRF Connect* en el móvil |
| 1b | Arduino | **(auto)** Codificación de la trama: UUID, major, minor (con signo) y fabricante 0x004C | `src/arduino`: `g++ -std=c++17 -I tests/mocks -I HolaMundoIBeacon tests/NodoSensorTest.cpp -o tests/nodo && tests/nodo` → `# fail 0` |
| 2 | Android | "Buscar dispositivos" lista dispositivos BLE en Logcat; "Detener" para el escaneo | Logcat, filtro `>>>>` |
| 3 | Android | "Buscar nuestro dispositivo" detecta solo nuestro beacon y hace **un** POST por medida nueva (por tipo y contador), no por anuncio | Logcat: `¡Nuestro beacon!` y `POST OK` una vez cada ~2 s |
| 3b | Android | La pantalla muestra estado del Bluetooth, O3, temperatura, contador, RSSI, major/minor y resultado del último envío | Con la placa encendida; sin internet → "Servidor: ERROR ..." y al volver la red se recupera |
| 4 | Android | **(auto)** Decodificación de tramas (temperatura con signo), `MedicionNueva`/`Medicion` ⇄ JSON, `LogicaFake.guardarMedicion()` (201, 400, sin red), filtro de duplicados y estado de pantalla | Tests JUnit en verde (19): `TramaIBeaconTest`, `LogicaFakeTest`, `FiltroDuplicadosTest`, `EstadoNodoTest` |
| 5 | Lógica del negocio | **(auto)** Guarda y lee de la BD (dispositivo, tipo, valor, fecha en hora de Madrid con ms), filtra por tipo, rechaza datos no válidos y (dispositivo, fecha) repetidos, y convierte los fallos de la BD en ErrorDeAlmacenamiento | `php src/business_logic/tests/LogicaTest.php` → `# fail 0` (10 tests) |
| 6 | Comunicación | **(auto)** API REST: POST 201 / 400, GET 200 / 404, filtro `?tipo=`, ruta alternativa `?ruta=` | `php src/communication/tests/ApiTest.php` → `# fail 0` (7 tests) |
| 7 | Web | **(auto)** La LogicaFake web pide la URL correcta y traduce 200 / 404 / 500. La página muestra la última medición de O3 y de temperatura con su dispositivo y fecha | `cd src/frontend_business_logic && node --test` (5 tests) + abrir `https://ablabla.upv.edu.es/biometria/gui/` |
| 8 | Sistema completo | Si en `Medidor.h` se pone `VALOR_O3_FICTICIO = 235` y `VALOR_TEMPERATURA_FICTICIO = 215`, la app y la web muestran **0.235 ppm** y **21.5 °C** a los pocos segundos, y en phpMyAdmin aparecen filas `AARON-GTI-PBIO-1 \| 11 \| 0.235 \| <fecha>` | Prueba manual extremo a extremo (ver README) |
