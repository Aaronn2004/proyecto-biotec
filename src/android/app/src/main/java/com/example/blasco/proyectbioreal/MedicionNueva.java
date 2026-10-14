// ==============================================================
// Fichero:     MedicionNueva.java
// Descripción: Tipo MedicionNueva (dispositivo, tipo, valor) que se envía
//              a la lógica del negocio.
// Diseño:      doc/android_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
package com.example.blasco.proyectbioreal;

import java.util.Locale;

// ==============================================================
// MedicionNueva = ( dispositivo: Text, tipo: N, valor: R )
// Lo que el móvil envía a la lógica del negocio (igual que en
// business_logic_design.md). La fecha la pone el servidor.
// ==============================================================
public class MedicionNueva {

    // El O3 viaja en el minor como ppm * 1000 (sin signo)
    private static final double FACTOR_O3 = 1000.0;
    // La temperatura viaja como décimas de °C (con signo: puede ser negativa)
    private static final double FACTOR_TEMPERATURA = 10.0;

    private final String dispositivo;
    private final int tipo;
    private final double valor;

    // --------------------------------------------------------------
    // dispositivo: Text, tipo: N, valor: R --> MedicionNueva()
    // --------------------------------------------------------------
    public MedicionNueva(String dispositivo, int tipo, double valor) {
        this.dispositivo = dispositivo;
        this.tipo = tipo;
        this.valor = valor;
    }

    // --------------------------------------------------------------
    // trama: TramaIBeacon --> desdeTrama() --> MedicionNueva
    //   dispositivo = UUID ; O3 = minor / 1000 ; temperatura = minorConSigno / 10
    // --------------------------------------------------------------
    public static MedicionNueva desdeTrama(TramaIBeacon trama) {
        int tipo = trama.getTipoMedicion();
        double valor;
        if (tipo == TipoMedicion.O3) {
            valor = trama.getValorEntero() / FACTOR_O3;
        } else if (tipo == TipoMedicion.TEMPERATURA) {
            valor = trama.getValorEnteroConSigno() / FACTOR_TEMPERATURA;
        } else {
            valor = trama.getValorEntero();
        }
        return new MedicionNueva(trama.getUUIDComoTexto(), tipo, valor);
    }

    // --------------------------------------------------------------
    // toJSON() --> Text   {"dispositivo":"...","tipo":N,"valor":R}
    // El UUID no necesita escaparse: el servicio solo crea mediciones
    // de nuestro beacon ("AARON-GTI-PBIO-1").
    // --------------------------------------------------------------
    public String toJSON() {
        return String.format(Locale.US,
                "{\"dispositivo\":\"%s\",\"tipo\":%d,\"valor\":%s}",
                dispositivo, tipo, Double.toString(valor));
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
    // toString() --> Text   (el mismo JSON, para el Logcat)
    // --------------------------------------------------------------
    @Override
    public String toString() {
        return toJSON();
    }
}
