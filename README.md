# Proyecto Biometría y Medio Ambiente — Sprint 0

Una medida **ficticia** de O3 sale de la placa Arduino por iBeacon, el móvil Android la recibe y la envía por REST al servidor, que la guarda en una base de datos; la web muestra la última medida guardada.

```
Arduino (C++) --BLE iBeacon--> Android (Java) --POST /medicion--> Servidor (Node.js) --> SQLite
                                                   Web (HTML/JS) --GET /medicion/ultima--^
```

## Estructura

| Carpeta | Contenido | Se abre con |
|---------|-----------|-------------|
| `src/arduino/HolaMundoIBeacon/` | Nodo sensor (medida ficticia) | Arduino IDE |
| `src/android/` | App Android | Android Studio (*File → Open* → `src/android`) |
| `src/servidor/` | Servidor REST + lógica del negocio + BD + tests | `node servidor.js` |
| `src/web/` | Página web + LogicaFake (la sirve el servidor) | `http://localhost:8080` |
| `doc/` | Diseños (`*_design.md`) y criterios de aceptación | — |

## Requisitos

- Arduino IDE con la placa SparkFun Pro nRF52840 Mini (núcleo Adafruit nRF52).
- Android Studio y un **móvil real** con Bluetooth (el emulador no tiene BLE).
- Node.js **22.13 o superior** (`node --version`). No hace falta `npm install`: no hay dependencias.
- PC y móvil en la **misma red WiFi**.

## Puesta en marcha (prueba extremo a extremo)

1. **Elige las medidas ficticias**: en `src/arduino/HolaMundoIBeacon/Medidor.h` pon, por ejemplo, `VALOR_O3_FICTICIO = 235` (= 0.235 ppm) y `VALOR_TEMPERATURA_FICTICIO = 215` (= 21.5 °C). Sube el sketch a la placa. En el monitor serie (115200) verás `tipo=11 valor=235` y `tipo=12 valor=215`, alternando cada 2 s (`TIEMPO_ENTRE_DATOS_MS` en `HolaMundoIBeacon.ino`).
2. **Arranca el servidor**:
   ```
   cd src/servidor
   node servidor.js
   ```
   Abre `http://localhost:8080/` en el navegador: dirá "Todavía no hay mediciones".
3. **Averigua la IP del PC**: `ipconfig` → "Dirección IPv4" de la WiFi (p. ej. `192.168.1.37`).
   Si Windows pregunta por el firewall al arrancar Node, permite el acceso en **redes privadas**.
4. **Configura la app**: en `LogicaFake.java` cambia `URL_SERVIDOR` a `http://<IP-del-PC>:8080`. Instala la app en el móvil y acepta los permisos (Bluetooth y ubicación). Ten el Bluetooth **y la ubicación** activados.
5. Pulsa **"Buscar nuestro dispositivo BTLE"**. En Logcat (filtro `>>>>`) verás `¡Nuestro beacon! {"tipo":11,"valor":0.235,...}` y `POST OK: 201 ...`. En la pantalla de la app verás el estado del Bluetooth, O₃, temperatura, contador, RSSI, major/minor y si el último envío al servidor fue bien.
6. La web se refresca sola cada 2 s y muestra **O3: 0.235 ppm** y **Temperatura: 21.5 °C**.

Para comprobar el servidor sin móvil (desde otra ventana de cmd):
```
curl -X POST http://localhost:8080/medicion -H "Content-Type: application/json" -d "{\"tipo\":11,\"valor\":0.5,\"contador\":1}"
```

## Tests automáticos

- Servidor, API REST y LogicaFake web: `cd src/servidor && npm test` (12 tests).
- Android: en Android Studio, botón derecho sobre `app/src/test/java` → *Run 'Tests in ...'* (16 tests + el de ejemplo).

Criterios de aceptación: `doc/criterios_aceptacion.md`.
