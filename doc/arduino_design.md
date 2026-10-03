# Arduino (nodo sensor) — Diseño

## Diseño del Componente

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
            Z <-- medirO3() <--            // ppm * 1000 (valor FICTICIO)
                 |
                 -----------------------------------

                 --------- EmisoraBLE --------------
                 |
                 | nombreEmisora: Text
                 | fabricanteID: N
                 | txPower: Z
                 |
nombre: Text,
fabID: N,
txPower: Z   --> EmisoraBLE() -->
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

                 --------- Publicador --------------
                 |
                 | beaconUUID: [N]_16 = "AARON-GTI-PBIO-1"
                 | RSSI_A_1_METRO: Z = -53
                 | laEmisora: EmisoraBLE
                 |
                 encenderEmisora() -->
                 |
                 |
valor: Z,
contador: N,
tiempoEspera: N --> publicarO3() -->       // major = (11 << 8) + contador ; minor = valor
                 |
                 -----------------------------------

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
 baudios: N  --> PuertoSerie() -->
                 |
                 |
                 esperarDisponible() -->
                 |
                 |
 mensaje: T  --> escribir() -->
                 |
                 -----------------------------------
```

### Programa principal

```text
setup():  elPublicador.encenderEmisora() ; elMedidor.iniciarMedidor()
loop():   contador++ ; valor = elMedidor.medirO3() ; elPublicador.publicarO3( valor, contador, 3000 )
```

## Aclaraciones del Diseño

- **Medida ficticia**: se cambia en `Medidor.h` (`VALOR_O3_FICTICIO`). Con `USAR_VALOR_ALEATORIO = true` genera valores entre `VALOR_MIN` y `VALOR_MAX`.
- Cambios respecto a la versión anterior de `HolaMundoIBeacon`:
  - `EmisoraBLE` estaba vacía (métodos sin código): **la placa no emitía nada**. Ahora usa la pila Bluefruit.
  - El UUID (`GRUPO-JORDI-BLE0`) no coincidía con el que filtraba la app (`EPSG-GTI-PROY-3A`). Ahora ambos usan `AARON-GTI-PBIO-1`, propio, para no captar los beacons de otros equipos.
  - `txPower` valía -59 (no es una potencia válida del nRF52840; -59 es un RSSI a 1 m). Ahora 4 dBm.
  - `setup()` hacía `while(!Serial)`: con batería y sin USB la placa se quedaba bloqueada. Eliminado.
  - Se codificaba CO2 en el major y el contador en el minor, al revés del diseño. Ahora `major = tipo+contador`, `minor = valor`.
- `ServicioEnEmisora.h` no se usa en el Sprint 0 (servicios GATT); se deja sin incluir.

## Reglas Generales

- Lenguaje: C++ (Arduino IDE, placa SparkFun Pro nRF52840 Mini, núcleo Adafruit nRF52 / Bluefruit).
- Cada método lleva su diseño lógico en un comentario delimitado por `--------------------`.
- Código claro y autoexplicativo.
- Prueba (manual, reproducible): monitor serie a 115200 baudios muestra `valor=235`; con la app *nRF Connect* se ve el iBeacon con UUID `AARON-GTI-PBIO-1`, major `0x0Bxx`, minor `235`.
