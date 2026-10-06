package com.example.blasco.proyectbioreal;

import java.util.Locale;

// ==============================================================
// EstadoNodo: lo que la pantalla tiene que mostrar.
// El servicio lo actualiza y la actividad lo observa (patrón
// Observador). Es Java puro: se prueba con JUnit sin móvil.
// ==============================================================
public class EstadoNodo {

    public interface Observador {
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
    // medicion: Medicion, rssi: Z, major: N, minor: N --> registrarMedicion()
    // --------------------------------------------------------------
    public synchronized void registrarMedicion(Medicion medicion, int rssi, int major, int minor) {
        if (medicion.getTipo() == Medicion.TIPO_O3) {
            this.ultimoO3 = medicion.getValor();
        } else if (medicion.getTipo() == Medicion.TIPO_TEMPERATURA) {
            this.ultimaTemperatura = medicion.getValor();
        }
        this.contador = medicion.getContador();
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

    // ---- textos listos para la pantalla ----
    public synchronized String getTextoBluetooth() { return textoBluetooth; }
    public synchronized String getTextoServidor() { return textoServidor; }

    public synchronized String getTextoO3() {
        return ultimoO3 == null ? "O₃: --" : String.format(Locale.US, "O₃: %.3f ppm", ultimoO3);
    }

    public synchronized String getTextoTemperatura() {
        return ultimaTemperatura == null ? "Temperatura: --"
                : String.format(Locale.US, "Temperatura: %.1f °C", ultimaTemperatura);
    }

    public synchronized String getTextoContador() {
        return contador < 0 ? "Contador: --" : "Contador: " + contador;
    }

    public synchronized String getTextoRssi() {
        return contador < 0 ? "RSSI: --" : "RSSI: " + rssi + " dBm";
    }

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
