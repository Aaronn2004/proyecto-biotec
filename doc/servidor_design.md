# Servidor — Diseño (base de datos, lógica del negocio, API REST)

Desplegado en el hosting **Plesk** de la UPV: PHP + MariaDB.

## Diseño del Componente

### Base de datos (MariaDB, `esquema.sql`)

```text
medicion
+-------------+------------------+----------------------------------------------------+
| dispositivo | VARCHAR(16)      | PK   UUID del iBeacon ("AARON-GTI-PBIO-1")          |
| tipo        | TINYINT UNSIGNED |      11=O3, 12=temperatura, 13=ruido               |
| valor       | DOUBLE           |      O3 en ppm, temperatura en °C                  |
| fecha       | DATETIME(3)      | PK   hora local de Madrid con milisegundos (servidor) |
+-------------+------------------+----------------------------------------------------+
clave primaria compuesta: (dispositivo, fecha)
índice (tipo, fecha) para "la última de un tipo"
```

### Lógica del negocio (`Logica.php`)

```text
MedicionNueva = ( dispositivo: Text, tipo: N, valor: R )
Medicion      = ( dispositivo: Text, tipo: N, valor: R, fecha: Text )   // fecha ISO 8601 con ms y diferencia horaria "2026-10-13T11:50:14.237+02:00"

                 --------- Logica ------------------
                 |
                 | laBD: PDO
                 |
laBD: PDO,
[reloj: Funcion] --> Logica() -->          // la conexión se crea fuera (inyección de dependencias); el reloj solo en los tests
                 |
                 |
m: MedicionNueva --> guardarMedicion() -->  // InvalidArgumentException si m no es válida;
                 |                          // MedicionRepetidaException si ya existe (dispositivo, fecha)
  r: Medicion    <--
                 |
                 |
 [tipo: N]    --> leerUltimaMedicion() <--   // sin tipo: la última de cualquier tipo
 Medicion | null <--
                 |
                 -----------------------------------
```

### API REST (`api.php`)

URL base: `https://<usuario>.upv.edu.es/biometria/api.php`

| Método | Ruta (tras `api.php`)     | Cuerpo petición        | Respuesta OK                 | Errores                                              | Llama a                           |
|--------|---------------------------|------------------------|------------------------------|------------------------------------------------------|-----------------------------------|
| POST   | `/medicion`               | `MedicionNueva` (JSON) | 201 + `Medicion`             | 400 `{error}` (JSON mal formado o datos no válidos); 409 si ya existe (dispositivo, fecha) | `Logica.guardarMedicion()`        |
| GET    | `/medicion/ultima`        | —                      | 200 + `Medicion`             | 404 `{error}` si no hay mediciones                   | `Logica.leerUltimaMedicion()`     |
| GET    | `/medicion/ultima?tipo=N` | —                      | 200 + `Medicion` de ese tipo | 400 si `tipo` no es entero; 404 si no hay de ese tipo | `Logica.leerUltimaMedicion(tipo)` |

Cualquier error de conexión con la BD → 500 `{error}`. Ruta desconocida → 404.

```text
--> conectarBaseDeDatos() --> PDO        // lee config.php (o PBIO_DSN en los tests)
--> rutaPedida() --> Text                // PATH_INFO ("/medicion/ultima") o ?ruta=
codigo: N, datos --> responderJSON() -->
```

## Aclaraciones del Diseño

- `Logica` no sabe nada de HTTP y recibe la conexión hecha: en producción es MariaDB y en los tests SQLite en memoria, sin cambiar una línea de `Logica`.
- `api.php` no contiene SQL.
- La fecha la pone el servidor en hora local de Madrid (`Europe/Madrid`, cambia sola entre verano e invierno); el nodo sensor no tiene reloj real. En la API se devuelve con la diferencia con UTC (`+02:00` / `+01:00`) para que no sea ambigua. Inconveniente asumido: la hora que se repite al pasar a invierno (de 2:00 a 3:00) no se distingue en la BD.
- La tabla tiene solo 4 columnas. La clave primaria es (dispositivo, fecha): un mismo dispositivo no puede tener dos medidas en el mismo instante. La fecha lleva **milisegundos** porque con precisión de 1 s chocaban medidas reales (O3 y temperatura del mismo nodo llegaron en el mismo segundo). Si aun así coincidieran, la API responde 409 y el móvil reintenta con el siguiente anuncio.
- `contador` ya no se guarda: solo lo usa el móvil para no enviar dos veces el mismo anuncio. Inconveniente asumido: desde la BD no se puede saber si se ha perdido alguna medida.
- "La última" se decide por `fecha`. Inconveniente asumido: en la hora que se repite al pasar a invierno el orden podría salir cambiado.
- Las credenciales de la BD van en `config.php`, que solo existe en el servidor y está en `.gitignore`. En el repositorio solo está `config.example.php`.
- Si el hosting no pasara la ruta tras `api.php` (PATH_INFO), la API acepta también `api.php?ruta=/medicion/ultima`.
- Sin usuarios ni autenticación (Sprint 0): cualquiera que conozca la URL puede insertar mediciones.

## Reglas Generales

- Lenguaje: PHP 8 (PDO) y SQL de MariaDB/MySQL.
- Cada función/método lleva su diseño lógico en un comentario delimitado por `--------------------`.
- Código claro y autoexplicativo.
- Tests automáticos sin dependencias (desde `src/servidor`): `php tests/LogicaTest.php` (lógica con SQLite en memoria) y `php tests/ApiTest.php` (API REST con peticiones HTTP reales al servidor web de PHP; en Linux/macOS).
