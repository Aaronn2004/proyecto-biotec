// ==============================================================
// Fichero:     LogicaFake.js
// Descripción: Lógica del negocio del cliente web ("fake"/proxy).
//              Ofrece a la GUI la misma función que la lógica del
//              servidor, pero por dentro hace la petición REST.
// Diseño:      doc/frontend_business_logic_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
class LogicaFake {

    // --------------------------------------------------------------
    // urlApi: Text | null --> LogicaFake()
    // Por defecto, la API REST desplegada junto a la web:
    // biometria/gui/index.html  ->  biometria/communication/api.php
    // --------------------------------------------------------------
    constructor(urlApi = null) {
        this.urlApi = urlApi ?? "../communication/api.php"
    }

    // --------------------------------------------------------------
    // tipo: N | null --> leerUltimaMedicion() --> Medicion | null
    // (asíncrona: devuelve una Promise). Sin tipo: la última de cualquier tipo.
    // --------------------------------------------------------------
    async leerUltimaMedicion(tipo = null) {
        const filtro = tipo === null || tipo === undefined ? "" : "?tipo=" + tipo
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

// Permite usar esta clase también desde Node (tests); no es una función
if (typeof module !== "undefined") {
    module.exports = LogicaFake
}
