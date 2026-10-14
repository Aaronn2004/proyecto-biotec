# GUI web — Diseño

Implementación: `src/gui/index.html`. Desplegada en `https://ablabla.upv.edu.es/biometria/gui/`.

## Diseño del Componente (Component Design)

Pantalla única: "Últimas mediciones".

```text
+--------------------------------------------------+
| Últimas mediciones                               |
|                                                  |
| Ozono (O3)                                       |
|   Valor: 0.235 ppm                               |
|   Dispositivo: AARON-GTI-PBIO-1 | Fecha: ...     |
|                                                  |
| Temperatura                                      |
|   Valor: 21.5 °C                                 |
|   Dispositivo: AARON-GTI-PBIO-1 | Fecha: ...     |
|                                                  |
| Actualizado: 10:45:14        [ Actualizar ]      |
+--------------------------------------------------+
```

Funciones de la página:

```text
mostrarUltimasMediciones()                          // pide a LogicaFake la última de O3 (11) y la de temperatura (12)
tipo: N, m: Medicion | null --> pintarMedicion()     // pinta valor, dispositivo y fecha (o "sin datos" y "-")
```

Eventos:

```text
al cargar la página          --> mostrarUltimasMediciones()
cada 2 s                     --> mostrarUltimasMediciones()
botón "Actualizar" pulsado   --> mostrarUltimasMediciones()
```

## Aclaraciones del Diseño (Design Clarifications)

- La GUI solo habla con `LogicaFake` (`frontend_business_logic_design.md`). No contiene `fetch` ni ninguna otra comunicación.
- La fecha se muestra en la hora local del navegador (`toLocaleString()`). La API la manda con la diferencia con UTC, así que no hay ambigüedad.
- Si hay un error (servidor caído), se muestra `Error: ...` en la línea de estado, y el siguiente refresco lo vuelve a intentar.
- `poner(campo, texto)` es una función local de una línea dentro de `pintarMedicion()`, no una función de la página.
- Página sin estilos: solo lo necesario para comprobar que la medida ficticia de la placa llega a la BD.

## Reglas Generales (General Rules)

- Lenguaje: HTML + JavaScript (navegador), sin librerías.
- Cada función lleva su diseño lógico en un comentario delimitado por `--------------------`.
- El fichero empieza con una cabecera: nombre, descripción, diseño, autor, fecha y copyright.
- Código claro y autoexplicativo.
- Tests automáticos: la lógica que usa la GUI (`LogicaFake`) tiene tests automáticos. La pantalla se comprueba con el criterio de aceptación 7 (`criterios_aceptacion.md`).
