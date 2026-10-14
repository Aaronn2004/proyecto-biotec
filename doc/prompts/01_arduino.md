# Prompt 01 — Nodo sensor (Arduino)

Adjuntar: `00_contexto_comun.md` + `doc/arduino_design.md` + el ejemplo `HolaMundoIBeacon` de la asignatura.

```text
Implementa el componente src/arduino/HolaMundoIBeacon (Arduino IDE, núcleo Adafruit nRF52, librería Bluefruit)
según doc/arduino_design.md. Parte del ejemplo HolaMundoIBeacon que adjunto y corrige lo que no
cumpla el diseño.

Requisitos:
- Clases Medidor, EmisoraBLE, Publicador, LED y PuertoSerie con los métodos del diseño.
- Medidas ficticias editables en Medidor.h:
  - VALOR_O3_FICTICIO = 235 (ppm*1000);
  - VALOR_TEMPERATURA_FICTICIO = 215 (décimas de °C, puede ser negativo);
  - opción USAR_VALOR_ALEATORIO, con los rangos VALOR_MIN..VALOR_MAX (O3) y TEMPERATURA_MIN..TEMPERATURA_MAX.
- En Publicador, laEmisora y publicarMedida() son privados.
- Trama iBeacon:
  - UUID de 16 bytes ASCII "AARON-GTI-PBIO-1";
  - fabricante 0x004C;
  - major = (tipo << 8) + contador;
  - minor = valor como entero de 16 bits CON signo.
  - Tipos: O3 = 11, TEMPERATURA = 12.
- loop(): contador++, anuncia O3 durante TIEMPO_ENTRE_DATOS_MS = 2000 y luego temperatura otros 2000 ms.
- setup() NO debe esperar al puerto serie (con batería se bloquearía).
- txPower válido para el nRF52840 (4 dBm).

Tests:
- Crea src/arduino/tests/NodoSensorTest.cpp y mocks de Arduino.h, bluefruit.h y Adafruit_TinyUSB.h
  en tests/mocks/.
- El mock de BLEBeacon debe guardar el último anuncio.
- Los tests comprueban:
  - el UUID;
  - el major de O3 y de temperatura;
  - el minor, incluida una temperatura negativa (-35 -> 0xFFDD);
  - el fabricante;
  - el contador 255.
- Comando: g++ -std=c++17 -I tests/mocks -I HolaMundoIBeacon tests/NodoSensorTest.cpp -o tests/nodo && tests/nodo
```
