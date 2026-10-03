# Servidor — Diseño (base de datos, lógica del negocio, API REST)

## Diseño del Componente

### Base de datos (SQLite, `bd/esquema.sql`)

```text
medicion
+----------+---------+------------------------------------------+
| id       | INTEGER | PK, AUTOINCREMENT                        |
| tipo     | INTEGER | NOT NULL   11=O3, 12=temperatura, 13=ruido|
| valor    | REAL    | NOT NULL   O3 en ppm                     |
| contador | INTEGER | NOT NULL   nº de muestra de la placa     |
| fecha    | TEXT    | NOT NULL   ISO 8601 UTC (la pone el servidor) |
+----------+---------+------------------------------------------+
```

### Lógica del negocio (`logica/Logica.js`)

```text
MedicionNueva = ( tipo: N, valor: R, contador: N )
Medicion      = ( id: N, tipo: N, valor: R, contador: N, fecha: Text )

                 --------- Logica ------------------
                 |
                 | laBD: BaseDeDatos
                 |
rutaBD: Text --> Logica() -->              // crea la tabla si no existe
                 |
                 |
m: MedicionNueva --> guardarMedicion() -->  // lanza Error si m no es válida
  r: Medicion    <--
                 |
                 |
 Medicion | null <-- leerUltimaMedicion() <--
                 |
                 |
                 cerrar() -->
                 |
                 -----------------------------------
```

### API REST (`apirest/ReglasREST.js`)

| Método | Ruta               | Cuerpo petición   | Respuesta OK         | Errores                                   | Llama a                        |
|--------|--------------------|-------------------|----------------------|-------------------------------------------|--------------------------------|
| POST   | `/medicion`        | `MedicionNueva` (JSON) | 201 + `Medicion` | 400 `{error}` (JSON mal formado o datos no válidos) | `Logica.guardarMedicion()` |
| GET    | `/medicion/ultima` | —                 | 200 + `Medicion`     | 404 `{error}` si no hay mediciones        | `Logica.leerUltimaMedicion()`  |

Además `GET /` sirve la web (`src/web/index.html` y `LogicaFake.js`).

```text
laLogica: Logica --> crearReglasREST() --> manejador
laLogica: Logica --> crearServidor()   --> http.Server       // servidor.js
```

## Aclaraciones del Diseño

- `Logica` no sabe nada de HTTP: se prueba directamente (tests) y se podría exponer por MQTT sin tocarla.
- `ReglasREST` no contiene SQL.
- La fecha la pone el servidor (el nodo sensor no tiene reloj real).
- SQLite integrado en Node (`node:sqlite`): no hay que instalar ningún gestor de BD ni hacer `npm install`. Node 22 muestra un aviso *ExperimentalWarning*; es normal.
- Sin usuarios ni autenticación (Sprint 0).

## Reglas Generales

- Lenguaje: JavaScript (Node.js >= 22.13), sin dependencias externas.
- Cada función/método lleva su diseño lógico en un comentario delimitado por `--------------------`.
- Código claro y autoexplicativo.
- Tests automáticos (`npm test`): `test/logica.test.js` (lógica con BD en memoria) y `test/apirest.test.js` (API REST + LogicaFake de la web).
