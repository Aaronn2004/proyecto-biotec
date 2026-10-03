# Android (app) — Diseño

## Diseño del Componente

```text
                 --------- TramaIBeacon ------------
                 |
                 | losBytes: [N]
                 | uuid: [N]_16 , major: [N]_2 , minor: [N]_2 , txPower: Z
                 | esIBeacon: B
                 |
 bytes: [N]  --> TramaIBeacon() -->        // busca la cabecera 4C 00 02 15
                 |
                 |
            B <-- esIBeacon() <--
                 |
                 |
         Text <-- getUUIDComoTexto() <--
                 |
                 |
            N <-- getMajorEntero() <--
                 |
                 |
            N <-- getTipoMedicion() <--    // major >> 8
                 |
                 |
            N <-- getContador() <--        // major & 0xFF
                 |
                 |
            N <-- getValorEntero() <--     // minor
                 |
                 -----------------------------------

                 --------- Medicion ----------------
                 |
                 | tipo: N , valor: R , contador: N
                 |
tipo: N,
valor: R,
contador: N  --> Medicion() -->
                 |
                 |
trama: TramaIBeacon --> desdeTrama() --x   // O3: valor = minor / 1000
     r: Medicion    <--
                 |
                 |
         Text <-- toJSON() <--
                 |
                 -----------------------------------

                 --------- PeticionarioREST --------
                 |
metodo: Text,
url: Text,
cuerpo: Text --> hacerPeticionREST() <--   // en otro hilo
codigo: Z,
respuesta: Text <--                        // codigo -1 = error de red
                 |
                 -----------------------------------

                 --------- LogicaFake --------------
                 |
                 | urlServidor: Text
                 | elPeticionario: PeticionarioREST
                 |
 url: Text   --> LogicaFake() -->
                 |
                 |
m: Medicion  --> guardarMedicion() <--     // POST /medicion
exito: B,
detalle: Text <--
                 |
                 -----------------------------------

                 --------- ServicioEscuharBeacons --
                 |
                 | modoActual: Text = { TODOS, NUESTRO }
                 | ultimoMajorEnviado: Z
                 | laLogica: LogicaFake
                 |
 modo: Text  --> onStartCommand() -->      // arranca el escaneo en ese modo
                 |
                 |
                 onDestroy() -->           // detiene el escaneo
                 |
                 -----------------------------------

                 --------- MainActivity ------------
                 |
                 botonBuscarDispositivosBTLEPulsado() -->          // servicio en modo TODOS
                 |
                 |
                 botonDetenerBusquedaDispositivosBTLEPulsado() --> // para el servicio
                 |
                 |
                 botonBuscarNuestroDispositivoBTLEPulsado() -->    // servicio en modo NUESTRO
                 |
                 -----------------------------------
```

### Flujo "buscar nuestro dispositivo"

```text
ScanResult --> TramaIBeacon --> ¿esIBeacon y uuid == "AARON-GTI-PBIO-1"?
           --> ¿major != ultimoMajorEnviado? --> Medicion.desdeTrama() --> LogicaFake.guardarMedicion()
```

## Aclaraciones del Diseño

- Interfaz mínima: 3 botones; toda la información sale por Logcat (filtro `>>>>`).
- `LogicaFake.URL_SERVIDOR` debe apuntar a la IP del PC que ejecuta el servidor (móvil y PC en la misma WiFi).
- Solo se envía una medida cuando cambia el major (contador): evita cientos de POST repetidos por el mismo anuncio.
- `TramaIBeacon` busca la cabecera iBeacon en lugar de usar posiciones fijas (el campo *flags* puede estar o no).
- `Medicion.toJSON()` se construye a mano porque `org.json` no funciona en los tests JUnit locales.

## Reglas Generales

- Lenguaje: Java (Android, minSdk 28).
- Cada método lleva su diseño lógico en un comentario delimitado por `--------------------`.
- Código claro y autoexplicativo.
- Tests automáticos JUnit (`app/src/test`): `TramaIBeaconTest` (decodificación y `Medicion`) y `LogicaFakeTest` (POST contra un servidor HTTP falso).
