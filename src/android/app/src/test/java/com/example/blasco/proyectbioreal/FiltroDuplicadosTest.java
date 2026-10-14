// ==============================================================
// Fichero:     FiltroDuplicadosTest.java
// Descripción: Tests JUnit de FiltroDuplicados.
// Diseño:      doc/android_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
package com.example.blasco.proyectbioreal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FiltroDuplicadosTest {

    // --------------------------------------------------------------
    // laPrimeraVezEsNuevaYLuegoRepetida() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void laPrimeraVezEsNuevaYLuegoRepetida() {
        FiltroDuplicados filtro = new FiltroDuplicados();
        assertTrue(filtro.esNueva(TipoMedicion.O3, 7));
        assertFalse(filtro.esNueva(TipoMedicion.O3, 7));
        assertTrue(filtro.esNueva(TipoMedicion.O3, 8));
    }

    // --------------------------------------------------------------
    // cadaTipoLlevaSuPropioContador() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void cadaTipoLlevaSuPropioContador() {
        FiltroDuplicados filtro = new FiltroDuplicados();
        assertTrue(filtro.esNueva(TipoMedicion.O3, 7));
        assertTrue(filtro.esNueva(TipoMedicion.TEMPERATURA, 7)); // mismo contador, otro tipo
        assertFalse(filtro.esNueva(TipoMedicion.TEMPERATURA, 7));
    }

    // --------------------------------------------------------------
    // olvidarPermiteReintentarTrasUnFallo() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void olvidarPermiteReintentarTrasUnFallo() {
        FiltroDuplicados filtro = new FiltroDuplicados();
        assertTrue(filtro.esNueva(TipoMedicion.O3, 7));
        filtro.olvidar(TipoMedicion.O3, 7);
        assertTrue(filtro.esNueva(TipoMedicion.O3, 7));
    }

    // --------------------------------------------------------------
    // olvidarUnaMedidaAntiguaNoBorraLaActual() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void olvidarUnaMedidaAntiguaNoBorraLaActual() {
        FiltroDuplicados filtro = new FiltroDuplicados();
        filtro.esNueva(TipoMedicion.O3, 7);
        filtro.esNueva(TipoMedicion.O3, 8);
        filtro.olvidar(TipoMedicion.O3, 7); // llega tarde el fallo de la 7
        assertFalse(filtro.esNueva(TipoMedicion.O3, 8));
    }
}
