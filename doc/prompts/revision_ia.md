# Revisión de lo generado por la IA

Lo que **no** se aceptó tal cual al revisar el código generado (contra el diseño, los tests y la prueba real), y cómo se resolvió. Orden cronológico.

| # | Componente | Qué se detectó al revisar | Cómo se comprobó | Corrección |
|---|---|---|---|---|
| 1 | Arduino | En el ejemplo de partida `HolaMundoIBeacon`, `EmisoraBLE` tenía los métodos vacíos: la placa no emitía nada | Ningún móvil veía el beacon | `EmisoraBLE` implementado con Bluefruit |
| 2 | Arduino | UUID del sketch distinto del que filtraba la app; `txPower = -59` (eso es un RSSI, no una potencia válida); `while(!Serial)` bloqueaba la placa con batería; CO2 en el major y contador en el minor, al revés del diseño | Lectura del código contra `arduino_design.md` | UUID propio `AARON-GTI-PBIO-1`; 4 dBm; sin espera al puerto serie; `major = (tipo<<8)+contador`, `minor = valor` |
| 3 | Arduino / Android | Duda: ¿Bluefruit envía el UUID en el orden escrito o invertido? | Escaneando beacons reales de compañeros con la app | Se confirmó el orden escrito; no hizo falta invertir |
| 4 | Android | Un POST por cada anuncio (la placa repite el mismo unas 16 veces por segundo): cientos de filas repetidas | Logcat | `FiltroDuplicados` por (tipo, contador), con `olvidar()` si el envío falla |
| 5 | Android | La temperatura salía como 215.0 en vez de 21.5, y la URL apuntaba a una IP local antigua | Pantalla del móvil | Se estaba instalando la app desde la carpeta vieja; se abrió `src/android` del repositorio |
| 6 | Servidor | La primera versión usaba Node + Express, pero el registro de npm estaba bloqueado y el despliegue debía ser en Plesk (PHP) | Instalación fallida | Se rehízo en PHP 8 + MariaDB, con la misma arquitectura en capas |
| 7 | Comunicación | En Plesk, `{"error":"ruta no encontrada: GET "}`: el hosting de la UPV no rellena `PATH_INFO` | Prueba con el navegador contra Plesk | `rutaPedida()` saca la ruta de `REQUEST_URI` y acepta `?ruta=` |
| 8 | Lógica del negocio | Las fechas se guardaban en UTC (2 h menos en phpMyAdmin) | Comparando con la hora real | Hora de `Europe/Madrid`; la API la devuelve con la diferencia con UTC |
| 9 | Base de datos | Petición de una tabla de 4 columnas sin `valor` y con la clave (dispositivo, fecha). La IA avisó: sin `valor` no se guarda la medida, y con segundos chocaban dos filas reales del mismo segundo (148 y 149) | Datos reales de phpMyAdmin | 4 columnas `dispositivo, tipo, valor, fecha`, fecha con milisegundos y 409 si se repite |
| 10 | Android | La IA añadió escapado de comillas en `toJSON()` | Revisión: el servicio solo crea mediciones de nuestro UUID, así que era código muerto | Eliminado para simplificar |
| 11 | Web (tests) | `node --test tests/` falla en Node 22 | Ejecutando los tests | El comando correcto es `node --test` |
| 12 | Todo | Auditoría con el agente revisor de la asignatura (`AGENTS.md`): faltaban la triada comunicación / lógica / BD, el formato oficial de la BD, el diseño de la LogicaFake web, los tests del Arduino y las cabeceras; y la LogicaFake de Android no tenía la misma firma que la del servidor | Checklist del agente | Diseños y carpetas separados, `database_design.md` en formato oficial, `MedicionNueva`/`Medicion` en Android, tests en el PC del nodo, cabeceras en todos los ficheros |
| 13 | Todo | Segunda auditoría, hecha por otro agente independiente. Encontró: un 500 sin JSON en `GET` si fallaba la BD; un rango de temperatura aleatoria fijo en el código que el diseño no recogía; `leerUltimaMedicion(null)` en la web pedía `?tipo=null`; métodos de Android que faltaban en el diseño; métodos privados dibujados en el borde de las cajas; flechas y tipos fuera de la notación (`PDO`, `T`, `[tipo]`); y el nombre `ServicioEscuharBeacons`, mal escrito | Informe del agente | Corregido en el código y en los diseños; se añadieron tests de `ErrorDeAlmacenamiento` y de `null` en la web |

Verificación final, tras la última revisión:

- **Tests automáticos:**
  - Arduino: 10;
  - lógica del negocio: 10;
  - API REST: 7;
  - LogicaFake web: 5;
  - Android: 19.
- **Prueba real:** placa → móvil → Plesk → web, criterio 8 de `criterios_aceptacion.md`.
