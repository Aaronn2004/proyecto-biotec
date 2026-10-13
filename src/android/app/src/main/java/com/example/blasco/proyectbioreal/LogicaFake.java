package com.example.blasco.proyectbioreal;

// ==============================================================
// LogicaFake (móvil): desde fuera parece la lógica del negocio,
// pero solo hace de cliente REST contra el servidor.
// ==============================================================
public class LogicaFake {

    // Dirección de api.php en el hosting Plesk de la UPV
    // Es la dirección de api.php en tu hosting (la misma carpeta que la web).
    // Al estar en internet, el móvil no tiene que estar en la misma WiFi que el PC.
    public static final String URL_SERVIDOR = "https://ablabla.upv.edu.es/biometria/api.php";

    public interface Resultado {
        void callback(boolean exito, String detalle);
    }

    private final String urlServidor;
    private final PeticionarioREST elPeticionario = new PeticionarioREST();

    // --------------------------------------------------------------
    // urlServidor: Text (dirección de api.php) --> LogicaFake()
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
