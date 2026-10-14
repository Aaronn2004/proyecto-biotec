# Prompt 03 — Lógica del negocio

Adjuntar: `00_contexto_comun.md` + `doc/business_logic_design.md` + `doc/database_design.md`.

```text
Implementa src/business_logic/Logica.php (PHP 8 + PDO) según business_logic_design.md.
Es el ÚNICO componente que accede a la tabla medicion de database_design.md.

Requisitos:
- Excepciones del dominio: DatosNoValidosException (extiende InvalidArgumentException), MedicionRepetidaException
  y ErrorDeAlmacenamientoException.
- Clase Logica(PDO $laBD, ?callable $reloj = null). La conexión llega hecha (inyección de dependencias).
  El reloj devuelve un Text "Y-m-d H:i:s.v" en hora de Europe/Madrid; por defecto, la hora actual.
- guardarMedicion(MedicionNueva) -> Medicion:
  - valida la MedicionNueva:
    - dispositivo, texto de 1 a 16 caracteres;
    - tipo en {11, 12, 13};
    - valor, número finito.

    Si no es válida, lanza DatosNoValidosException.
  - pone la fecha con el reloj.
  - hace INSERT con sentencia preparada.
  - si la clave primaria (dispositivo, fecha) se repite (SQLSTATE 23000), lanza MedicionRepetidaException;
    cualquier otro error de PDO, ErrorDeAlmacenamientoException.
  - devuelve la fecha en ISO 8601 con ms y diferencia con UTC ("Y-m-d\TH:i:s.vP").
- leerUltimaMedicion(?int $tipo) -> Medicion | null: ORDER BY fecha DESC LIMIT 1, opcionalmente WHERE tipo = ?.
  Si falla la BD, lanza ErrorDeAlmacenamientoException.
- PROHIBIDO cualquier cosa de HTTP: códigos de estado, $_SERVER, cabeceras, rutas.

Tests:
- src/business_logic/tests/LogicaTest.php con una mini librería propia (tests/miniTest.php, sin PHPUnit).
- BD SQLite en memoria con la misma tabla, incluidas las restricciones.
- Un reloj falso para fijar la hora.
- Casos:
  - BD vacía;
  - guardar y devolver los 4 campos;
  - hora de Madrid en verano (+02:00) y en invierno (+01:00);
  - la última por fecha;
  - filtro por tipo;
  - clave repetida;
  - 9 datos no válidos;
  - fallo de la base de datos.
- Comando: php src/business_logic/tests/LogicaTest.php
```
