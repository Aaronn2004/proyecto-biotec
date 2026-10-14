# Arduino (nodo sensor) — Diseño

Implementación: `src/arduino/HolaMundoIBeacon/` (sketch) y `src/arduino/tests/` (tests en el PC).

## Diseño del Componente (Component Design)

Tipos:

```text
TipoMedicion = { O3 = 11, TEMPERATURA = 12, RUIDO = 13 }      // enum MedicionesID de Publicador
```

Constantes editables (`Medidor.h`):

```text
VALOR_O3_FICTICIO: Z = 235               // ppm * 1000  (0.235 ppm)
VALOR_TEMPERATURA_FICTICIO: Z = 215      // décimas de °C (21.5 °C); puede ser negativa
USAR_VALOR_ALEATORIO: B = false
VALOR_MIN: Z = 100 ; VALOR_MAX: Z = 400                // rango aleatorio del O3
TEMPERATURA_MIN: Z = 150 ; TEMPERATURA_MAX: Z = 300    // rango aleatorio de la temperatura
```

```text
                 --------- Medidor -----------------
                 |
                 |
                 Medidor() -->
                 |
                 |
                 iniciarMedidor() -->
                 |
                 |
            Z <-- medirO3() <--            // VALOR_O3_FICTICIO (o aleatorio entre VALOR_MIN y VALOR_MAX)
                 |
                 |
            Z <-- medirTemperatura() <--   // VALOR_TEMPERATURA_FICTICIO (o aleatorio entre TEMPERATURA_MIN y TEMPERATURA_MAX)
                 |
                 -----------------------------------

                 --------- EmisoraBLE --------------
                 |
                 | nombreEmisora: Text
                 | fabricanteID: N
                 | txPower: Z
                 |
nombre: Text,
fabricante: N,
potencia: Z  --> EmisoraBLE() -->
                 |
                 |
                 encenderEmisora() -->
                 |
                 |
                 detenerAnuncio() -->
                 |
                 |
            B <-- estaAnunciando() <--
                 |
                 |
uuid: [N]_16,
major: N,
minor: N,
rssi: Z      --> emitirAnuncioIBeacon() -->
                 |
                 -----------------------------------

                 --------- Publicador ----------------------------------------------
                 |
                 | beaconUUID: [N]_16 = "AARON-GTI-PBIO-1"
                 | RSSI_A_1_METRO: Z = -53
                 | laEmisora: EmisoraBLE = ( "Aaron_GTI", 0x004C, 4 )
                 |
                 | tipo: N, valor: Z, contador: N, tiempoEspera: N --> publicarMedida() -->
                 |      // major = (tipo << 8) + contador ; minor = valor (16 bits con signo);
                 |      // anuncia durante tiempoEspera y deja de anunciar
                 |
                 |
                 Publicador() -->
                 |
                 |
                 encenderEmisora() -->
                 |
                 |
valor: Z,
contador: N,
tiempoEspera: N --> publicarO3() -->       // publicarMedida( O3, valor, contador, tiempoEspera )
                 |
                 |
valor: Z,
contador: N,
tiempoEspera: N --> publicarTemperatura() -->   // publicarMedida( TEMPERATURA, valor, contador, tiempoEspera )
                 |
                 --------------------------------------------------------------------

                 --------- LED ---------------------
                 |
                 | numeroPin: N
                 |
    pin: N   --> LED() -->
                 |
                 |
                 encender() -->
                 |
                 |
                 apagar() -->
                 |
                 |
 tiempoMs: N --> brillar() -->
                 |
                 -----------------------------------

                 --------- PuertoSerie -------------
                 |
                 |
 baudios: N  --> PuertoSerie() -->
                 |
                 |
                 esperarDisponible() -->   // solo para depurar con USB: bloquea si no hay
                 |
                 |
mensaje: Text | Z --> escribir() -->
                 |
                 -----------------------------------
```

### Programa principal (`HolaMundoIBeacon.ino`)

```text
contador: N = 0
TIEMPO_ENTRE_DATOS_MS: N = 2000

setup() -->      // elPublicador.encenderEmisora() ; elMedidor.iniciarMedidor() ; elLED.brillar(500)
loop()  -->      // contador++ ;
                 // elPublicador.publicarO3( elMedidor.medirO3(), contador, TIEMPO_ENTRE_DATOS_MS ) ;
                 // elPublicador.publicarTemperatura( elMedidor.medirTemperatura(), contador, TIEMPO_ENTRE_DATOS_MS )
```

## Aclaraciones del Diseño (Design Clarifications)

- **Medidas ficticias:** se cambian en `Medidor.h` (`VALOR_O3_FICTICIO` y `VALOR_TEMPERATURA_FICTICIO`).
- **Trama iBeacon:**
  - UUID: los 16 bytes ASCII de `AARON-GTI-PBIO-1`, propios, para no captar los beacons de otros equipos.
  - Fabricante: Apple 0x004C, obligatorio en iBeacon.
  - `major`: byte alto = tipo, byte bajo = contador (0..255, da la vuelta).
  - `minor`: el valor como entero de 16 bits **con signo**, para que la temperatura pueda ser negativa (-35 → 0xFFDD).
- **Ritmo:** cada medida se anuncia `TIEMPO_ENTRE_DATOS_MS` (2 s) y se pasa a la siguiente, así que sale un dato nuevo cada 2 s.
- **Potencia:** `txPower = 4` dBm, una potencia válida del nRF52840.
- **Arranque sin USB:** `setup()` no espera al puerto serie (`esperarDisponible()` es solo para depurar). Con batería y sin USB, la placa se quedaría bloqueada.
- **Extensión de notación:** `Text | Z` significa "un texto o un entero" (`escribir()` es una plantilla de C++).

## Reglas Generales (General Rules)

- Lenguaje: C++ (Arduino IDE, placa SparkFun Pro nRF52840 Mini, núcleo Adafruit nRF52 / Bluefruit).
- Cada función y método lleva su diseño lógico en un comentario delimitado por `--------------------`.
- Cada fichero empieza con una cabecera: nombre, descripción, diseño, autor, fecha y copyright.
- Código claro y autoexplicativo.
- **Tests automáticos en el PC** (`src/arduino/tests/NodoSensorTest.cpp`):
  - Usan *mocks* de Arduino y Bluefruit (`tests/mocks/`), que solo imitan esas API y no llevan diseño propio.
  - Comprueban que `Medidor` devuelve los valores ficticios.
  - Comprueban que `Publicador` emite el UUID, `major = (tipo << 8) + contador`, `minor = valor` (también una temperatura negativa) y el fabricante 0x004C, y que deja de anunciar al acabar.
  - Comando, desde `src/arduino`: `g++ -std=c++17 -I tests/mocks -I HolaMundoIBeacon tests/NodoSensorTest.cpp -o tests/nodo && tests/nodo`.
- **Prueba en la placa (manual, reproducible):**
  - el monitor serie a 115200 baudios muestra `valor=235`;
  - con la app *nRF Connect* se ve el iBeacon con UUID `AARON-GTI-PBIO-1`, major `0x0Bxx` y minor `235`.
