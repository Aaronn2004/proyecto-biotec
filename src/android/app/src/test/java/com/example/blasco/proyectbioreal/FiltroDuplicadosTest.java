package com.example.blasco.proyectbioreal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FiltroDuplicadosTest {

    @Test
    public void laPrimeraVezEsNuevaYLuegoRepetida() {
        FiltroDuplicados filtro = new FiltroDuplicados();
        assertTrue(filtro.esNueva(Medicion.TIPO_O3, 7));
        assertFalse(filtro.esNueva(Medicion.TIPO_O3, 7));
        assertTrue(filtro.esNueva(Medicion.TIPO_O3, 8));
    }

    @Test
    public void cadaTipoLlevaSuPropioContador() {
        FiltroDuplicados filtro = new FiltroDuplicados();
        assertTrue(filtro.esNueva(Medicion.TIPO_O3, 7));
        assertTrue(filtro.esNueva(Medicion.TIPO_TEMPERATURA, 7)); // mismo contador, otro tipo
        assertFalse(filtro.esNueva(Medicion.TIPO_TEMPERATURA, 7));
    }

    @Test
    public void olvidarPermiteReintentarTrasUnFallo() {
        FiltroDuplicados filtro = new FiltroDuplicados();
        assertTrue(filtro.esNueva(Medicion.TIPO_O3, 7));
        filtro.olvidar(Medicion.TIPO_O3, 7);
        assertTrue(filtro.esNueva(Medicion.TIPO_O3, 7));
    }

    @Test
    public void olvidarUnaMedidaAntiguaNoBorraLaActual() {
        FiltroDuplicados filtro = new FiltroDuplicados();
        filtro.esNueva(Medicion.TIPO_O3, 7);
        filtro.esNueva(Medicion.TIPO_O3, 8);
        filtro.olvidar(Medicion.TIPO_O3, 7); // llega tarde el fallo de la 7
        assertFalse(filtro.esNueva(Medicion.TIPO_O3, 8));
    }
}
