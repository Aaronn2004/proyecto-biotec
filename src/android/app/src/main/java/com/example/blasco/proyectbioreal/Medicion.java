package com.example.blasco.proyectbioreal;

import java.util.Locale;

// ==============================================================
// Medicion = (dispositivo: Text, tipo: N, valor: R, contador: N)
//   dispositivo: UUID del iBeacon (p. ej. "AARON-GTI-PBIO-1")
//   contador: solo se usa dentro de la app (filtro de duplicados y
//   pantalla); NO se envía al servidor (la BD solo guarda
//   dispositivo, tipo, valor y fecha).
// ==============================================================
public class Medicion {

    public static final int TIPO_O3 = 11;
    public static final int TIPO_TEMPERATURA = 12;
    public static final int TIPO_RUIDO = 13;

    // El O3 viaja en el minor como ppm * 1000 (sin signo)
    private static final double FACTOR_O3 = 1000.0;
    // La temperatura viaja como décimas de °C (con signo: puede ser negativa)
    private static final double FACTOR_TEMPERATURA = 10.0;

    private final String dispositivo;
    private final int tipo;
    private final double valor;
    private final int contador;

    // --------------------------------------------------------------
    // dispositivo: Text, tipo: N, valor: R, contador: N --> Medicion()
    // --------------------------------------------------------------
    public Medicion(String dispositivo, int tipo, double valor, int contador) {
        this.dispositivo = dispositivo;
        this.tipo = tipo;
        this.valor = valor;
        this.contador = contador;
    }

    // --------------------------------------------------------------
    // trama: TramaIBeacon --> desdeTrama() --> Medicion
    // --------------------------------------------------------------
    public static Medicion desdeTrama(TramaIBeacon trama) {
        int tipo = trama.getTipoMedicion();
        double valor;
        if (tipo == TIPO_O3) {
            valor = trama.getValorEntero() / FACTOR_O3;
        } else if (tipo == TIPO_TEMPERATURA) {
            valor = trama.getValorEnteroConSigno() / FACTOR_TEMPERATURA;
        } else {
            valor = trama.getValorEntero();
        }
        return new Medicion(trama.getUUIDComoTexto(), tipo, valor, trama.getContador());
    }

    // --------------------------------------------------------------
    // toJSON() --> Text   {"dispositivo":"...","tipo":N,"valor":R}
    // (a mano para que se pueda probar con JUnit sin Android; el UUID no
    // necesita escaparse porque el servicio solo crea mediciones de
    // nuestro beacon, "AARON-GTI-PBIO-1")
    // --------------------------------------------------------------
    public String toJSON() {
        return String.format(Locale.US,
                "{\"dispositivo\":\"%s\",\"tipo\":%d,\"valor\":%s}",
                dispositivo, tipo, Double.toString(valor));
    }

    public String getDispositivo() { return dispositivo; }
    public int getTipo() { return tipo; }
    public double getValor() { return valor; }
    public int getContador() { return contador; }

    @Override
    public String toString() {
        return toJSON();
    }
}
