// ==============================================================
// LogicaFake.js (web)
// "Fake" porque desde fuera parece la lógica del negocio, pero en
// realidad solo hace de cliente REST contra el servidor.
// ==============================================================
class LogicaFake {

    // --------------------------------------------------------------
    // urlApi: Text --> LogicaFake()
    // "api.php" = la API REST que está en la misma carpeta que esta página
    // --------------------------------------------------------------
    constructor(urlApi = "api.php") {
        this.urlApi = urlApi
    }

    // --------------------------------------------------------------
    // [tipo: N] --> leerUltimaMedicion() --> Medicion | null
    // (asíncrona: devuelve una Promise). Sin tipo: la última de cualquier tipo.
    // --------------------------------------------------------------
    async leerUltimaMedicion(tipo) {
        const filtro = tipo === undefined ? "" : "?tipo=" + tipo
        const respuesta = await fetch(this.urlApi + "/medicion/ultima" + filtro)
        if (respuesta.status === 404) {
            return null
        }
        if (!respuesta.ok) {
            throw new Error("error del servidor: " + respuesta.status)
        }
        return await respuesta.json()
    }
}

// Permite usar esta clase también desde Node (tests)
if (typeof module !== "undefined") {
    module.exports = LogicaFake
}
