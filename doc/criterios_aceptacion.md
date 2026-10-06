# Criterios de aceptación — Sprint 0

Cada criterio es reproducible siguiendo el README.

| # | Entregable | Criterio | Cómo se comprueba |
|---|------------|----------|-------------------|
| 1 | Arduino | La placa emite, alternando cada 2 s, un iBeacon de O3 (major `(11<<8)+contador`, minor = `VALOR_O3_FICTICIO`) y otro de temperatura (major `(12<<8)+contador`, minor = `VALOR_TEMPERATURA_FICTICIO`), con UUID `AARON-GTI-PBIO-1` | Monitor serie (115200) + app *nRF Connect* en el móvil |
| 2 | Android | "Buscar dispositivos" lista dispositivos BLE en Logcat; "Detener" para el escaneo | Logcat, filtro `>>>>` |
| 3 | Android | "Buscar nuestro dispositivo" detecta solo nuestro beacon y hace **un** POST por medida nueva (por tipo y contador), no por anuncio | Logcat: `¡Nuestro beacon!` y `POST OK: 201` una vez cada ~2 s |
| 3b | Android | La pantalla muestra estado del Bluetooth, O3, temperatura, contador, RSSI, major/minor y resultado del último envío | Ver la pantalla con la placa encendida; apagar el servidor → "Servidor: ERROR ..." y al encenderlo se recupera |
| 4 | Android | Decodificación de tramas (temperatura con signo), POST de LogicaFake, filtro de duplicados y estado de pantalla | Tests JUnit automáticos en verde (`TramaIBeaconTest`, `LogicaFakeTest`, `FiltroDuplicadosTest`, `EstadoNodoTest`) |
| 5 | Servidor | La lógica del negocio guarda y lee de la BD, y rechaza datos no válidos | `npm test` en verde (`logica.test.js`) |
| 6 | Servidor | API REST: POST 201 / 400, GET 200 / 404, filtro `?tipo=` | `npm test` en verde (`apirest.test.js`) |
| 7 | Web | Muestra la última medición de O3 y la última de temperatura | Abrir `http://localhost:8080/` |
| 8 | Sistema completo | Si en `Medidor.h` se pone `VALOR_O3_FICTICIO = 235` y `VALOR_TEMPERATURA_FICTICIO = 215`, la app y la web muestran **0.235 ppm** y **21.5 °C** a los pocos segundos | Prueba manual extremo a extremo (ver README) |
