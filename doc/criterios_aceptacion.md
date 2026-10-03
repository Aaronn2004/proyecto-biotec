# Criterios de aceptación — Sprint 0

Cada criterio es reproducible siguiendo el README.

| # | Entregable | Criterio | Cómo se comprueba |
|---|------------|----------|-------------------|
| 1 | Arduino | La placa emite un iBeacon con UUID `AARON-GTI-PBIO-1`, major `(11<<8)+contador` y minor = `VALOR_O3_FICTICIO` | Monitor serie (115200) + app *nRF Connect* en el móvil |
| 2 | Android | "Buscar dispositivos" lista dispositivos BLE en Logcat; "Detener" para el escaneo | Logcat, filtro `>>>>` |
| 3 | Android | "Buscar nuestro dispositivo" detecta solo nuestro beacon y hace **un** POST por medida nueva (no por anuncio) | Logcat: `¡Nuestro beacon!` y `POST OK: 201` una vez cada ~3 s |
| 4 | Android | Decodificación de tramas y POST de LogicaFake | Tests JUnit automáticos en verde (`TramaIBeaconTest`, `LogicaFakeTest`) |
| 5 | Servidor | La lógica del negocio guarda y lee de la BD, y rechaza datos no válidos | `npm test` en verde (`logica.test.js`) |
| 6 | Servidor | API REST: POST 201 / 400, GET 200 / 404 | `npm test` en verde (`apirest.test.js`) |
| 7 | Web | Muestra la última medición almacenada | Abrir `http://localhost:8080/` |
| 8 | Sistema completo | Si en `Medidor.h` se pone `VALOR_O3_FICTICIO = 235`, la web muestra **0.235** ppm a los pocos segundos | Prueba manual extremo a extremo (ver README) |
