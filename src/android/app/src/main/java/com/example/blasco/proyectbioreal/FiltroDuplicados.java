package com.example.blasco.proyectbioreal;

import java.util.HashMap;
import java.util.Map;

// ==============================================================
// FiltroDuplicados: la placa repite el mismo anuncio muchas veces
// por segundo. Solo dejamos pasar una medida por (tipo, contador).
// Cada tipo lleva su propio contador: O3 y temperatura comparten
// el número de muestra pero son medidas distintas.
// ==============================================================
public class FiltroDuplicados {

    private final Map<Integer, Integer> ultimoContadorPorTipo = new HashMap<>();

    // --------------------------------------------------------------
    // tipo: N, contador: N --> esNueva() --> B
    // true la primera vez que llega ese contador para ese tipo
    // (y lo apunta); false si es una repetición.
    // --------------------------------------------------------------
    public synchronized boolean esNueva(int tipo, int contador) {
        Integer ultimo = ultimoContadorPorTipo.get(tipo);
        if (ultimo != null && ultimo == contador) {
            return false;
        }
        ultimoContadorPorTipo.put(tipo, contador);
        return true;
    }

    // --------------------------------------------------------------
    // tipo: N, contador: N --> olvidar()
    // Si el envío falló, olvidamos la medida para reintentarla
    // cuando vuelva a llegar el mismo anuncio.
    // --------------------------------------------------------------
    public synchronized void olvidar(int tipo, int contador) {
        Integer ultimo = ultimoContadorPorTipo.get(tipo);
        if (ultimo != null && ultimo == contador) {
            ultimoContadorPorTipo.remove(tipo);
        }
    }

    // --------------------------------------------------------------
    // reiniciar()
    // --------------------------------------------------------------
    public synchronized void reiniciar() {
        ultimoContadorPorTipo.clear();
    }
}
