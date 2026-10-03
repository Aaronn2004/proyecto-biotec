// Tests automáticos de la API REST + LogicaFake de la web (integración).
// Arranca el servidor en un puerto libre con una BD en memoria.
const { test, before, after } = require("node:test")
const assert = require("node:assert/strict")
const Logica = require("../logica/Logica")
const crearServidor = require("../servidor")
const LogicaFake = require("../../web/LogicaFake")

let laLogica, servidor, url

before(async () => {
    laLogica = new Logica(":memory:")
    servidor = crearServidor(laLogica).listen(0)
    await new Promise((resolver) => servidor.once("listening", resolver))
    url = "http://127.0.0.1:" + servidor.address().port
})

after(() => {
    servidor.close()
    laLogica.cerrar()
})

function post(cuerpo) {
    return fetch(url + "/medicion", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: typeof cuerpo === "string" ? cuerpo : JSON.stringify(cuerpo),
    })
}

test("GET /medicion/ultima sin datos -> 404", async () => {
    const respuesta = await fetch(url + "/medicion/ultima")
    assert.equal(respuesta.status, 404)
})

test("LogicaFake web: sin datos devuelve null", async () => {
    assert.equal(await new LogicaFake(url).leerUltimaMedicion(), null)
})

test("POST /medicion válida -> 201 y la devuelve con id", async () => {
    const respuesta = await post({ tipo: 11, valor: 0.235, contador: 3 })
    assert.equal(respuesta.status, 201)
    const cuerpo = await respuesta.json()
    assert.equal(cuerpo.valor, 0.235)
    assert.ok(cuerpo.id > 0)
})

test("POST /medicion no válida -> 400", async () => {
    assert.equal((await post({ tipo: 11 })).status, 400)
    assert.equal((await post("{esto no es json")).status, 400)
})

test("Flujo completo: POST y luego LogicaFake web lee la misma medición", async () => {
    await post({ tipo: 11, valor: 0.321, contador: 4 })
    const ultima = await new LogicaFake(url).leerUltimaMedicion()
    assert.equal(ultima.valor, 0.321)
    assert.equal(ultima.contador, 4)
    assert.equal(ultima.tipo, 11)
})

test("La web (index.html) se sirve en /", async () => {
    const respuesta = await fetch(url + "/")
    assert.equal(respuesta.status, 200)
    assert.match(await respuesta.text(), /Última medición/)
})
