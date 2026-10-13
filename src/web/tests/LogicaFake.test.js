// Tests automáticos de la LogicaFake de la web (sin servidor: se simula fetch).
// Ejecutar desde src/web:   node --test
const { test } = require("node:test")
const assert = require("node:assert/strict")
const LogicaFake = require("../LogicaFake")

// --------------------------------------------------------------
// codigo: N, cuerpo --> simularFetch() --> urlsPedidas: [Text]
// Sustituye fetch por uno falso que siempre responde lo indicado.
// --------------------------------------------------------------
function simularFetch(codigo, cuerpo) {
    const urlsPedidas = []
    global.fetch = async (url) => {
        urlsPedidas.push(url)
        return { status: codigo, ok: codigo >= 200 && codigo < 300, json: async () => cuerpo }
    }
    return urlsPedidas
}

test("leerUltimaMedicion(tipo) pide /medicion/ultima?tipo=N a la API", async () => {
    const urls = simularFetch(200, { dispositivo: "AARON-GTI-PBIO-1", tipo: 12, valor: 21.5, fecha: "2026-10-13T11:00:00.000+02:00" })
    const medicion = await new LogicaFake("https://ejemplo.upv.edu.es/biometria/api.php").leerUltimaMedicion(12)
    assert.equal(medicion.valor, 21.5)
    assert.equal(medicion.dispositivo, "AARON-GTI-PBIO-1")
    assert.deepEqual(urls, ["https://ejemplo.upv.edu.es/biometria/api.php/medicion/ultima?tipo=12"])
})

test("sin tipo pide la última de cualquier tipo; por defecto usa api.php relativo", async () => {
    const urls = simularFetch(200, { dispositivo: "AARON-GTI-PBIO-1", tipo: 11, valor: 0.235, fecha: "x" })
    await new LogicaFake().leerUltimaMedicion()
    assert.deepEqual(urls, ["api.php/medicion/ultima"])
})

test("404 (no hay mediciones) devuelve null", async () => {
    simularFetch(404, { error: "no hay mediciones" })
    assert.equal(await new LogicaFake().leerUltimaMedicion(11), null)
})

test("error del servidor (500) lanza una excepción", async () => {
    simularFetch(500, { error: "no se pudo conectar con la base de datos" })
    await assert.rejects(() => new LogicaFake().leerUltimaMedicion(11))
})
