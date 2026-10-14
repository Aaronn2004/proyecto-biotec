<?php
// ==============================================================
// Fichero:     api.php
// Descripción: Comunicación (API REST). Traduce peticiones HTTP a
//              llamadas a la lógica del negocio. Aquí NO hay SQL.
// Diseño:      doc/communication_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
//
//   POST .../api.php/medicion               body: {"dispositivo":"AARON-GTI-PBIO-1","tipo":11,"valor":0.235}
//   GET  .../api.php/medicion/ultima         (última de cualquier tipo)
//   GET  .../api.php/medicion/ultima?tipo=12 (última de ese tipo)
//
// Si el hosting no pasara la ruta tras "api.php", también vale
//   .../api.php?ruta=/medicion/ultima&tipo=12
// ==============================================================

require_once __DIR__ . '/../business_logic/Logica.php';

// --------------------------------------------------------------
// codigo: N, datos: Medicion | Error --> responderJSON()
// Error = ( error: Text )
// --------------------------------------------------------------
function responderJSON(int $codigo, $datos): void {
    http_response_code($codigo);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode($datos, JSON_UNESCAPED_UNICODE | JSON_PRESERVE_ZERO_FRACTION);
}

// --------------------------------------------------------------
// conectarBaseDeDatos() --> ConexionBD
// Lee config.php (no se sube a Git). Para los tests se puede usar
// la variable de entorno PBIO_DSN (por ejemplo una BD SQLite).
// --------------------------------------------------------------
function conectarBaseDeDatos(): PDO {
    $dsnDeTest = getenv('PBIO_DSN');
    if ($dsnDeTest !== false && $dsnDeTest !== '') {
        return new PDO($dsnDeTest);
    }
    $config = require __DIR__ . '/config.php';
    return new PDO($config['dsn'], $config['usuario'], $config['clave']);
}

// --------------------------------------------------------------
// rutaPedida() --> Text   (p. ej. "/medicion/ultima")
// --------------------------------------------------------------
function rutaPedida(): string {
    if (!empty($_GET['ruta'])) {
        return rtrim($_GET['ruta'], '/');
    }
    foreach (['PATH_INFO', 'ORIG_PATH_INFO'] as $variable) {
        if (!empty($_SERVER[$variable])) {
            return rtrim($_SERVER[$variable], '/');
        }
    }
    // Algunos hostings (p. ej. Plesk de la UPV) no rellenan PATH_INFO:
    // sacamos lo que va detrás de "api.php" en la URL pedida.
    $camino = parse_url($_SERVER['REQUEST_URI'] ?? '', PHP_URL_PATH) ?? '';
    $posicion = strpos($camino, basename(__FILE__));
    if ($posicion === false) {
        return '';
    }
    return rtrim(substr($camino, $posicion + strlen(basename(__FILE__))), '/');
}

// ---------------------------- principal ----------------------------
// Atiende la petición según la tabla de communication_design.md.
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Content-Type');
$metodo = $_SERVER['REQUEST_METHOD'];
if ($metodo === 'OPTIONS') {
    http_response_code(204);
    exit;
}

try {
    $laLogica = new Logica(conectarBaseDeDatos());
} catch (Throwable $error) {
    responderJSON(500, ['error' => 'no se pudo conectar con la base de datos']);
    exit;
}

$ruta = rutaPedida();

if ($metodo === 'POST' && $ruta === '/medicion') {
    $medicion = json_decode(file_get_contents('php://input'), true);
    if ($medicion === null) {
        responderJSON(400, ['error' => 'JSON no válido']);
        exit;
    }
    try {
        responderJSON(201, $laLogica->guardarMedicion($medicion));
    } catch (DatosNoValidosException $error) {
        responderJSON(400, ['error' => $error->getMessage()]);
    } catch (MedicionRepetidaException $error) {
        responderJSON(409, ['error' => $error->getMessage()]);
    } catch (Throwable $error) {
        responderJSON(500, ['error' => 'error al guardar en la base de datos']);
    }
    exit;
}

if ($metodo === 'GET' && $ruta === '/medicion/ultima') {
    $textoTipo = $_GET['tipo'] ?? null;
    if ($textoTipo !== null && !ctype_digit($textoTipo)) {
        responderJSON(400, ['error' => 'tipo debe ser un entero >= 0']);
        exit;
    }
    try {
        $ultima = $laLogica->leerUltimaMedicion($textoTipo === null ? null : (int) $textoTipo);
    } catch (Throwable $error) {
        responderJSON(500, ['error' => 'error al leer de la base de datos']);
        exit;
    }
    if ($ultima === null) {
        responderJSON(404, ['error' => 'no hay mediciones']);
    } else {
        responderJSON(200, $ultima);
    }
    exit;
}

responderJSON(404, ['error' => 'ruta no encontrada: ' . $metodo . ' ' . $ruta]);
