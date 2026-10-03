# Web — Diseño

## Diseño del Componente

```text
                 --------- LogicaFake --------------
                 |
                 | urlBase: Text
                 |
urlBase: Text --> LogicaFake() -->         // "" = el mismo servidor que sirve la página
                 |
                 |
 Medicion | null <-- leerUltimaMedicion() <--   // GET /medicion/ultima (404 -> null)
                 |
                 -----------------------------------

index.html
  mostrarUltimaMedicion() -->   // llama a laLogica.leerUltimaMedicion() y pinta valor, tipo, contador, id y fecha
  botón "Actualizar"            // mostrarUltimaMedicion()
  refresco automático cada 3 s
```

## Aclaraciones del Diseño

- Página sin diseño: solo lo necesario para comprobar que la medida ficticia de la placa llega a la BD.
- La sirve el propio servidor (`http://localhost:8080/`), así no hay problemas de CORS.
- `LogicaFake.js` funciona en el navegador y en Node (se prueba en `servidor/test/apirest.test.js`).

## Reglas Generales

- Lenguaje: HTML + JavaScript (navegador).
- Cada función lleva su diseño lógico en un comentario delimitado por `--------------------`.
- Código claro y autoexplicativo.
- Test automático de `LogicaFake.leerUltimaMedicion()` contra el servidor real con BD en memoria.
