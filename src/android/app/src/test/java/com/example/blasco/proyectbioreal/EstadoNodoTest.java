// ==============================================================
// Fichero:     EstadoNodoTest.java
// Descripción: Tests JUnit de EstadoNodo.
// Diseño:      doc/android_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
package com.example.blasco.proyectbioreal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class EstadoNodoTest {

    private final EstadoNodo estado = EstadoNodo.getInstancia();

    // --------------------------------------------------------------
    // limpiar() -->   (antes de cada test)
    // --------------------------------------------------------------
    @Before
    public void limpiar() {
        estado.observar(null);
        estado.reiniciarMedidas();
    }

    // --------------------------------------------------------------
    // quitarObservador() -->   (después de cada test)
    // --------------------------------------------------------------
    @After
    public void quitarObservador() {
        estado.observar(null);
    }

    // --------------------------------------------------------------
    // sinMedidasMuestraGuiones() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void sinMedidasMuestraGuiones() {
        assertEquals("O₃: --", estado.getTextoO3());
        assertEquals("Temperatura: --", estado.getTextoTemperatura());
        assertEquals("Contador: --", estado.getTextoContador());
        assertEquals("Major / Minor: --", estado.getTextoTrama());
    }

    // --------------------------------------------------------------
    // guardaO3YTemperaturaPorSeparado() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void guardaO3YTemperaturaPorSeparado() {
        estado.registrarMedicion(new MedicionNueva("AARON-GTI-PBIO-1", TipoMedicion.O3, 0.235), 7, -60, (11 << 8) + 7, 235);
        estado.registrarMedicion(new MedicionNueva("AARON-GTI-PBIO-1", TipoMedicion.TEMPERATURA, -3.5), 7, -61, (12 << 8) + 7, -35);
        assertEquals("O₃: 0.235 ppm", estado.getTextoO3());
        assertEquals("Temperatura: -3.5 °C", estado.getTextoTemperatura());
        assertEquals("Contador: 7", estado.getTextoContador());
        assertEquals("RSSI: -61 dBm", estado.getTextoRssi());
        assertEquals("Major: 3079 (tipo 12, contador 7) | Minor: -35", estado.getTextoTrama());
    }

    // --------------------------------------------------------------
    // avisaAlObservadorEnCadaCambio() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void avisaAlObservadorEnCadaCambio() {
        AtomicInteger avisos = new AtomicInteger();
        estado.observar(e -> {
            assertSame(estado, e);
            avisos.incrementAndGet();
        });
        int trasSuscribirse = avisos.get(); // al suscribirse ya avisa una vez
        estado.ponerEstadoBluetooth("buscando");
        estado.ponerEstadoServidor("ok");
        assertEquals(1, trasSuscribirse);
        assertEquals(3, avisos.get());
        assertEquals("Bluetooth: buscando", estado.getTextoBluetooth());
        assertEquals("Servidor: ok", estado.getTextoServidor());
    }
}
