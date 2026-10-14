# Lógica del negocio de los clientes (LogicaFake) — Diseño

Las GUI nunca hablan con la red: usan una **LogicaFake** con las mismas firmas que la lógica del negocio del servidor (`business_logic_design.md`).

| Cliente | Lo usa | Implementación |
|---|---|---|
| Web | `src/gui/index.html` | `src/frontend_business_logic/LogicaFake.js` (JavaScript) |
| Android | `ServicioEscucharBeacons` / `MainActivity` | `src/android/.../LogicaFake.java` (Java, se compila dentro de la app; detalle en `android_design.md`) |

## Diseño del Componente (Component Design)

Tipos (los mismos que en `business_logic_design.md`):

```text
MedicionNueva = ( dispositivo: Text, tipo: N, valor: R )
Medicion      = ( dispositivo: Text, tipo: N, valor: R, fecha: Text )
```

### Cliente web (`LogicaFake.js`)

```text
                 --------- LogicaFake (web) ----------------
                 |
                 | urlApi: Text
                 |
urlApi: Text | null --> LogicaFake() -->    // null: "../communication/api.php"
                 |
                 |
tipo: N | null --> leerUltimaMedicion() <-- // = Logica.leerUltimaMedicion(); GET /medicion/ultima[?tipo=N]
Medicion | null <--
                 |
                 --------------------------------------------
```

### Cliente Android (`LogicaFake.java`)

```text
                 --------- LogicaFake (Android) ------------
                 |
                 | urlServidor: Text
                 |
urlServidor: Text --> LogicaFake() -->
                 |
                 |
m: MedicionNueva --> guardarMedicion() <-- // = Logica.guardarMedicion(); POST /medicion
      Medicion   <--
                 |
                 --------------------------------------------
```

## Aclaraciones del Diseño (Design Clarifications)

- **Fake/proxy:** para su GUI, cada LogicaFake tiene la misma interfaz que la lógica del negocio del servidor, pero por dentro hace la petición REST de `communication_design.md`.
- **Subconjunto:** cada cliente solo ofrece lo que usa su GUI:
  - la web, `leerUltimaMedicion()`;
  - Android, `guardarMedicion()`.
- **Respuestas de la API, traducidas al dominio:**
  - Web:
    - 200 devuelve la `Medicion`;
    - 404 devuelve `null` (no hay mediciones);
    - cualquier otro código lanza un error, igual que la lógica del servidor lanza una excepción.
  - Android:
    - 201 devuelve la `Medicion` guardada;
    - cualquier otro resultado, incluido el fallo de red, entrega un error.
- **Resultado asíncrono:** es un detalle de implementación; la firma lógica es la misma.
  - En JavaScript devuelve una `Promise`.
  - En Android el resultado llega por la interfaz `Respuesta { medicionGuardada(Medicion) ; error(motivo: Text) }`, porque no se puede esperar a la red en el hilo principal.
- **Extensión de notación:** `T | null` significa "un valor de tipo T o ninguno" (parámetro opcional o resultado vacío).
- Desplegada en `biometria/frontend_business_logic/`. Por eso la API por defecto es `../communication/api.php`, relativa a la página `biometria/gui/index.html`.

## Reglas Generales (General Rules)

- Lenguaje: JavaScript (ES2017+, sin librerías) en la web; Java en Android. La versión web se exporta con `module.exports` para poder probarla en Node.
- Cada función o método lleva su diseño lógico en un comentario delimitado por `--------------------`.
- Cada fichero empieza con una cabecera: nombre, descripción, diseño, autor, fecha y copyright.
- Código claro y autoexplicativo.
- **Tests automáticos:**
  - Web: `fetch` simulado (URL pedida con y sin tipo, URL por defecto, 200, 404 y 500). Comando: `cd src/frontend_business_logic && node --test`.
  - Cada caso se describe con el nombre del `test("...")`; las funciones auxiliares (`simularFetch()`) llevan su diseño entre guiones.
  - Android: `LogicaFakeTest` (JUnit) contra un servidor HTTP falso (201, 400, sin red).
