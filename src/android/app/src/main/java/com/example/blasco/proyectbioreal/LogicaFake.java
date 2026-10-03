package com.example.blasco.proyectbioreal;

// ==============================================================
// LogicaFake (móvil): desde fuera parece la lógica del negocio,
// pero solo hace de cliente REST contra el servidor.
// ==============================================================
public class LogicaFake {

    // >>> CAMBIA ESTO por la IP de tu PC en la red WiFi (ipconfig en Windows) <<<
    // El móvil y el PC tienen que estar en la misma red.
    // (En el emulador de Android, el PC es 10.0.2.2, pero el emulador no tiene BLE.)
    public static final String URL_SERVIDOR = "http://192.168.1.100:8080";

    public interface Resultado {
        void callback(boolean exito, String detalle);
    }

    private final String urlServidor;
    private final PeticionarioREST elPeticionario = new PeticionarioREST();

    // --------------------------------------------------------------
    // urlServidor: Text --> LogicaFake()
    // --------------------------------------------------------------
    public LogicaFake(String urlServidor) {
        this.urlServidor = urlServidor;
    }

    // --------------------------------------------------------------
    // medicion: Medicion --> guardarMedicion() --> exito: B, detalle: Text  (por callback)
    // --------------------------------------------------------------
    public void guardarMedicion(Medicion medicion, Resultado resultado) {
        elPeticionario.hacerPeticionREST("POST", urlServidor + "/medicion", medicion.toJSON(),
                (codigo, cuerpo) -> resultado.callback(codigo == 201, codigo + " " + cuerpo));
    }
}
