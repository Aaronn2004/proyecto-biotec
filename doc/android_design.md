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
            N <-- getValorEntero() <--     // minor sin signo
                 |
                 |
            Z <-- getValorEnteroConSigno() <--   // minor como entero de 16 bits con signo
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
trama: TramaIBeacon --> desdeTrama() --x   // O3: minor / 1000 ; TEMPERATURA: minorConSigno / 10
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

                 --------- FiltroDuplicados --------
                 |
                 | ultimoContadorPorTipo: tipo: N --> contador: N
                 |
tipo: N,
contador: N  --> esNueva() -->             // true la 1ª vez (y la apunta), false si se repite
          B  <--
                 |
                 |
tipo: N,
contador: N  --> olvidar() -->             // tras un POST fallido, para reintentar
                 |
                 |
                 reiniciar() -->
                 |
                 -----------------------------------

                 --------- EstadoNodo (observable, instancia única) --
                 |
                 | textoBluetooth, textoServidor: Text
                 | ultimoO3, ultimaTemperatura: R | nada
                 | contador: Z, rssi: Z, major: Z, minor: Z
                 | elObservador: Observador | nada
                 |
EstadoNodo   <-- getInstancia() --x
                 |
                 |
o: Observador --> observar() -->           // avisa en cada cambio
                 |
                 |
texto: Text  --> ponerEstadoBluetooth() -->
                 |
                 |
texto: Text  --> ponerEstadoServidor() -->
                 |
                 |
m: Medicion,
rssi: Z,
major: N,
minor: Z     --> registrarMedicion() -->
                 |
                 |
                 reiniciarMedidas() -->
                 |
                 |
         Text <-- getTextoO3() / getTextoTemperatura() / getTextoContador() /
                  getTextoRssi() / getTextoTrama() / getTextoBluetooth() / getTextoServidor() <--
                 |
                 -----------------------------------

Observador = ( estado: EstadoNodo ) --> estadoCambiado() -->

                 --------- ServicioEscuharBeacons --
                 |
                 | modoActual: Text = { TODOS, NUESTRO }
                 | elFiltro: FiltroDuplicados
                 | elEstado: EstadoNodo
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
                 |
                 onResume() -->            // observa EstadoNodo
                 |
                 |
                 onPause() -->             // deja de observar
                 |
                 |
e: EstadoNodo --> pintarEstado() -->       // (privado) copia los textos a la pantalla
                 |
                 -----------------------------------
```

### Flujo "buscar nuestro dispositivo"

```text
ScanResult --> TramaIBeacon --> ¿esIBeacon y uuid == "AARON-GTI-PBIO-1"?
           --> Medicion.desdeTrama() --> EstadoNodo.registrarMedicion()   (la pantalla se actualiza)
           --> ¿FiltroDuplicados.esNueva(tipo, contador)? --> LogicaFake.guardarMedicion()
           --> OK: EstadoNodo "guardada" | ERROR: FiltroDuplicados.olvidar() + EstadoNodo "ERROR ..."
```

## Aclaraciones del Diseño

- Pantalla: 3 botones + textos con el estado del Bluetooth, la última medida de O3 y de temperatura, contador, RSSI, major/minor y el resultado del último envío al servidor. El detalle completo sigue en Logcat (filtro `>>>>`).
- El servicio no toca la interfaz: publica en `EstadoNodo` y la actividad lo observa (patrón Observador). Así `EstadoNodo` se prueba con JUnit sin móvil.
- `LogicaFake.URL_SERVIDOR` debe apuntar a la IP del PC que ejecuta el servidor (móvil y PC en la misma WiFi).
- Solo se envía una medida por (tipo, contador): evita cientos de POST repetidos por el mismo anuncio. O3 y temperatura comparten contador, por eso el filtro va por tipo. Si el POST falla, se olvida la medida y se reintenta en el siguiente anuncio repetido.
- `TramaIBeacon` busca la cabecera iBeacon en lugar de usar posiciones fijas (el campo *flags* puede estar o no).
- `Medicion.toJSON()` se construye a mano porque `org.json` no funciona en los tests JUnit locales.

## Reglas Generales

- Lenguaje: Java (Android, minSdk 28).
- Cada método lleva su diseño lógico en un comentario delimitado por `--------------------`.
- Código claro y autoexplicativo.
- Tests automáticos JUnit (`app/src/test`): `TramaIBeaconTest` (decodificación, `Medicion`, temperatura con signo), `LogicaFakeTest` (POST contra un servidor HTTP falso), `FiltroDuplicadosTest` y `EstadoNodoTest`.
