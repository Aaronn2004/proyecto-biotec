<?php
// ==============================================================
// Fichero:     LogicaTest.php
// Descripción: Tests automáticos de la lógica del negocio (sin HTTP),
//              con una BD SQLite en memoria.
//              Ejecutar:  php src/business_logic/tests/LogicaTest.php
// Diseño:      doc/business_logic_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
require_once __DIR__ . '/miniTest.php';
require_once __DIR__ . '/../Logica.php';

const NODO = 'AARON-GTI-PBIO-1';

// --------------------------------------------------------------
// fechaInicial: Text --> relojFalso() --> Reloj
// Reloj de prueba: cada llamada devuelve 1 segundo más que la anterior,
// empezando en fechaInicial (hora de Madrid, "Y-m-d H:i:s.v").
// --------------------------------------------------------------
function relojFalso(string $fechaInicial = '2026-10-13 11:50:14.237'): callable {
    $instante = new DateTimeImmutable($fechaInicial);
    return function () use (&$instante) {
        $actual = $instante->format('Y-m-d H:i:s.v');
        $instante = $instante->modify('+1 second');
        return $actual;
    };
}

test('con la BD vacía, leerUltimaMedicion() devuelve null', function () {
    $laLogica = new Logica(crearBDdePrueba());
    igual(null, $laLogica->leerUltimaMedicion());
});

test('guardarMedicion() devuelve dispositivo, tipo, valor y fecha ISO de Madrid con milisegundos', function () {
    $laLogica = new Logica(crearBDdePrueba());
    $guardada = $laLogica->guardarMedicion(['dispositivo' => NODO, 'tipo' => 11, 'valor' => 0.235]);
    igual(['dispositivo', 'tipo', 'valor', 'fecha'], array_keys($guardada), 'solo las 4 columnas');
    igual(NODO, $guardada['dispositivo']);
    igual(11, $guardada['tipo']);
    igual(0.235, $guardada['valor']);
    igual(1, preg_match('/^\d{4}-\d\d-\d\dT\d\d:\d\d:\d\d\.\d{3}\+0[12]:00$/', $guardada['fecha']), 'formato de fecha');
    $diferencia = abs(strtotime($guardada['fecha']) - time());
    igual(true, $diferencia < 5, 'la fecha debe ser el instante actual');
});

test('fecha: se guarda en hora de Madrid con milisegundos', function () {
    $bd = crearBDdePrueba();
    $laLogica = new Logica($bd, relojFalso('2026-10-13 11:50:14.237'));
    $guardada = $laLogica->guardarMedicion(['dispositivo' => NODO, 'tipo' => 11, 'valor' => 0.1]);
    igual('2026-10-13 11:50:14.237', $bd->query('SELECT fecha FROM medicion')->fetchColumn());
    igual('2026-10-13T11:50:14.237+02:00', $guardada['fecha']);
});

test('fecha: en invierno la diferencia con UTC es +01:00', function () {
    $laLogica = new Logica(crearBDdePrueba(), relojFalso('2026-12-01 09:00:00.000'));
    igual('2026-12-01T09:00:00.000+01:00',
        $laLogica->guardarMedicion(['dispositivo' => NODO, 'tipo' => 12, 'valor' => 5.0])['fecha']);
});

test('leerUltimaMedicion() devuelve la más reciente (por fecha)', function () {
    $laLogica = new Logica(crearBDdePrueba(), relojFalso());
    $laLogica->guardarMedicion(['dispositivo' => NODO, 'tipo' => 11, 'valor' => 0.1]);
    $laLogica->guardarMedicion(['dispositivo' => NODO, 'tipo' => 11, 'valor' => 0.235]);
    $ultima = $laLogica->leerUltimaMedicion();
    igual(0.235, $ultima['valor']);
    igual('2026-10-13T11:50:15.237+02:00', $ultima['fecha']);
});

