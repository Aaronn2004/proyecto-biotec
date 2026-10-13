<?php
// Tests automáticos de la API REST (integración): arranca el servidor
// web de PHP con una BD SQLite temporal y le hace peticiones HTTP reales.
// Ejecutar desde src/servidor:   php tests/ApiTest.php
require_once __DIR__ . '/miniTest.php';

$ficheroBD = tempnam(sys_get_temp_dir(), 'pbio') . '.sqlite';
crearBDdePrueba('sqlite:' . $ficheroBD);

$puerto = 8765;
$url = "http://127.0.0.1:$puerto/api.php";
$comando = sprintf('PBIO_DSN=%s php -S 127.0.0.1:%d -t %s > /dev/null 2>&1 & echo $!',
    escapeshellarg('sqlite:' . $ficheroBD), $puerto, escapeshellarg(dirname(__DIR__)));
if (stripos(PHP_OS, 'WIN') === 0) {
    echo "Este test de integración usa comandos de Linux/macOS; en Windows ejecuta solo LogicaTest.php\n";
    exit(0);
}
$pid = (int) shell_exec($comando);
register_shutdown_function(function () use ($pid, $ficheroBD) {
    exec("kill $pid");
    @unlink($ficheroBD);
});
usleep(500000);

// --------------------------------------------------------------
// metodo: Text, url: Text, [cuerpo: Text] --> peticion() --> (codigo: N, json)
// --------------------------------------------------------------
function peticion(string $metodo, string $url, ?string $cuerpo = null): array {
    $contexto = stream_context_create(['http' => [
        'method' => $metodo,
        'header' => "Content-Type: application/json\r\n",
        'content' => $cuerpo ?? '',
        'ignore_errors' => true,
    ]]);
    $respuesta = file_get_contents($url, false, $contexto);
    preg_match('/\s(\d{3})\s/', $http_response_header[0], $partes);
    return [(int) $partes[1], json_decode($respuesta, true)];
}

test('GET /medicion/ultima sin datos -> 404', function () use ($url) {
    igual(404, peticion('GET', "$url/medicion/ultima")[0]);
});

test('POST /medicion válida -> 201 y la devuelve con dispositivo y fecha', function () use ($url) {
    [$codigo, $cuerpo] = peticion('POST', "$url/medicion", '{"dispositivo":"AARON-GTI-PBIO-1","tipo":11,"valor":0.235}');
    igual(201, $codigo);
    igual('AARON-GTI-PBIO-1', $cuerpo['dispositivo']);
    igual(0.235, $cuerpo['valor']);
    igual(true, isset($cuerpo['fecha']));
    usleep(5000); // la clave es (dispositivo, fecha): que la siguiente no caiga en el mismo milisegundo
});

test('POST /medicion no válida -> 400', function () use ($url) {
    igual(400, peticion('POST', "$url/medicion", '{"tipo":11,"valor":0.2}')[0]); // sin dispositivo
    igual(400, peticion('POST', "$url/medicion", '{esto no es json')[0]);
});

test('GET /medicion/ultima?tipo=N filtra por tipo; tipo no numérico -> 400', function () use ($url) {
    peticion('POST', "$url/medicion", '{"dispositivo":"AARON-GTI-PBIO-1","tipo":12,"valor":21.5}');
    igual(0.235, peticion('GET', "$url/medicion/ultima?tipo=11")[1]['valor']);
    igual(21.5, peticion('GET', "$url/medicion/ultima?tipo=12")[1]['valor']);
    igual(21.5, peticion('GET', "$url/medicion/ultima")[1]['valor']);
    igual(404, peticion('GET', "$url/medicion/ultima?tipo=13")[0]);
    igual(400, peticion('GET', "$url/medicion/ultima?tipo=abc")[0]);
});

test('Ruta alternativa ?ruta= (por si el hosting no pasa la ruta tras api.php)', function () use ($url) {
    igual(21.5, peticion('GET', "$url?ruta=/medicion/ultima&tipo=12")[1]['valor']);
});

test('Ruta desconocida -> 404', function () use ($url) {
    igual(404, peticion('GET', "$url/otra/cosa")[0]);
});

resumenTests();
