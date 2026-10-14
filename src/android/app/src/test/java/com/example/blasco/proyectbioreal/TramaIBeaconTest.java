// ==============================================================
// Fichero:     TramaIBeaconTest.java
// Descripción: Tests JUnit de TramaIBeacon, MedicionNueva y Medicion.
// Diseño:      doc/android_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
package com.example.blasco.proyectbioreal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

// Tests automáticos (JUnit, sin móvil): botón derecho > Run 'Tests in ...'
public class TramaIBeaconTest {

    // --------------------------------------------------------------
    // conFlags: B, uuid: Text, major: N, minor: N --> construirAnuncio() --> [N]
    // Construye los bytes de un anuncio iBeacon como los emite la placa.
    // --------------------------------------------------------------
    static byte[] construirAnuncio(boolean conFlags, String uuid, int major, int minor) {
        byte[] flags = { 0x02, 0x01, 0x06 };
        byte[] cabecera = { 0x1A, (byte) 0xFF, 0x4C, 0x00, 0x02, 0x15 };
        byte[] uuidBytes = uuid.getBytes(StandardCharsets.US_ASCII);
        byte[] resto = { (byte) (major >> 8), (byte) major, (byte) (minor >> 8), (byte) minor, (byte) -53 };

        int inicio = conFlags ? flags.length : 0;
        byte[] anuncio = new byte[inicio + cabecera.length + 16 + resto.length + 10]; // +10: bytes de relleno
        if (conFlags) System.arraycopy(flags, 0, anuncio, 0, flags.length);
        System.arraycopy(cabecera, 0, anuncio, inicio, cabecera.length);
        System.arraycopy(uuidBytes, 0, anuncio, inicio + cabecera.length, 16);
        System.arraycopy(resto, 0, anuncio, inicio + cabecera.length + 16, resto.length);
        return anuncio;
    }

    // --------------------------------------------------------------
    // extraeUUIDMajorMinorConFlags() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void extraeUUIDMajorMinorConFlags() {
        TramaIBeacon trama = new TramaIBeacon(construirAnuncio(true, "AARON-GTI-PBIO-1", (11 << 8) + 7, 235));
        assertTrue(trama.esIBeacon());
        assertEquals("AARON-GTI-PBIO-1", trama.getUUIDComoTexto());
        assertEquals(11, trama.getTipoMedicion());
        assertEquals(7, trama.getContador());
        assertEquals(235, trama.getValorEntero());
        assertEquals(-53, trama.getTxPower());
    }

    // --------------------------------------------------------------
    // funcionaTambienSinFlags() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void funcionaTambienSinFlags() {
        TramaIBeacon trama = new TramaIBeacon(construirAnuncio(false, "AARON-GTI-PBIO-1", (11 << 8) + 200, 65000));
        assertTrue(trama.esIBeacon());
        assertEquals(200, trama.getContador());
        assertEquals(65000, trama.getValorEntero()); // sin signo
    }

    // --------------------------------------------------------------
    // noEsIBeaconSiNoHayCabecera() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void noEsIBeaconSiNoHayCabecera() {
        assertFalse(new TramaIBeacon(new byte[] { 0x02, 0x01, 0x06, 0x05, 0x09, 'H', 'o', 'l', 'a' }).esIBeacon());
        assertFalse(new TramaIBeacon(null).esIBeacon());
        assertFalse(new TramaIBeacon(new byte[0]).esIBeacon());
    }

    // --------------------------------------------------------------
    // medicionDesdeTramaConvierteO3APpm() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void medicionDesdeTramaConvierteO3APpm() {
        TramaIBeacon trama = new TramaIBeacon(construirAnuncio(true, "AARON-GTI-PBIO-1", (11 << 8) + 7, 235));
        MedicionNueva medicion = MedicionNueva.desdeTrama(trama);
        assertEquals("AARON-GTI-PBIO-1", medicion.getDispositivo());
        assertEquals(TipoMedicion.O3, medicion.getTipo());
        assertEquals(0.235, medicion.getValor(), 1e-9);
        assertEquals(7, trama.getContador());
    }

    // --------------------------------------------------------------
    // medicionNuevaToJSONLlevaDispositivoTipoYValor() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void medicionNuevaToJSONLlevaDispositivoTipoYValor() {
        assertEquals("{\"dispositivo\":\"AARON-GTI-PBIO-1\",\"tipo\":11,\"valor\":0.235}",
                new MedicionNueva("AARON-GTI-PBIO-1", 11, 0.235).toJSON());
    }

    // --------------------------------------------------------------
    // medicionDesdeJSONLeeLaRespuestaDelServidor() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void medicionDesdeJSONLeeLaRespuestaDelServidor() {
        Medicion m = Medicion.desdeJSON(
                "{\"dispositivo\":\"AARON-GTI-PBIO-1\",\"tipo\":12,\"valor\":-3.5,\"fecha\":\"2026-10-14T10:45:12.345+02:00\"}");
        assertEquals("AARON-GTI-PBIO-1", m.getDispositivo());
        assertEquals(12, m.getTipo());
        assertEquals(-3.5, m.getValor(), 1e-9);
        assertEquals("2026-10-14T10:45:12.345+02:00", m.getFecha());
    }

    // --------------------------------------------------------------
    // medicionDesdeJSONIncompletoDevuelveNull() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void medicionDesdeJSONIncompletoDevuelveNull() {
        assertNull(Medicion.desdeJSON("{\"error\":\"no hay mediciones\"}"));
        assertNull(Medicion.desdeJSON(null));
    }

    // --------------------------------------------------------------
    // temperaturaNegativaSeDecodificaConSigno() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void temperaturaNegativaSeDecodificaConSigno() {
        // -35 décimas = -3.5 °C ; en 16 bits es 0xFFDD
        TramaIBeacon trama = new TramaIBeacon(construirAnuncio(true, "AARON-GTI-PBIO-1", (12 << 8) + 9, 0xFFDD));
        assertEquals(65501, trama.getValorEntero());
        assertEquals(-35, trama.getValorEnteroConSigno());
        MedicionNueva medicion = MedicionNueva.desdeTrama(trama);
        assertEquals(TipoMedicion.TEMPERATURA, medicion.getTipo());
        assertEquals(-3.5, medicion.getValor(), 1e-9);
        assertEquals(9, trama.getContador());
    }

    // --------------------------------------------------------------
    // temperaturaPositiva() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void temperaturaPositiva() {
        MedicionNueva medicion = MedicionNueva.desdeTrama(
                new TramaIBeacon(construirAnuncio(true, "AARON-GTI-PBIO-1", (12 << 8) + 1, 215)));
        assertEquals(21.5, medicion.getValor(), 1e-9);
    }
}
