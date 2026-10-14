# Prompt 06 — GUI web

Adjuntar: `00_contexto_comun.md` + `doc/gui_design.md` + `doc/frontend_business_logic_design.md`.

```text
Implementa src/gui/index.html según gui_design.md: una sola pantalla, "Últimas mediciones".

Requisitos:
- Carga ../frontend_business_logic/LogicaFake.js y SOLO usa LogicaFake: ningún fetch en la página.
- Dos bloques, O3 (tipo 11) y Temperatura (tipo 12), cada uno con:
  - valor (ppm / °C);
  - dispositivo;
  - fecha (new Date(fecha).toLocaleString()).
- Si no hay datos, "sin datos".
- Funciones mostrarUltimasMediciones() y pintarMedicion(tipo, m), con su diseño en comentario.
- Refresco al cargar la página, cada 2 s y con el botón "Actualizar". Los errores se ven en una línea de estado.
- Sin CSS ni librerías.
```
