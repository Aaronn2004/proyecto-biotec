# Arquitectura del sistema (Sprint 0)

## Diseño del Componente

```text
 ┌──────────────┐  BLE / iBeacon   ┌───────────────────────────┐  HTTP POST /medicion   ┌───────────────────────────────┐
 │   ARDUINO    │ ───────────────> │          ANDROID          │ ─────────────────────> │           SERVIDOR            │
 │ nRF52840     │  UUID, major,    │ ServicioEscuharBeacons    │  JSON                  │ servidor.js (punto de entrada)│
 │ Medidor      │  minor           │   TramaIBeacon            │                        │   ReglasREST  (API REST)      │
 │ Publicador   │                  │   Medicion                │                        │   Logica      (lógica negocio)│
 │ EmisoraBLE   │                  │   LogicaFake ── cliente   │                        │      │                        │
 └──────────────┘                  │   PeticionarioREST  REST  │                        │      v                        │
                                   └───────────────────────────┘                        │   SQLite (bd/mediciones.db)   │
                                                                                        └───────────────────────────────┘
                                   ┌───────────────────────────┐  HTTP GET /medicion/ultima          ^
                                   │            WEB            │ ── (?tipo=11 y ?tipo=12) ───────────┘
                                   │ index.html                │  JSON
                                   │   LogicaFake ── cliente   │
                                   └───────────────────────────┘
```

### Tipos compartidos

```text
TipoMedicion = { O3 = 11, TEMPERATURA = 12, RUIDO = 13 }

MedicionNueva = ( tipo: N, valor: R, contador: N )                   // la envía el móvil
Medicion      = ( id: N, tipo: N, valor: R, contador: N, fecha: Text ) // la devuelve el servidor
```

### Protocolo iBeacon (Arduino -> Android)

```text
uuid  : [N]_16  = "AARON-GTI-PBIO-1" (ASCII)     identifica NUESTRO nodo
major : N (16 bits) = (tipo << 8) + contador      byte alto: tipo, byte bajo: contador 0..255
minor : N (16 bits) = valor entero                O3: ppm * 1000, sin signo  (235 -> 0.235 ppm)
                                                  TEMPERATURA: décimas de °C, con signo (215 -> 21.5 °C, 0xFFDD -> -3.5 °C)

La placa alterna: anuncia O3 durante 2 s y después temperatura otros 2 s (mismo contador): sale un dato nuevo cada 2 s.
```

### Capas del servidor

```text
Punto de entrada (servidor.js) --> API REST (ReglasREST.js) --> Lógica del negocio (Logica.js) --> BD (SQLite)
```

Solo `Logica` conoce la base de datos. Solo `ReglasREST` conoce HTTP.

## Aclaraciones del Diseño

- El esquema dibujado pone `major = tipo, minor = valor`. Se usa el byte alto del major para el tipo y el byte bajo para un contador, así el móvil distingue una medida nueva de un anuncio repetido (la placa repite el mismo anuncio unas 16 veces por segundo).
- Los clientes (móvil y web) tienen una **LogicaFake**: expone las mismas funciones que la lógica del negocio (`guardarMedicion()`, `leerUltimaMedicion()`) pero por dentro hace peticiones REST. Si mañana cambia la API, solo cambia la LogicaFake.
- Patrones usados: arquitectura en capas (servidor), *proxy* / *fachada* (LogicaFake), inyección de dependencias (`Logica` recibe la ruta de la BD; los tests usan `:memory:`).
- Sin usuarios ni autenticación (fuera del alcance del Sprint 0).

## Reglas Generales

- Lenguajes: C++ (Arduino), Java (Android), JavaScript/Node.js >= 22.13 (servidor y web).
- Cada función/método lleva su diseño lógico en un comentario delimitado por `--------------------`.
- Código claro y autoexplicativo, nombres en castellano.
- Tests automáticos para la lógica del negocio, la API REST y las LogicaFake.