test('leerUltimaMedicion(tipo) devuelve la última de ese tipo', function () {
    $laLogica = new Logica(crearBDdePrueba(), relojFalso());
    $laLogica->guardarMedicion(['dispositivo' => NODO, 'tipo' => 11, 'valor' => 0.235]);
    $laLogica->guardarMedicion(['dispositivo' => NODO, 'tipo' => 12, 'valor' => 21.5]);
    $laLogica->guardarMedicion(['dispositivo' => NODO, 'tipo' => 12, 'valor' => -3.5]);
    igual(0.235, $laLogica->leerUltimaMedicion(11)['valor']);
    igual(-3.5, $laLogica->leerUltimaMedicion(12)['valor']);
    igual(-3.5, $laLogica->leerUltimaMedicion()['valor']);
    igual(null, $laLogica->leerUltimaMedicion(13));
});

test('clave primaria: mismo dispositivo y misma fecha -> MedicionRepetidaException', function () {
    $relojParado = fn() => '2026-10-13 11:50:14.237';
    $laLogica = new Logica(crearBDdePrueba(), $relojParado);
    $laLogica->guardarMedicion(['dispositivo' => NODO, 'tipo' => 11, 'valor' => 0.235]);
    try {
        $laLogica->guardarMedicion(['dispositivo' => NODO, 'tipo' => 12, 'valor' => 21.5]);
        throw new Exception('debía rechazar la medición repetida');
    } catch (MedicionRepetidaException $esperada) {
    }
    // otro dispositivo en el mismo instante sí se acepta
    igual('OTRO-NODO', $laLogica->guardarMedicion(['dispositivo' => 'OTRO-NODO', 'tipo' => 11, 'valor' => 0.3])['dispositivo']);
});

test('guardarMedicion() rechaza mediciones no válidas y no guarda nada', function () {
    $laLogica = new Logica(crearBDdePrueba());
    lanzaExcepcion(fn() => $laLogica->guardarMedicion(null));
    lanzaExcepcion(fn() => $laLogica->guardarMedicion(['tipo' => 11, 'valor' => 0.2]));                       // sin dispositivo
    lanzaExcepcion(fn() => $laLogica->guardarMedicion(['dispositivo' => '', 'tipo' => 11, 'valor' => 0.2]));
    lanzaExcepcion(fn() => $laLogica->guardarMedicion(['dispositivo' => 'NOMBRE-DE-MAS-DE-16', 'tipo' => 11, 'valor' => 0.2]));
    lanzaExcepcion(fn() => $laLogica->guardarMedicion(['dispositivo' => 7, 'tipo' => 11, 'valor' => 0.2]));
    lanzaExcepcion(fn() => $laLogica->guardarMedicion(['dispositivo' => NODO, 'valor' => 0.2]));
    lanzaExcepcion(fn() => $laLogica->guardarMedicion(['dispositivo' => NODO, 'tipo' => '11', 'valor' => 0.2]));
    lanzaExcepcion(fn() => $laLogica->guardarMedicion(['dispositivo' => NODO, 'tipo' => 300, 'valor' => 0.2]));
    lanzaExcepcion(fn() => $laLogica->guardarMedicion(['dispositivo' => NODO, 'tipo' => 99, 'valor' => 0.2]));  // no es un TipoMedicion
    lanzaExcepcion(fn() => $laLogica->guardarMedicion(['dispositivo' => NODO, 'tipo' => 11, 'valor' => 'abc']));
    igual(null, $laLogica->leerUltimaMedicion());
});

test('los datos no válidos lanzan DatosNoValidosException', function () {
    $laLogica = new Logica(crearBDdePrueba());
    try {
        $laLogica->guardarMedicion(['dispositivo' => NODO, 'tipo' => 99, 'valor' => 1.0]);
        throw new Exception('debía rechazar el tipo 99');
    } catch (DatosNoValidosException $esperada) {
    }
});

test('si falla la BD se lanza ErrorDeAlmacenamientoException', function () {
    $laLogica = new Logica(new PDO('sqlite::memory:')); // BD sin la tabla medicion
    try {
        $laLogica->leerUltimaMedicion();
        throw new Exception('debía lanzar ErrorDeAlmacenamientoException al leer');
    } catch (ErrorDeAlmacenamientoException $esperada) {
    }
    try {
        $laLogica->guardarMedicion(['dispositivo' => NODO, 'tipo' => 11, 'valor' => 0.1]);
        throw new Exception('debía lanzar ErrorDeAlmacenamientoException al guardar');
    } catch (ErrorDeAlmacenamientoException $esperada) {
    }
});

resumenTests();
