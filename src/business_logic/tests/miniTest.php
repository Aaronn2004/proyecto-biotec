<?php
// ==============================================================
// Fichero:     miniTest.php
// Descripción: Mini librería de tests (sin instalar PHPUnit) y BD de
//              prueba SQLite con la misma tabla que database_design.md.
//                php src/business_logic/tests/LogicaTest.php
//                php src/communication/tests/ApiTest.php
// Diseño:      doc/business_logic_design.md (tests) y doc/database_design.md (tabla)
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================

$GLOBALS['tests_ok'] = 0;
$GLOBALS['tests_fallidos'] = 0;

// --------------------------------------------------------------
// nombre: Text, prueba: Funcion --> test()
// --------------------------------------------------------------
function test(string $nombre, callable $prueba): void {
    try {
        $prueba();
        $GLOBALS['tests_ok']++;
        echo "ok     - $nombre\n";
    } catch (Throwable $error) {
        $GLOBALS['tests_fallidos']++;
        echo "FALLO  - $nombre\n         " . $error->getMessage() . "\n";
    }
}

// --------------------------------------------------------------
// esperado: Valor, obtenido: Valor, mensaje: Text --> igual()   (lanza si no coinciden)
// --------------------------------------------------------------
function igual($esperado, $obtenido, string $mensaje = ''): void {
    if ($esperado !== $obtenido) {
        throw new Exception("$mensaje esperado " . var_export($esperado, true)
            . " pero se obtuvo " . var_export($obtenido, true));
    }
}

// --------------------------------------------------------------
// prueba: Funcion --> lanzaExcepcion()   (falla si prueba() no lanza)
// --------------------------------------------------------------
function lanzaExcepcion(callable $prueba): void {
    try {
        $prueba();
    } catch (Throwable $esperada) {
        return;
    }
    throw new Exception('se esperaba una excepción y no se lanzó');
}

// --------------------------------------------------------------
// resumenTests()   (código de salida 1 si algo falla)
// --------------------------------------------------------------
function resumenTests(): void {
    echo "\n# pass {$GLOBALS['tests_ok']}\n# fail {$GLOBALS['tests_fallidos']}\n";
    exit($GLOBALS['tests_fallidos'] === 0 ? 0 : 1);
}

// --------------------------------------------------------------
// dsn: Text | null --> crearBDdePrueba() --> ConexionBD
// SQLite (en memoria si no se da dsn) con la misma tabla que database_design.md
// --------------------------------------------------------------
function crearBDdePrueba(string $dsn = 'sqlite::memory:'): PDO {
    $bd = new PDO($dsn);
    $bd->exec('CREATE TABLE IF NOT EXISTS medicion (
        dispositivo TEXT NOT NULL CHECK (dispositivo <> \'\'),
        tipo INTEGER NOT NULL CHECK (tipo IN (11, 12, 13)),
        valor REAL NOT NULL, fecha TEXT NOT NULL,
        PRIMARY KEY (dispositivo, fecha))');
    return $bd;
}
