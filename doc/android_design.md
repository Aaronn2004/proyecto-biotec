# Android (app) — Diseño

Implementación: `src/android/` (paquete `com.example.blasco.proyectbioreal`).

## Diseño del Componente (Component Design)

Tipos (los mismos que en `business_logic_design.md`):

```text
TipoMedicion  = { O3 = 11, TEMPERATURA = 12, RUIDO = 13 }
MedicionNueva = ( dispositivo: Text, tipo: N, valor: R )
Medicion      = ( dispositivo: Text, tipo: N, valor: R, fecha: Text )
Modo          = { TODOS, NUESTRO }            // cómo escanea el servicio (constantes MODO_TODOS, MODO_NUESTRO)
```

### Datos y protocolo

```text
                 --------- TramaIBeacon --------------------------------
                 |
                 | uuid: [N]_16 , major: [N]_2 , minor: [N]_2 , txPower: Z
                 | esIBeacon: B
                 |
                 | bytes: [N] --> buscarCabecera() --x      // posición donde empieza 4C 00 02 15, o -1
                 |          Z <--
                 |
                 |
 bytes: [N]  --> TramaIBeacon() -->
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
                 |
            Z <-- getTxPower() <--
                 |
                 --------------------------------------------------------

                 --------- Utilidades ---------------
                 |
                 |
 bytes: [N]  --> bytesToString() --x
         Text <--
                 |
                 |
bytes: [N]_2 --> bytesToIntOK() --x        // big endian, sin signo
            N <--
                 |
                 ------------------------------------

                 --------- MedicionNueva ------------
                 |
                 | dispositivo: Text , tipo: N , valor: R
                 | FACTOR_O3: R = 1000 ; FACTOR_TEMPERATURA: R = 10
                 |
dispositivo: Text,
tipo: N,
valor: R     --> MedicionNueva() -->
                 |
                 |
trama: TramaIBeacon --> desdeTrama() --x   // dispositivo = UUID ; O3: minor / 1000 ; TEMPERATURA: minorConSigno / 10
r: MedicionNueva    <--
                 |
                 |
         Text <-- toJSON() <--             // {"dispositivo":"...","tipo":N,"valor":R}
                 |
                 |
         Text <-- getDispositivo() <--
                 |
                 |
            N <-- getTipo() <--
                 |
                 |
            R <-- getValor() <--
                 |
                 |
         Text <-- toString() <--           // el mismo JSON (para el Logcat)
                 |
                 ------------------------------------

                 --------- Medicion -----------------
                 |
                 | dispositivo: Text , tipo: N , valor: R , fecha: Text
                 |
                 | texto: Text, patron: Text --> buscar() --x
                 |              Text | null <--
                 |
                 |
dispositivo: Text,
tipo: N,
valor: R,
fecha: Text  --> Medicion() -->
                 |
                 |
  json: Text --> desdeJSON() --x           // respuesta del servidor; null si le falta algún campo
Medicion | null <--
                 |
                 |
         Text <-- getDispositivo() <--
                 |
                 |
            N <-- getTipo() <--
                 |
                 |
            R <-- getValor() <--
                 |
                 |
         Text <-- getFecha() <--
                 |
                 ------------------------------------
```

### Lógica del negocio del cliente (LogicaFake) y comunicación

```text
                 --------- LogicaFake ---------------
                 |
                 | URL_SERVIDOR: Text = "https://ablabla.upv.edu.es/biometria/communication/api.php"
                 | urlServidor: Text
                 | elPeticionario: PeticionarioREST
                 |
                 |
urlServidor: Text --> LogicaFake() -->
                 |
                 |
m: MedicionNueva --> guardarMedicion() <-- // misma firma que Logica.guardarMedicion(); POST /medicion
     Medicion    <--
                 |
                 ------------------------------------

Respuesta (interfaz por la que llega el resultado de guardarMedicion()):
  guardada: Medicion --> medicionGuardada()
  motivo: Text       --> error()

                 --------- PeticionarioREST ---------
                 |
                 | hilos: Ejecutor                         // un hilo aparte para la red
                 |
                 | entrada: FlujoDeBytes --> leerTodo() --x
                 |                  Text <--
                 |
                 |
metodo: Text,
urlDestino: Text,
cuerpo: Text | null --> hacerPeticionREST() <--
codigo: Z,
cuerpoRespuesta: Text <--                  // por RespuestaREST; codigo -1 = error de red
                 |
                 ------------------------------------

RespuestaREST (interfaz):
  codigo: Z, cuerpo: Text --> callback()
```

### Estado de la pantalla y filtro de duplicados

