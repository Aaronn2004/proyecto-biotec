// ==============================================================
// servidor.js
// Punto de entrada: arranca el servidor REST y sirve la web.
// Sin dependencias externas (solo Node >= 22.13, no hace falta npm install).
//
//   node servidor.js           (puerto 8080 por defecto)
//   set PUERTO=9000 && node servidor.js   (Windows, otro puerto)
// ==============================================================
const http = require("node:http")
const fs = require("node:fs")
const path = require("node:path")
const Logica = require("./logica/Logica")
const crearReglasREST = require("./apirest/ReglasREST")

const RUTA_BD = path.join(__dirname, "bd", "mediciones.db")
const RUTA_WEB = path.join(__dirname, "..", "web")

const FICHEROS_WEB = {
    "/": { fichero: "index.html", tipo: "text/html; charset=utf-8" },
    "/index.html": { fichero: "index.html", tipo: "text/html; charset=utf-8" },
    "/LogicaFake.js": { fichero: "LogicaFake.js", tipo: "text/javascript; charset=utf-8" },
}

// --------------------------------------------------------------
// laLogica: Logica --> crearServidor() --> http.Server
// --------------------------------------------------------------
function crearServidor(laLogica) {
    const manejarPeticionREST = crearReglasREST(laLogica)

    return http.createServer(async (peticion, respuesta) => {
        console.log(new Date().toISOString(), peticion.method, peticion.url)

        if (await manejarPeticionREST(peticion, respuesta)) {
            return
        }

        const ficheroWeb = FICHEROS_WEB[new URL(peticion.url, "http://servidor").pathname]
        if (peticion.method === "GET" && ficheroWeb) {
            respuesta.writeHead(200, { "Content-Type": ficheroWeb.tipo })
            respuesta.end(fs.readFileSync(path.join(RUTA_WEB, ficheroWeb.fichero)))
            return
        }

        respuesta.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" })
        respuesta.end("no encontrado")
    })
}

if (require.main === module) {
    const puerto = Number(process.env.PUERTO) || 8080
    const laLogica = new Logica(RUTA_BD)
    crearServidor(laLogica).listen(puerto, () => { // sin host: escucha en IPv4 e IPv6 (localhost puede ser ::1)
        console.log(`Servidor escuchando en http://localhost:${puerto}`)
        console.log("Desde el móvil usa la IP de este PC (misma red WiFi): http://<IP-del-PC>:" + puerto)
    })
}

module.exports = crearServidor
