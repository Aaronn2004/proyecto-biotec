package com.example.blasco.proyectbioreal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

// ==============================================================
// ServicioEscuharBeacons: escanea BLE en segundo plano.
//
// Modos (extra MODO del Intent):
//   MODO_TODOS   -> muestra en el Log todos los dispositivos BLE
//   MODO_NUESTRO -> solo nuestro iBeacon; cada medida NUEVA (por tipo
//                   y contador) se envía al servidor con
//                   LogicaFake.guardarMedicion(). Lo que pasa se
//                   publica en EstadoNodo para que lo pinte la pantalla.
// ==============================================================
public class ServicioEscuharBeacons extends Service {

    public static final String EXTRA_MODO = "MODO";
    public static final String MODO_TODOS = "TODOS";
    public static final String MODO_NUESTRO = "NUESTRO";

    // Debe coincidir EXACTAMENTE con beaconUUID de Publicador.h (Arduino)
    public static final String UUID_NUESTRO_BEACON = "AARON-GTI-PBIO-1";

    private static final String ETIQUETA_LOG = ">>>>";

    private BluetoothLeScanner elEscaner;
    private ScanCallback elCallbackDelEscaner;
    private String modoActual = null;

    private final LogicaFake laLogica = new LogicaFake(LogicaFake.URL_SERVIDOR);
    private final FiltroDuplicados elFiltro = new FiltroDuplicados();
    private final EstadoNodo elEstado = EstadoNodo.getInstancia();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String modo = (intent != null && intent.getStringExtra(EXTRA_MODO) != null)
                ? intent.getStringExtra(EXTRA_MODO) : MODO_TODOS;
        Log.d(ETIQUETA_LOG, "ServicioEscuharBeacons.onStartCommand(): modo = " + modo);

