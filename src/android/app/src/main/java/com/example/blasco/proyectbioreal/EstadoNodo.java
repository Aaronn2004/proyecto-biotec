// ==============================================================
// Fichero:     EstadoNodo.java
// Descripción: Estado que muestra la pantalla (observable, instancia
//              única).
// Diseño:      doc/android_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
package com.example.blasco.proyectbioreal;

import java.util.Locale;

// ==============================================================
// EstadoNodo: lo que la pantalla tiene que mostrar.
// El servicio lo actualiza y la actividad lo observa (patrón
// Observador). Es Java puro: se prueba con JUnit sin móvil.
// ==============================================================
public class EstadoNodo {

    public interface Observador {
        // ----------------------------------------------------------
        // estado: EstadoNodo --> estadoCambiado()
        // ----------------------------------------------------------
        void estadoCambiado(EstadoNodo estado);
    }

    private static final EstadoNodo instancia = new EstadoNodo();

    private String textoBluetooth = "Bluetooth: sin comprobar";
    private String textoServidor = "Servidor: esperando medidas";
    private Double ultimoO3 = null;
    private Double ultimaTemperatura = null;
    private int contador = -1;
    private int rssi = 0;
    private int major = -1;
    private int minor = -1;
    private Observador elObservador = null;

    // --------------------------------------------------------------
    // getInstancia() --> EstadoNodo   (única para toda la app)
    // --------------------------------------------------------------
    public static EstadoNodo getInstancia() {
        return instancia;
    }

    // --------------------------------------------------------------
    // observador: Observador | null --> observar()
    // --------------------------------------------------------------
    public synchronized void observar(Observador observador) {
        this.elObservador = observador;
        avisar();
    }

    // --------------------------------------------------------------
    // texto: Text --> ponerEstadoBluetooth()
    // --------------------------------------------------------------
    public synchronized void ponerEstadoBluetooth(String texto) {
        this.textoBluetooth = "Bluetooth: " + texto;
        avisar();
    }

    // --------------------------------------------------------------
    // texto: Text --> ponerEstadoServidor()
    // --------------------------------------------------------------
    public synchronized void ponerEstadoServidor(String texto) {
        this.textoServidor = "Servidor: " + texto;
        avisar();
    }

    // --------------------------------------------------------------
    // m: MedicionNueva, contador: N, rssi: Z, major: N, minor: Z --> registrarMedicion()
    // --------------------------------------------------------------
    public synchronized void registrarMedicion(MedicionNueva m, int contador, int rssi, int major, int minor) {
        if (m.getTipo() == TipoMedicion.O3) {
            this.ultimoO3 = m.getValor();
        } else if (m.getTipo() == TipoMedicion.TEMPERATURA) {
            this.ultimaTemperatura = m.getValor();
        }
        this.contador = contador;
        this.rssi = rssi;
        this.major = major;
        this.minor = minor;
        avisar();
    }

    // --------------------------------------------------------------
    // reiniciarMedidas()   (al empezar una búsqueda nueva)
    // --------------------------------------------------------------
    public synchronized void reiniciarMedidas() {
        ultimoO3 = null;
        ultimaTemperatura = null;
        contador = -1;
        rssi = 0;
        major = -1;
        minor = -1;
        textoServidor = "Servidor: esperando medidas";
        avisar();
    }

    // --------------------------------------------------------------
    // getTextoBluetooth() --> Text
    // --------------------------------------------------------------
    public synchronized String getTextoBluetooth() { return textoBluetooth; }

    // --------------------------------------------------------------
    // getTextoServidor() --> Text
    // --------------------------------------------------------------
    public synchronized String getTextoServidor() { return textoServidor; }

    // --------------------------------------------------------------
    // getTextoO3() --> Text   ("O₃: 0.235 ppm")
    // --------------------------------------------------------------
    public synchronized String getTextoO3() {
        return ultimoO3 == null ? "O₃: --" : String.format(Locale.US, "O₃: %.3f ppm", ultimoO3);
    }

    // --------------------------------------------------------------
    // getTextoTemperatura() --> Text   ("Temperatura: 21.5 °C")
    // --------------------------------------------------------------
    public synchronized String getTextoTemperatura() {
        return ultimaTemperatura == null ? "Temperatura: --"
                : String.format(Locale.US, "Temperatura: %.1f °C", ultimaTemperatura);
    }

    // --------------------------------------------------------------
    // getTextoContador() --> Text
    // --------------------------------------------------------------
    public synchronized String getTextoContador() {
        return contador < 0 ? "Contador: --" : "Contador: " + contador;
    }

    // --------------------------------------------------------------
    // getTextoRssi() --> Text
    // --------------------------------------------------------------
    public synchronized String getTextoRssi() {
        return contador < 0 ? "RSSI: --" : "RSSI: " + rssi + " dBm";
    }

    // --------------------------------------------------------------
    // getTextoTrama() --> Text   (major con su tipo y contador, y minor)
    // --------------------------------------------------------------
    public synchronized String getTextoTrama() {
        return major < 0 ? "Major / Minor: --"
                : "Major: " + major + " (tipo " + (major >> 8) + ", contador " + (major & 0xFF) + ") | Minor: " + minor;
    }

    // --------------------------------------------------------------
    // avisar()   (llama al observador, si hay)
    // --------------------------------------------------------------
    private void avisar() {
        if (elObservador != null) {
            elObservador.estadoCambiado(this);
        }
    }
}
