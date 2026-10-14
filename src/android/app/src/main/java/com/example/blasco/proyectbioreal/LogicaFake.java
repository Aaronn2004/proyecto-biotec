// ==============================================================
// Fichero:     LogicaFake.java
// Descripción: Lógica del negocio del cliente móvil ("fake"/proxy): misma
//              firma que la del servidor, por dentro hace POST /medicion.
// Diseño:      doc/android_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
package com.example.blasco.proyectbioreal;

// ==============================================================
// LogicaFake (móvil): ofrece la MISMA función que la lógica del
// negocio del servidor,
//     m: MedicionNueva --> guardarMedicion() --> Medicion
// pero por dentro hace una petición REST (POST /medicion).
// ==============================================================
public class LogicaFake {

    // Dirección de api.php en el hosting Plesk de la UPV
    public static final String URL_SERVIDOR = "https://ablabla.upv.edu.es/biometria/communication/api.php";

    // Cómo llega el resultado (Android no deja esperar a la red en el
    // hilo principal): o la Medicion guardada, o el motivo del error
    // (equivale a la excepción que lanza la lógica del servidor).
    public interface Respuesta {
        // ----------------------------------------------------------
        // guardada: Medicion --> medicionGuardada()
        // ----------------------------------------------------------
        void medicionGuardada(Medicion guardada);

        // ----------------------------------------------------------
        // motivo: Text --> error()
        // ----------------------------------------------------------
        void error(String motivo);
    }

    private final String urlServidor;
    private final PeticionarioREST elPeticionario = new PeticionarioREST();

    // --------------------------------------------------------------
    // urlServidor: Text --> LogicaFake()
    // --------------------------------------------------------------
    public LogicaFake(String urlServidor) {
        this.urlServidor = urlServidor;
    }

    // --------------------------------------------------------------
    // m: MedicionNueva --> guardarMedicion() --> Medicion
    // (por Respuesta; si falla, Respuesta.error(motivo))
    // --------------------------------------------------------------
    public void guardarMedicion(MedicionNueva m, Respuesta respuesta) {
        elPeticionario.hacerPeticionREST("POST", urlServidor + "/medicion", m.toJSON(),
                (codigo, cuerpo) -> {
                    Medicion guardada = codigo == 201 ? Medicion.desdeJSON(cuerpo) : null;
                    if (guardada != null) {
                        respuesta.medicionGuardada(guardada);
                    } else {
                        respuesta.error(codigo + " " + cuerpo);
                    }
                });
    }
}
