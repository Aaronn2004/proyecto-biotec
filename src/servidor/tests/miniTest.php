<?php
// ==============================================================
// miniTest.php: mini librería de tests (sin instalar PHPUnit).
//   php tests/LogicaTest.php
//   php tests/ApiTest.php
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
// esperado, obtenido, [mensaje: Text] --> igual()   (lanza si no coinciden)
// --------------------------------------------------------------
function igual($esperado, $obtenido, string $mensaje = ''): void {
    if ($esperado !== $obtenido) {
        throw new Exception("$mensaje esperado " . var_export($esperado, true)
            . " pero se obtuvo " . var_export($obtenido, true));
    }
}

// --------------------------------------------------------------
// funcion: Funcion --> lanzaExcepcion()
// --------------------------------------------------------------
function lanzaExcepcion(callable $funcion): void {
    try {
        $funcion();
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
// --> crearBDdePrueba() --> PDO   (SQLite en memoria con la misma tabla)
// --------------------------------------------------------------
function crearBDdePrueba(string $dsn = 'sqlite::memory:'): PDO {
    $bd = new PDO($dsn);
    $bd->exec('CREATE TABLE IF NOT EXISTS medicion (
        dispositivo TEXT NOT NULL, tipo INTEGER NOT NULL,
        valor REAL NOT NULL, fecha TEXT NOT NULL,
        PRIMARY KEY (dispositivo, fecha))');
    return $bd;
}
