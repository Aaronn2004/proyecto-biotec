// ==============================================================
// ReglasREST.js
// API REST: traduce peticiones HTTP a llamadas a la lógica del
// negocio. Aquí NO hay SQL.
//
//   POST /medicion          body: {"tipo":11,"valor":0.235,"contador":7}
//   GET  /medicion/ultima            (última de cualquier tipo)
//   GET  /medicion/ultima?tipo=12    (última de ese tipo)
// ==============================================================

// --------------------------------------------------------------
// laLogica: Logica --> crearReglasREST() --> ManejadorREST
//
// ManejadorREST = (peticion, respuesta) --> B
//   (true si la ruta era de la API y ya se ha respondido)
// --------------------------------------------------------------
function crearReglasREST(laLogica) {

    return async function manejarPeticionREST(peticion, respuesta) {

        const url = new URL(peticion.url, "http://servidor")

        if (peticion.method === "POST" && url.pathname === "/medicion") {
            let medicion
            try {
                medicion = JSON.parse(await leerCuerpo(peticion))
            } catch (error) {
                responderJSON(respuesta, 400, { error: "JSON no válido" })
                return true
            }
            try {
                responderJSON(respuesta, 201, laLogica.guardarMedicion(medicion))
            } catch (error) {
                responderJSON(respuesta, 400, { error: error.message })
            }
            return true
        }

        if (peticion.method === "GET" && url.pathname === "/medicion/ultima") {
            const textoTipo = url.searchParams.get("tipo")
            if (textoTipo !== null && !/^\d+$/.test(textoTipo)) {
                responderJSON(respuesta, 400, { error: "tipo debe ser un entero >= 0" })
                return true
            }
            const ultima = laLogica.leerUltimaMedicion(textoTipo === null ? undefined : Number(textoTipo))
            if (ultima === null) {
                responderJSON(respuesta, 404, { error: "no hay mediciones" })
            } else {
                responderJSON(respuesta, 200, ultima)
            }
            return true
        }

        return false
    }
}

// --------------------------------------------------------------
// peticion --> leerCuerpo() --> Text
// --------------------------------------------------------------
function leerCuerpo(peticion) {
    return new Promise((resolver, rechazar) => {
        let cuerpo = ""
        peticion.setEncoding("utf8")
        peticion.on("data", (trozo) => { cuerpo += trozo })
        peticion.on("end", () => resolver(cuerpo))
        peticion.on("error", rechazar)
    })
}

// --------------------------------------------------------------
// respuesta, codigo: N, objeto --> responderJSON()
// --------------------------------------------------------------
function responderJSON(respuesta, codigo, objeto) {
    respuesta.writeHead(codigo, { "Content-Type": "application/json; charset=utf-8" })
    respuesta.end(JSON.stringify(objeto))
}

module.exports = crearReglasREST