```text
                 --------- FiltroDuplicados ---------
                 |
                 | ultimoContadorPorTipo: [ ( tipo: N, contador: N ) ]   // uno por tipo
                 |
                 |
tipo: N,
contador: N  --> esNueva() -->             // true la 1ª vez (y la apunta), false si se repite
          B  <--
                 |
                 |
tipo: N,
contador: N  --> olvidar() -->             // tras un envío fallido, para reintentar
                 |
                 |
                 reiniciar() -->
                 |
                 ------------------------------------

                 --------- EstadoNodo (observable, instancia única) ---
                 |
                 | instancia: EstadoNodo
                 | textoBluetooth: Text , textoServidor: Text
                 | ultimoO3: R | null , ultimaTemperatura: R | null
                 | contador: Z , rssi: Z , major: Z , minor: Z
                 | elObservador: Observador | null
                 |
                 | avisar() -->                          // llama a elObservador.estadoCambiado(this)
                 |
                 |
   EstadoNodo <-- getInstancia() --x
                 |
                 |
observador: Observador | null --> observar() -->
                 |
                 |
  texto: Text --> ponerEstadoBluetooth() -->
                 |
                 |
  texto: Text --> ponerEstadoServidor() -->
                 |
                 |
m: MedicionNueva,
contador: N,
rssi: Z,
major: N,
minor: Z      --> registrarMedicion() -->
                 |
                 |
                 reiniciarMedidas() -->
                 |
                 |
         Text <-- getTextoBluetooth() <--
                 |
                 |
         Text <-- getTextoServidor() <--
                 |
                 |
         Text <-- getTextoO3() <--             // "O₃: 0.235 ppm"
                 |
                 |
         Text <-- getTextoTemperatura() <--    // "Temperatura: 21.5 °C"
                 |
                 |
         Text <-- getTextoContador() <--
                 |
                 |
         Text <-- getTextoRssi() <--
                 |
                 |
         Text <-- getTextoTrama() <--          // major (tipo, contador) y minor
                 |
                 -------------------------------------------------------

Observador (interfaz):
  estado: EstadoNodo --> estadoCambiado()
```

### Servicio de escaneo

```text
                 --------- ServicioEscucharBeacons (Service) ------------------
                 |
                 | EXTRA_MODO: Text = "MODO" ; MODO_TODOS: Text = "TODOS" ; MODO_NUESTRO: Text = "NUESTRO"
                 | UUID_NUESTRO_BEACON: Text = "AARON-GTI-PBIO-1"
                 | elEscaner: EscanerBLE ; elCallbackDelEscaner: CallbackDeEscaneo
                 | modoActual: Modo | null
                 | laLogica: LogicaFake ; elFiltro: FiltroDuplicados ; elEstado: EstadoNodo
                 |
                 | obtenerEscaner() --> EscanerBLE | null      // null si no hay Bluetooth o está apagado
                 | tienePermisoEscanear() --> B
                 | modo: Modo --> arrancarEscaneo() -->        // onScanResult(): NUESTRO -> procesarSiEsNuestroBeacon(),
                 |                                             //                 TODOS   -> mostrarDispositivo()
                 | resultado: ResultadoEscaneo --> mostrarDispositivo() -->          // solo Logcat
                 | resultado: ResultadoEscaneo --> procesarSiEsNuestroBeacon() -->   // ver flujo
                 | m: MedicionNueva, contador: N --> enviarAlServidor() -->          // LogicaFake.guardarMedicion()
                 | detenerEscaneo() -->
                 |
                 |
intent: Intent,
flags: Z,
idInicio: Z  --> onStartCommand() -->      // lee el Modo del intent y (re)arranca el escaneo
           Z <--
                 |
                 |
                 onDestroy() -->           // detiene el escaneo
                 |
                 |
intent: Intent --> onBind() <--            // siempre null: servicio no enlazable
Enlace | null  <--
                 |
                 -------------------------------------------------------------
```

### GUI (`MainActivity`)

```text
                 --------- MainActivity --------------------------------------
                 |
                 | textoBluetooth, textoO3, textoTemperatura, textoContador,
                 | textoRssi, textoTrama, textoServidor: CampoDeTexto
                 |
                 | estado: EstadoNodo --> pintarEstado() -->       // copia los textos a la pantalla
                 | pedirPermisosNecesarios() -->
                 | modo: Modo --> arrancarServicio() -->
                 | bluetoothListo() --> B                          // si está apagado, pide encenderlo
                 | mensaje: Text --> avisar() -->                  // Toast + Logcat
                 |
                 |
estadoGuardado: Bundle --> onCreate() -->  // enlaza los textos y pide permisos
                 |
                 |
                 onResume() -->            // observa EstadoNodo
                 |
                 |
                 onPause() -->             // deja de observar
                 |
                 |
codigo: Z,
permisos: [Text],
resultados: [Z] --> onRequestPermissionsResult() -->
                 |
                 |
      v: View --> botonBuscarDispositivosBTLEPulsado() -->          // arrancarServicio(TODOS)
                 |
                 |
      v: View --> botonDetenerBusquedaDispositivosBTLEPulsado() --> // para el servicio
                 |
                 |
      v: View --> botonBuscarNuestroDispositivoBTLEPulsado() -->    // arrancarServicio(NUESTRO)
                 |
                 -------------------------------------------------------------
```

