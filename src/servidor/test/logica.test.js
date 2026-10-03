// Tests automáticos de la lógica del negocio (sin HTTP).
// Ejecutar: npm test
const { test, beforeEach, afterEach } = require("node:test")
const assert = require("node:assert/strict")
const Logica = require("../logica/Logica")

let laLogica

beforeEach(() => { laLogica = new Logica(":memory:") })
afterEach(() => { laLogica.cerrar() })

test("con la BD vacía, leerUltimaMedicion() devuelve null", () => {
    assert.equal(laLogica.leerUltimaMedicion(), null)
})

test("guardarMedicion() devuelve la medición con id y fecha", () => {
    const guardada = laLogica.guardarMedicion({ tipo: 11, valor: 0.235, contador: 7 })
    assert.equal(guardada.id, 1)
    assert.equal(guardada.tipo, 11)
    assert.equal(guardada.valor, 0.235)
    assert.equal(guardada.contador, 7)
    assert.ok(!Number.isNaN(Date.parse(guardada.fecha)))
})

test("leerUltimaMedicion() devuelve la última guardada", () => {
    laLogica.guardarMedicion({ tipo: 11, valor: 0.1, contador: 1 })
    laLogica.guardarMedicion({ tipo: 11, valor: 0.235, contador: 2 })
    const ultima = laLogica.leerUltimaMedicion()
    assert.equal(ultima.id, 2)
    assert.equal(ultima.valor, 0.235)
    assert.equal(ultima.contador, 2)
})

test("guardarMedicion() rechaza mediciones no válidas", () => {
    assert.throws(() => laLogica.guardarMedicion(null))
    assert.throws(() => laLogica.guardarMedicion({ valor: 0.2, contador: 1 }))
    assert.throws(() => laLogica.guardarMedicion({ tipo: 11, valor: "abc", contador: 1 }))
    assert.throws(() => laLogica.guardarMedicion({ tipo: 11, valor: 0.2, contador: -1 }))
    assert.equal(laLogica.leerUltimaMedicion(), null)
})
