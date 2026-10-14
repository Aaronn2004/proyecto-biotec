# Prompt 07 — App Android

Adjuntar: `00_contexto_comun.md` + `doc/android_design.md` + `doc/communication_design.md` + el apartado "Protocolo iBeacon" de `doc/arquitectura.md`.

```text
Implementa la app de src/android (Java, minSdk 28, paquete com.example.blasco.proyectbioreal)
según android_design.md.

Requisitos:
- Tipos iguales a los del servidor:
  - TipoMedicion;
  - MedicionNueva (dispositivo, tipo, valor) con desdeTrama() y toJSON();
  - Medicion (dispositivo, tipo, valor, fecha) con desdeJSON().

  Los dos JSON se hacen a mano, sin org.json, para poder probarlos con JUnit.
- TramaIBeacon:
  - busca la cabecera 4C 00 02 15 (no en posiciones fijas);
  - UUID como texto;
  - major, tipo (major >> 8) y contador (major & 0xFF);
  - minor con y sin signo.
- LogicaFake con la MISMA firma que la lógica del negocio: m: MedicionNueva --> guardarMedicion() --> Medicion.
  - Por dentro hace POST /medicion con PeticionarioREST (HttpURLConnection en otro hilo).
  - Resultado por interfaz Respuesta { medicionGuardada(Medicion); error(String) }.
  - URL_SERVIDOR = "https://ablabla.upv.edu.es/biometria/communication/api.php".
- ServicioEscucharBeacons (Service) con los modos TODOS y NUESTRO:
  - en NUESTRO, solo el UUID "AARON-GTI-PBIO-1";
  - FiltroDuplicados por (tipo, contador) para enviar UNA vez cada medida;
  - olvidar() si el envío falla.
- EstadoNodo (Observador, instancia única) con los textos de pantalla. MainActivity lo observa en
  onResume/onPause.
- Tres botones: buscar dispositivos, detener búsqueda, buscar nuestro dispositivo.
- La GUI y el servicio NUNCA hacen HTTP: solo LogicaFake.
- Permisos BLUETOOTH_SCAN/CONNECT y ACCESS_FINE_LOCATION.

Tests JUnit (app/src/test):
- TramaIBeaconTest: tramas reales, temperatura -3.5, JSON.
- LogicaFakeTest: con un servidor HTTP falso (ServerSocket), casos 201 + Medicion, 400 -> error, sin red -> error.
- FiltroDuplicadosTest.
- EstadoNodoTest.
```