        detenerEscaneo(); // si ya escaneaba en otro modo, se reinicia
        arrancarEscaneo(modo);
        return START_NOT_STICKY;
    }

    // --------------------------------------------------------------
    // obtenerEscaner() --> BluetoothLeScanner | null
    // --------------------------------------------------------------
    private BluetoothLeScanner obtenerEscaner() {
        BluetoothManager gestor = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adaptador = gestor == null ? null : gestor.getAdapter();
        if (adaptador == null) {
            Log.e(ETIQUETA_LOG, "Este dispositivo no tiene Bluetooth (¿emulador?)");
            elEstado.ponerEstadoBluetooth("no disponible (¿emulador?)");
            return null;
        }
        if (!adaptador.isEnabled()) {
            Log.e(ETIQUETA_LOG, "El Bluetooth está apagado");
            elEstado.ponerEstadoBluetooth("apagado");
            return null;
        }
        return adaptador.getBluetoothLeScanner();
    }

    // --------------------------------------------------------------
    // tienePermisoEscanear() --> B
    // --------------------------------------------------------------
    private boolean tienePermisoEscanear() {
        String permiso = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? Manifest.permission.BLUETOOTH_SCAN
                : Manifest.permission.ACCESS_FINE_LOCATION;
        return ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED;
    }

    // --------------------------------------------------------------
    // modo: Text --> arrancarEscaneo()
    // --------------------------------------------------------------
    @SuppressLint("MissingPermission") // se comprueba en tienePermisoEscanear()
    private void arrancarEscaneo(String modo) {
        if (!tienePermisoEscanear()) {
            Log.e(ETIQUETA_LOG, "Faltan permisos para escanear BLE");
            elEstado.ponerEstadoBluetooth("faltan permisos");
            return;
        }
        elEscaner = obtenerEscaner();
        if (elEscaner == null) {
            Log.e(ETIQUETA_LOG, "No se pudo obtener el escáner BLE");
            return;
        }

        modoActual = modo;
        elFiltro.reiniciar();
        elEstado.reiniciarMedidas();

        elCallbackDelEscaner = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                if (MODO_NUESTRO.equals(modoActual)) {
                    procesarSiEsNuestroBeacon(resultado);
                } else {
                    mostrarDispositivo(resultado);
                }
            }

            @Override
            public void onScanFailed(int codigoError) {
                Log.e(ETIQUETA_LOG, "Error al escanear BLE. Código: " + codigoError);
                elEstado.ponerEstadoBluetooth("error al escanear (código " + codigoError + ")");
            }
        };

        ScanSettings ajustes = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        elEscaner.startScan(null, ajustes, elCallbackDelEscaner);
        Log.d(ETIQUETA_LOG, "Escaneo BLE arrancado en modo " + modo);
        elEstado.ponerEstadoBluetooth(MODO_NUESTRO.equals(modo)
                ? "buscando nuestro nodo (" + UUID_NUESTRO_BEACON + ")..."
                : "buscando todos los dispositivos (ver Logcat)");
    }

    // --------------------------------------------------------------
    // resultado: ScanResult --> mostrarDispositivo()
    // --------------------------------------------------------------
    @SuppressLint("MissingPermission")
    private void mostrarDispositivo(ScanResult resultado) {
        String nombre = resultado.getScanRecord() != null ? resultado.getScanRecord().getDeviceName() : null;
        String infoIBeacon = "";
        if (resultado.getScanRecord() != null) {
            TramaIBeacon trama = new TramaIBeacon(resultado.getScanRecord().getBytes());
            if (trama.esIBeacon()) {
                infoIBeacon = " | iBeacon UUID=" + trama.getUUIDComoTexto()
                        + " major=" + trama.getMajorEntero() + " minor=" + trama.getValorEntero()
                        + (UUID_NUESTRO_BEACON.equals(trama.getUUIDComoTexto()) ? "  <<< ¡ES EL NUESTRO!" : "");
            }
        }
        Log.d(ETIQUETA_LOG, "Dispositivo: " + resultado.getDevice().getAddress()
                + " | nombre: " + nombre + " | rssi: " + resultado.getRssi() + infoIBeacon);
    }

    // --------------------------------------------------------------
    // resultado: ScanResult --> procesarSiEsNuestroBeacon()
    // --------------------------------------------------------------
    private void procesarSiEsNuestroBeacon(ScanResult resultado) {
        if (resultado.getScanRecord() == null) {
            return;
        }
        TramaIBeacon trama = new TramaIBeacon(resultado.getScanRecord().getBytes());
        if (!trama.esIBeacon() || !UUID_NUESTRO_BEACON.equals(trama.getUUIDComoTexto())) {
            return;
        }
        Medicion medicion = Medicion.desdeTrama(trama);
        elEstado.ponerEstadoBluetooth("recibiendo nuestro nodo");
        int minor = medicion.getTipo() == Medicion.TIPO_TEMPERATURA
                ? trama.getValorEnteroConSigno() : trama.getValorEntero();
        elEstado.registrarMedicion(medicion, resultado.getRssi(), trama.getMajorEntero(), minor);

        if (!elFiltro.esNueva(medicion.getTipo(), medicion.getContador())) {
            return; // anuncio repetido: ya enviado
        }
        Log.d(ETIQUETA_LOG, "¡Nuestro beacon! " + medicion + " rssi=" + resultado.getRssi());
        enviarAlServidor(medicion);
    }

    // --------------------------------------------------------------
    // medicion: Medicion --> enviarAlServidor()
    // Si falla, se olvida la medida para reintentarla en el siguiente anuncio.
    // --------------------------------------------------------------
    private void enviarAlServidor(Medicion medicion) {
        String queEs = medicion.getTipo() == Medicion.TIPO_O3 ? "O₃" : "temperatura";
        elEstado.ponerEstadoServidor("enviando " + queEs + " (contador " + medicion.getContador() + ")...");

        laLogica.guardarMedicion(medicion, (exito, detalle) -> {
            Log.d(ETIQUETA_LOG, (exito ? "POST OK: " : "POST FALLIDO: ") + detalle);
            if (exito) {
                elEstado.ponerEstadoServidor(queEs + " guardada (contador " + medicion.getContador() + ")");
            } else {
                elFiltro.olvidar(medicion.getTipo(), medicion.getContador());
                elEstado.ponerEstadoServidor("ERROR al guardar " + queEs + ": " + detalle);
            }
        });
    }

    // --------------------------------------------------------------
    // detenerEscaneo()
    // --------------------------------------------------------------
    @SuppressLint("MissingPermission")
    private void detenerEscaneo() {
        if (elEscaner != null && elCallbackDelEscaner != null && tienePermisoEscanear()) {
            elEscaner.stopScan(elCallbackDelEscaner);
            Log.d(ETIQUETA_LOG, "Escaneo BLE detenido");
            elEstado.ponerEstadoBluetooth("búsqueda detenida");
        }
        elCallbackDelEscaner = null;
        modoActual = null;
    }

    @Override
    public void onDestroy() {
        detenerEscaneo();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
