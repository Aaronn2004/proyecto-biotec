// ==============================================================
// Fichero:     Medicion.java
// Descripción: Tipo Medicion (dispositivo, tipo, valor, fecha) que
//              devuelve la lógica del negocio.
// Diseño:      doc/android_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
package com.example.blasco.proyectbioreal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ==============================================================
// Medicion = ( dispositivo: Text, tipo: N, valor: R, fecha: Text )
// Lo que devuelve la lógica del negocio al guardar (igual que en
// business_logic_design.md).
// ==============================================================
public class Medicion {

    private final String dispositivo;
    private final int tipo;
    private final double valor;
    private final String fecha;

    // --------------------------------------------------------------
    // dispositivo: Text, tipo: N, valor: R, fecha: Text --> Medicion()
    // --------------------------------------------------------------
    public Medicion(String dispositivo, int tipo, double valor, String fecha) {
        this.dispositivo = dispositivo;
        this.tipo = tipo;
        this.valor = valor;
        this.fecha = fecha;
    }

    // --------------------------------------------------------------
    // json: Text --> desdeJSON() --> Medicion | null   (null si no se entiende)
    // A mano (sin org.json) para poder probarlo con JUnit sin Android.
    // --------------------------------------------------------------
    public static Medicion desdeJSON(String json) {
        String dispositivo = buscar(json, "\"dispositivo\"\\s*:\\s*\"([^\"]*)\"");
        String tipo = buscar(json, "\"tipo\"\\s*:\\s*(-?\\d+)");
        String valor = buscar(json, "\"valor\"\\s*:\\s*(-?[0-9.eE+-]+)");
        String fecha = buscar(json, "\"fecha\"\\s*:\\s*\"([^\"]*)\"");
        if (dispositivo == null || tipo == null || valor == null || fecha == null) {
            return null;
        }
        return new Medicion(dispositivo, Integer.parseInt(tipo), Double.parseDouble(valor), fecha);
    }

    // --------------------------------------------------------------
    // texto: Text, patron: Text --> buscar() --> Text | null
    // --------------------------------------------------------------
    private static String buscar(String texto, String patron) {
        if (texto == null) {
            return null;
        }
        Matcher encontrado = Pattern.compile(patron).matcher(texto);
        return encontrado.find() ? encontrado.group(1) : null;
    }

    // --------------------------------------------------------------
    // getDispositivo() --> Text
    // --------------------------------------------------------------
    public String getDispositivo() { return dispositivo; }
    // --------------------------------------------------------------
    // getTipo() --> N
    // --------------------------------------------------------------
    public int getTipo() { return tipo; }
    // --------------------------------------------------------------
    // getValor() --> R
    // --------------------------------------------------------------
    public double getValor() { return valor; }
    // --------------------------------------------------------------
    // getFecha() --> Text
    // --------------------------------------------------------------
    public String getFecha() { return fecha; }
}
