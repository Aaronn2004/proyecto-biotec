# Comunicación (API REST) — Diseño

Implementación: `src/communication/api.php` (PHP 8). Desplegado en el hosting Plesk de la UPV.

URL base: `https://ablabla.upv.edu.es/biometria/communication/api.php`

## Diseño del Componente (Component Design)

| Método | Ruta (tras `api.php`)     | Cuerpo de la petición  | Respuesta OK                 | Errores                                                    | Llama a                           |
|--------|---------------------------|------------------------|------------------------------|------------------------------------------------------------|-----------------------------------|
| POST   | `/medicion`               | `MedicionNueva` (JSON) | 201 + `Medicion`             | 400 si el JSON está mal formado o los datos no son válidos; 409 si ya existe (dispositivo, fecha) | `Logica.guardarMedicion()`        |
| GET    | `/medicion/ultima`        | —                      | 200 + `Medicion`             | 404 si no hay mediciones                                   | `Logica.leerUltimaMedicion()`     |
| GET    | `/medicion/ultima?tipo=N` | —                      | 200 + `Medicion` de ese tipo | 400 si `tipo` no es un N; 404 si no hay de ese tipo      | `Logica.leerUltimaMedicion(tipo)` |
| OPTIONS | cualquiera               | —                      | 204 (preflight CORS)         | —                                                          | —                                 |

Todos los errores responden con `Error = ( error: Text )` en JSON. Si no se puede conectar con la BD, o falla al guardar o al leer, la respuesta es 500. Una ruta desconocida da 404.

Ejemplos:

```text
POST .../api.php/medicion       {"dispositivo":"AARON-GTI-PBIO-1","tipo":11,"valor":0.235}
  -> 201 {"dispositivo":"AARON-GTI-PBIO-1","tipo":11,"valor":0.235,"fecha":"2026-10-14T10:45:12.345+02:00"}
GET  .../api.php/medicion/ultima?tipo=12
  -> 200 {"dispositivo":"AARON-GTI-PBIO-1","tipo":12,"valor":21.5,"fecha":"..."}
```

Funciones de `api.php`:

```text
codigo: N, datos: Medicion | Error --> responderJSON()     // código HTTP + JSON
conectarBaseDeDatos() --> ConexionBD         // lee config.php (o PBIO_DSN en los tests)
rutaPedida() --> Text                        // "/medicion/ultima" (de ?ruta=, PATH_INFO o REQUEST_URI)
```

Traducción de los errores de la lógica del negocio:

```text
DatosNoValidos        (DatosNoValidosException)        --> 400
MedicionRepetida      (MedicionRepetidaException)      --> 409
ErrorDeAlmacenamiento (ErrorDeAlmacenamientoException) --> 500
cualquier otro error                                   --> 500
```

## Aclaraciones del Diseño (Design Clarifications)

- `api.php` solo traduce HTTP ⇄ lógica del negocio. No tiene SQL ni reglas del negocio: hace `require` de `../business_logic/Logica.php`.
- Las credenciales de la BD van en `config.php`, en la misma carpeta que `api.php`. Ese fichero solo existe en el servidor y está en `.gitignore`; en el repositorio solo está `config.example.php`.
- El Plesk de la UPV no rellena `PATH_INFO`. Por eso la ruta se saca de lo que va detrás de `api.php` en `REQUEST_URI`, y también se acepta `api.php?ruta=/medicion/ultima`.
- Cabeceras CORS abiertas (`Access-Control-Allow-Origin: *`) para poder probar la API desde otras páginas.
- Sin usuarios ni autenticación (Sprint 0): cualquiera que conozca la URL puede insertar mediciones.

## Reglas Generales (General Rules)

- Lenguaje: PHP 8.
- Cada función lleva su diseño lógico en un comentario delimitado por `--------------------`.
- Cada fichero empieza con una cabecera: nombre, descripción, diseño, autor, fecha y copyright.
- Código claro y autoexplicativo.
- **Tests automáticos de integración:** `php src/communication/tests/ApiTest.php`.
  - Arranca el servidor web de PHP con una BD SQLite temporal y hace peticiones HTTP reales.
  - Casos probados: 201, 400, 404, filtro por tipo, tipo inexistente, ruta alternativa.
  - Funciona en Linux y macOS.
  - Cada caso se describe con el nombre del `test('...')`, y la función auxiliar `peticion()` lleva su diseño entre guiones.
