# Web — Diseño

## Diseño del Componente

```text
                 --------- LogicaFake --------------
                 |
                 | urlApi: Text
                 |
urlApi: Text --> LogicaFake() -->          // por defecto "api.php" (misma carpeta en Plesk)
                 |
                 |
 [tipo: N]    --> leerUltimaMedicion() <--   // GET api.php/medicion/ultima[?tipo=N] (404 -> null)
 Medicion | null <--
                 |
                 -----------------------------------

index.html
  mostrarUltimasMediciones() -->            // pide la última de O3 (11) y la de temperatura (12)
  tipo: N, m: Medicion | null --> pintarMedicion() -->   // pinta valor, dispositivo y fecha de ese tipo
  botón "Actualizar"            // mostrarUltimasMediciones()
  refresco automático cada 2 s
```

## Aclaraciones del Diseño

- Página sin diseño: solo lo necesario para comprobar que la medida ficticia de la placa llega a la BD.
- Se sube a la misma carpeta de Plesk que `api.php` (`httpdocs/biometria/`): mismo sitio web, sin problemas de CORS.
- `LogicaFake.js` funciona en el navegador y en Node: `tests/LogicaFake.test.js` la prueba simulando `fetch` (`node --test` dentro de `src/web`).

## Reglas Generales

- Lenguaje: HTML + JavaScript (navegador).
- Cada función lleva su diseño lógico en un comentario delimitado por `--------------------`.
- Código claro y autoexplicativo.
- Test automático de `LogicaFake.leerUltimaMedicion()` (URL pedida, 200, 404 y 500) con `fetch` simulado.
