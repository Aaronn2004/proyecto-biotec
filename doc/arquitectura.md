# Arquitectura del sistema (Sprint 0)

Visión general. No es un componente: cada componente tiene su propio `xxx_design.md` y su carpeta `src/xxx/`.

## Componentes, diseños y código

| Componente | Diseño | Código | Dónde se ejecuta |
|---|---|---|---|
| Nodo sensor | `arduino_design.md` | `src/arduino/` | Placa SparkFun Pro nRF52840 Mini |
| App móvil (GUI + servicio + LogicaFake) | `android_design.md` | `src/android/` | Móvil Android |
| Comunicación (API REST) | `communication_design.md` | `src/communication/` | Plesk UPV: `biometria/communication/` |
| Lógica del negocio | `business_logic_design.md` | `src/business_logic/` | Plesk UPV: `biometria/business_logic/` |
| Base de datos | `database_design.md` | `src/database/` | MariaDB de Plesk (BD `biotec`) |
| Lógica del negocio del cliente web (LogicaFake) | `frontend_business_logic_design.md` | `src/frontend_business_logic/` | Navegador: `biometria/frontend_business_logic/` |
| GUI web | `gui_design.md` | `src/gui/` | Navegador: `biometria/gui/` |

## Diagrama

```text
 ┌──────────────┐  BLE / iBeacon   ┌───────────────────────────┐   HTTPS POST /medicion   ┌──────────────────────────────────┐
 │   ARDUINO    │ ───────────────> │          ANDROID          │ ───────────────────────> │       SERVIDOR (Plesk UPV)       │
 │ Medidor      │  UUID, major,    │ ServicioEscucharBeacons   │   MedicionNueva (JSON)   │ communication/api.php            │
 │ Publicador   │  minor           │   TramaIBeacon            │                          │        │  (solo HTTP <-> lógica)  │
 │ EmisoraBLE   │                  │   MedicionNueva           │                          │        v                         │
 └──────────────┘                  │   LogicaFake ── proxy ────┼──                        │ business_logic/Logica.php        │
                                   │   PeticionarioREST        │                          │        │  (única que usa la BD)   │
                                   │ MainActivity (GUI)        │                          │        v                         │
                                   └───────────────────────────┘                          │ database: MariaDB, tabla medicion│
                                   ┌───────────────────────────┐  HTTPS GET /medicion/ultima └──────────────────────────────────┘
                                   │            WEB            │ ──── (?tipo=11, ?tipo=12) ──────────────^
                                   │ gui/index.html            │      Medicion (JSON)
                                   │   └─> frontend_business_  │
                                   │       logic/LogicaFake.js │
                                   └───────────────────────────┘
```

## Tipos compartidos

```text
TipoMedicion  = { O3 = 11, TEMPERATURA = 12, RUIDO = 13 }
MedicionNueva = ( dispositivo: Text, tipo: N, valor: R )               // la envía el móvil (dispositivo = UUID del iBeacon)
Medicion      = ( dispositivo: Text, tipo: N, valor: R, fecha: Text )   // la devuelve la lógica del negocio (fecha la pone el servidor)
```

## Protocolo iBeacon (Arduino -> Android)

```text
uuid  : [N]_16  = "AARON-GTI-PBIO-1" (ASCII)     identifica NUESTRO nodo (columna dispositivo)
major : N (16 bits) = (tipo << 8) + contador      byte alto: tipo, byte bajo: contador 0..255
minor : N (16 bits) = valor entero                O3: ppm * 1000, sin signo  (235 -> 0.235 ppm)
                                                  TEMPERATURA: décimas de °C, con signo (215 -> 21.5 °C, 0xFFDD -> -3.5 °C)

La placa alterna: anuncia O3 durante 2 s y después temperatura otros 2 s (mismo contador): sale un dato nuevo cada 2 s.
```

## Decisiones de arquitectura

- **Capas en el servidor:** comunicación → lógica del negocio → base de datos. La comunicación no tiene SQL, y la lógica no sabe nada de HTTP: recibe la conexión ya hecha (inyección de dependencias), así que en los tests usa SQLite en memoria.
- **LogicaFake (proxy/fachada) en los dos clientes.** La web y el móvil llaman a funciones con la misma firma que la lógica del negocio, y por dentro hacen peticiones REST:
  - web: `leerUltimaMedicion()`;
  - móvil: `guardarMedicion()`.

  Las GUI nunca hacen HTTP. Si cambia la API, solo cambia la LogicaFake.
- **Observador en el móvil:** el servicio publica en `EstadoNodo` y la pantalla lo observa.
- **Esquema dibujado:** pone `major = tipo, minor = valor`. Se usa el byte alto del major para el tipo y el byte bajo para un contador. Así el móvil distingue una medida nueva de un anuncio repetido, porque la placa repite el mismo anuncio unas 16 veces por segundo.
- **Alcance:** sin usuarios ni autenticación (fuera del alcance del Sprint 0).
