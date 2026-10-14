// ==============================================================
// Fichero:     LogicaFakeTest.java
// Descripción: Tests JUnit de LogicaFake contra un servidor HTTP falso.
// Diseño:      doc/android_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
package com.example.blasco.proyectbioreal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

// Test automático de LogicaFake + PeticionarioREST contra un servidor
// HTTP falso levantado en el propio test (no hace falta el servidor real).
public class LogicaFakeTest {

    private static final String NODO = "AARON-GTI-PBIO-1";

    private ServerSocket servidorFalso;
    private final AtomicReference<String> cuerpoRecibido = new AtomicReference<>();
    private final AtomicReference<String> rutaRecibida = new AtomicReference<>();

    // Lo que llega a la Respuesta de LogicaFake
    private final AtomicReference<Medicion> medicionRecibida = new AtomicReference<>();
    private final AtomicReference<String> errorRecibido = new AtomicReference<>();
    private final CountDownLatch terminado = new CountDownLatch(1);

    // --------------------------------------------------------------
    // codigo: N, cuerpo: Text --> arrancarServidorFalso() --> url: Text
    // Atiende UNA petición y responde con ese código y ese cuerpo.
    // --------------------------------------------------------------
    private String arrancarServidorFalso(int codigo, String respuestaJSON) throws Exception {
        servidorFalso = new ServerSocket(0);
        Thread hilo = new Thread(() -> {
            try (Socket cliente = servidorFalso.accept()) {
                BufferedReader entrada = new BufferedReader(
                        new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8));
                String primeraLinea = entrada.readLine(); // "POST /medicion HTTP/1.1"
                rutaRecibida.set(primeraLinea.substring(0, primeraLinea.lastIndexOf(' ')));

                int longitud = 0;
                String linea;
                while ((linea = entrada.readLine()) != null && !linea.isEmpty()) {
                    if (linea.toLowerCase().startsWith("content-length:")) {
                        longitud = Integer.parseInt(linea.substring(15).trim());
                    }
                }
                char[] cuerpo = new char[longitud];
                int leidos = 0;
                while (leidos < longitud) {
                    leidos += entrada.read(cuerpo, leidos, longitud - leidos);
                }
                cuerpoRecibido.set(new String(cuerpo));

                byte[] bytesRespuesta = respuestaJSON.getBytes(StandardCharsets.UTF_8);
                String cabecera = "HTTP/1.1 " + codigo + " X\r\nContent-Type: application/json\r\n"
                        + "Content-Length: " + bytesRespuesta.length + "\r\nConnection: close\r\n\r\n";
                OutputStream salida = cliente.getOutputStream();
                salida.write(cabecera.getBytes(StandardCharsets.UTF_8));
                salida.write(bytesRespuesta);
                salida.flush();
            } catch (Exception ignorada) {
                // el servidor se cierra al acabar el test
            }
        });
        hilo.setDaemon(true);
        hilo.start();
        return "http://127.0.0.1:" + servidorFalso.getLocalPort();
    }

    // --------------------------------------------------------------
    // url: Text, m: MedicionNueva --> guardarYEsperar()
    // --------------------------------------------------------------
    private void guardarYEsperar(String url, MedicionNueva m) throws Exception {
        new LogicaFake(url).guardarMedicion(m, new LogicaFake.Respuesta() {
            // ------------------------------------------------------
            // guardada: Medicion --> medicionGuardada()
            // ------------------------------------------------------
            @Override
            public void medicionGuardada(Medicion guardada) {
                medicionRecibida.set(guardada);
                terminado.countDown();
            }

            // ------------------------------------------------------
            // motivo: Text --> error()
            // ------------------------------------------------------
            @Override
            public void error(String motivo) {
                errorRecibido.set(motivo);
                terminado.countDown();
            }
        });
        assertTrue("no respondió a tiempo", terminado.await(10, TimeUnit.SECONDS));
    }

    // --------------------------------------------------------------
    // pararServidorFalso() -->   (después de cada test)
    // --------------------------------------------------------------
    @After
    public void pararServidorFalso() throws Exception {
        if (servidorFalso != null) {
            servidorFalso.close();
        }
    }

    // --------------------------------------------------------------
    // guardarMedicionHacePOSTYDevuelveLaMedicionGuardada() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void guardarMedicionHacePOSTYDevuelveLaMedicionGuardada() throws Exception {
        String url = arrancarServidorFalso(201,
                "{\"dispositivo\":\"" + NODO + "\",\"tipo\":11,\"valor\":0.235,\"fecha\":\"2026-10-14T10:45:12.345+02:00\"}");

        guardarYEsperar(url, new MedicionNueva(NODO, 11, 0.235));

        assertEquals("POST /medicion", rutaRecibida.get());
        assertEquals("{\"dispositivo\":\"" + NODO + "\",\"tipo\":11,\"valor\":0.235}", cuerpoRecibido.get());
        assertNull(errorRecibido.get());
        assertEquals(NODO, medicionRecibida.get().getDispositivo());
        assertEquals(0.235, medicionRecibida.get().getValor(), 1e-9);
        assertEquals("2026-10-14T10:45:12.345+02:00", medicionRecibida.get().getFecha());
    }

    // --------------------------------------------------------------
    // siElServidorRechazaLaMedicionAvisaDelError() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void siElServidorRechazaLaMedicionAvisaDelError() throws Exception {
        String url = arrancarServidorFalso(400, "{\"error\":\"tipo debe ser 11, 12 o 13\"}");

        guardarYEsperar(url, new MedicionNueva(NODO, 99, 1.0));

        assertNull(medicionRecibida.get());
        assertTrue(errorRecibido.get().startsWith("400"));
    }

    // --------------------------------------------------------------
    // sinServidorAvisaDelError() -->   (test)
    // --------------------------------------------------------------
    @Test
    public void sinServidorAvisaDelError() throws Exception {
        guardarYEsperar("http://127.0.0.1:1", new MedicionNueva(NODO, 11, 0.1));

        assertNull(medicionRecibida.get());
        assertTrue(errorRecibido.get().startsWith("-1"));
    }
}