### Flujo "buscar nuestro dispositivo"

```text
ResultadoEscaneo --> TramaIBeacon --> ¿esIBeacon y uuid == "AARON-GTI-PBIO-1"?
           --> MedicionNueva.desdeTrama() (+ contador = trama.getContador())
           --> EstadoNodo.registrarMedicion()   (la pantalla se actualiza)
           --> ¿FiltroDuplicados.esNueva(tipo, contador)? --> LogicaFake.guardarMedicion(MedicionNueva)
           --> Medicion: EstadoNodo "guardada" | error: FiltroDuplicados.olvidar() + EstadoNodo "ERROR ..."
```

## Aclaraciones del Diseño (Design Clarifications)

- **Pantalla:**
  - Tres botones.
  - Textos con el estado del Bluetooth, la última medida de O3 y de temperatura, el contador, el RSSI, el major/minor y el resultado del último envío al servidor.
  - El detalle completo sigue en Logcat (filtro `>>>>`).
- **El servicio no toca la interfaz:** publica en `EstadoNodo` y la actividad lo observa (patrón Observador). Así `EstadoNodo` se prueba con JUnit sin móvil.
- **`LogicaFake`** es la lógica del negocio del cliente móvil (*fake*/proxy; ver también `frontend_business_logic_design.md`).
  - `guardarMedicion()` tiene **la misma firma** que en `business_logic_design.md`: `m: MedicionNueva --> guardarMedicion() --> Medicion`. Por dentro hace `POST /medicion` con `PeticionarioREST`.
  - Android no deja esperar a la red en el hilo principal, así que el resultado llega por `Respuesta`, con dos casos:
    - `medicionGuardada(Medicion)`;
    - `error(motivo)`, que equivale a la excepción que lanza la lógica del servidor.
  - La GUI y el servicio nunca usan HTTP directamente.
- **Qué se envía al servidor:** `dispositivo` (UUID), `tipo` y `valor`. El `contador` se queda en el móvil: sirve para el filtro de duplicados y para la pantalla, pero no se guarda en la BD.
- **Una medida por (tipo, contador):**
  - Evita cientos de POST repetidos del mismo anuncio, que la placa repite unas 16 veces por segundo.
  - O3 y temperatura comparten contador; por eso el filtro va por tipo.
  - Si el envío falla, se olvida la medida y se reintenta con el siguiente anuncio repetido.
- **`TramaIBeacon`** busca la cabecera iBeacon en vez de usar posiciones fijas, porque el campo *flags* puede estar o no.
- **JSON a mano:** `MedicionNueva.toJSON()` y `Medicion.desdeJSON()` no usan `org.json`, porque no funciona en los tests JUnit locales.
- **Tipos de la plataforma Android** que se usan tal cual, sin diseño propio:
  - `Intent`, `Bundle` y `View`;
  - `ResultadoEscaneo` (`ScanResult`), `EscanerBLE` (`BluetoothLeScanner`) y `CallbackDeEscaneo` (`ScanCallback`);
  - `CampoDeTexto` (`TextView`);
  - `Ejecutor` (`ExecutorService`), `FlujoDeBytes` (`InputStream`) y `Enlace` (`IBinder`).
- **Callbacks del escaneo:** `arrancarEscaneo()` registra un `CallbackDeEscaneo` con dos métodos:
  - `tipo: Z, resultado: ResultadoEscaneo --> onScanResult()`;
  - `codigoError: Z --> onScanFailed()`, que muestra el error en `EstadoNodo`.
- **Extensión de notación:** `T | null` significa "un valor de tipo T o ninguno".

## Reglas Generales (General Rules)

- Lenguaje: Java (Android, minSdk 28).
- Cada método lleva su diseño lógico en un comentario delimitado por `--------------------`, también los de los tests y los de las interfaces.
- Cada fichero empieza con una cabecera: nombre, descripción, diseño, autor, fecha y copyright.
- Código claro y autoexplicativo.
- Tests automáticos JUnit (`app/src/test`) para todos los métodos críticos:
  - `TramaIBeaconTest`: decodificación, `MedicionNueva`, `Medicion.desdeJSON` y temperatura con signo.
  - `LogicaFakeTest`: POST contra un servidor HTTP falso, con respuesta 201, 400 y sin red.
  - `FiltroDuplicadosTest`.
  - `EstadoNodoTest`.
