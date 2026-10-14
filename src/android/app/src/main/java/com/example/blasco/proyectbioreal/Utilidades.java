// ==============================================================
// Fichero:     Utilidades.java
// Descripción: Conversión de bytes a texto y a entero.
// Diseño:      doc/android_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
package com.example.blasco.proyectbioreal;

public class Utilidades {

    // --------------------------------------------------------------
    // bytes: [N] --> bytesToString() --> Text
    // --------------------------------------------------------------
    public static String bytesToString(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append((char) b);
        }
        return sb.toString();
    }

    // --------------------------------------------------------------
    // bytes: [N]_2 --> bytesToIntOK() --> N   (big endian, sin signo)
    // --------------------------------------------------------------
    public static int bytesToIntOK(byte[] bytes) {
        if (bytes == null || bytes.length < 2) return 0;
        return ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
    }
}
