// ==============================================================
// Fichero:     TipoMedicion.java
// Descripción: Enumeración TipoMedicion = { O3 = 11, TEMPERATURA = 12,
//              RUIDO = 13 }.
// Diseño:      doc/android_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
package com.example.blasco.proyectbioreal;

// ==============================================================
// TipoMedicion = { O3 = 11, TEMPERATURA = 12, RUIDO = 13 }
// (mismos valores que la columna tipo de la tabla medicion)
// ==============================================================
public final class TipoMedicion {

    public static final int O3 = 11;
    public static final int TEMPERATURA = 12;
    public static final int RUIDO = 13;

    // --------------------------------------------------------------
    // TipoMedicion()   (privado: solo contiene constantes)
    // --------------------------------------------------------------
    private TipoMedicion() {
    }
}
