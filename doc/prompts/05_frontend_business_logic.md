# Prompt 05 — LogicaFake de la web

Adjuntar: `00_contexto_comun.md` + `doc/frontend_business_logic_design.md` + `doc/communication_design.md`.

```text
Implementa src/frontend_business_logic/LogicaFake.js (JavaScript sin librerías) según el diseño.

Requisitos:
- Clase LogicaFake(urlApi = "../communication/api.php").
- leerUltimaMedicion(tipo) con la MISMA firma lógica que la lógica del negocio del servidor
  (tipo: N | null --> leerUltimaMedicion() --> Medicion | null):
  - hace fetch a urlApi + "/medicion/ultima" (+ "?tipo=N" si tipo no es null ni undefined);
  - 200 devuelve el JSON;
  - 404 devuelve null;
  - cualquier otro código lanza un Error.
- Exporta con module.exports si existe, para usarla también en Node.

Tests:
- tests/LogicaFake.test.js con node:test, sustituyendo global.fetch.
- Casos: URL con tipo, URL por defecto sin tipo, 404 -> null, 500 -> error.
- Comando: cd src/frontend_business_logic && node --test
```
