// ==============================================================
// Logica.js
// Lógica del negocio: único punto que accede a la base de datos.
// No sabe nada de HTTP (se puede usar desde REST, tests, scripts...).
// ==============================================================
const fs = require("node:fs")
const path = require("node:path")
const { DatabaseSync } = require("node:sqlite")

const RUTA_ESQUEMA = path.join(__dirname, "..", "bd", "esquema.sql")

class Logica {

    // --------------------------------------------------------------
    // rutaBD: Text --> Logica()
    // (":memory:" crea una BD temporal, útil para los tests)
    // --------------------------------------------------------------
    constructor(rutaBD) {
        this.laBD = new DatabaseSync(rutaBD)
        this.laBD.exec(fs.readFileSync(RUTA_ESQUEMA, "utf8"))
    }

    // --------------------------------------------------------------
    // medicion: MedicionNueva --> guardarMedicion() --> Medicion
    //
    // MedicionNueva = (tipo: N, valor: R, contador: N)
    // Medicion      = (id: N, tipo: N, valor: R, contador: N, fecha: Text)
    // --------------------------------------------------------------
    guardarMedicion(medicion) {
        comprobarMedicionNueva(medicion)

        const fecha = new Date().toISOString()
        const resultado = this.laBD
            .prepare("INSERT INTO medicion (tipo, valor, contador, fecha) VALUES (?, ?, ?, ?)")
            .run(medicion.tipo, medicion.valor, medicion.contador, fecha)

        return {
            id: Number(resultado.lastInsertRowid),
            tipo: medicion.tipo,
            valor: medicion.valor,
            contador: medicion.contador,
            fecha: fecha,
        }
    }

    // --------------------------------------------------------------
    // leerUltimaMedicion() --> Medicion | null
    // --------------------------------------------------------------
    leerUltimaMedicion() {
        const fila = this.laBD
            .prepare("SELECT id, tipo, valor, contador, fecha FROM medicion ORDER BY id DESC LIMIT 1")
            .get()
        return fila ? { ...fila } : null
    }

    // --------------------------------------------------------------
    // cerrar() -->
    // --------------------------------------------------------------
    cerrar() {
        this.laBD.close()
    }
}

// --------------------------------------------------------------
// medicion: MedicionNueva --> comprobarMedicionNueva()
// Lanza un Error si algún campo no es válido.
// --------------------------------------------------------------
function comprobarMedicionNueva(medicion) {
    if (medicion === null || typeof medicion !== "object") {
        throw new Error("la medición debe ser un objeto")
    }
    if (!Number.isInteger(medicion.tipo) || medicion.tipo < 0) {
        throw new Error("tipo debe ser un entero >= 0")
    }
    if (typeof medicion.valor !== "number" || !Number.isFinite(medicion.valor)) {
        throw new Error("valor debe ser un número")
    }
    if (!Number.isInteger(medicion.contador) || medicion.contador < 0) {
        throw new Error("contador debe ser un entero >= 0")
    }
}

module.exports = Logica
