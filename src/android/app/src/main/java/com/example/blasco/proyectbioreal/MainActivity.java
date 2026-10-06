package com.example.blasco.proyectbioreal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

// ==============================================================
// MainActivity: 3 botones y unos textos con lo que está pasando
// (estado del Bluetooth, última trama de nuestro nodo y servidor).
// Los detalles completos siguen en el Logcat (filtro ">>>>").
// ==============================================================
public class MainActivity extends AppCompatActivity {

    private static final String ETIQUETA_LOG = ">>>>";
    private static final int CODIGO_PETICION_PERMISOS = 1234;

    private TextView textoBluetooth, textoO3, textoTemperatura, textoContador,
            textoRssi, textoTrama, textoServidor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textoBluetooth = findViewById(R.id.textoBluetooth);
        textoO3 = findViewById(R.id.textoO3);
        textoTemperatura = findViewById(R.id.textoTemperatura);
        textoContador = findViewById(R.id.textoContador);
        textoRssi = findViewById(R.id.textoRssi);
        textoTrama = findViewById(R.id.textoTrama);
        textoServidor = findViewById(R.id.textoServidor);

        pedirPermisosNecesarios();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // El servicio puede avisar desde otros hilos: pintamos siempre en el hilo de la UI
        EstadoNodo.getInstancia().observar(estado -> runOnUiThread(() -> pintarEstado(estado)));
    }

    @Override
    protected void onPause() {
        EstadoNodo.getInstancia().observar(null); // sin pantalla visible no hay nada que pintar
        super.onPause();
    }

    // --------------------------------------------------------------
    // estado: EstadoNodo --> pintarEstado()
    // --------------------------------------------------------------
    private void pintarEstado(EstadoNodo estado) {
        textoBluetooth.setText(estado.getTextoBluetooth());
        textoO3.setText(estado.getTextoO3());
        textoTemperatura.setText(estado.getTextoTemperatura());
        textoContador.setText(estado.getTextoContador());
        textoRssi.setText(estado.getTextoRssi());
        textoTrama.setText(estado.getTextoTrama());
        textoServidor.setText(estado.getTextoServidor());
    }

    // --------------------------------------------------------------
    // pedirPermisosNecesarios()
    // --------------------------------------------------------------
    private void pedirPermisosNecesarios() {
        List<String> permisos = new ArrayList<>();
        permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permisos.add(Manifest.permission.BLUETOOTH_SCAN);
            permisos.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        List<String> permisosQueFaltan = new ArrayList<>();
        for (String permiso : permisos) {
            if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
                permisosQueFaltan.add(permiso);
            }
        }
        if (!permisosQueFaltan.isEmpty()) {
            ActivityCompat.requestPermissions(this, permisosQueFaltan.toArray(new String[0]), CODIGO_PETICION_PERMISOS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != CODIGO_PETICION_PERMISOS) {
            return;
        }
        for (int resultado : grantResults) {
            if (resultado != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Sin permisos no se pueden escanear beacons", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    // --------------------------------------------------------------
    // modo: Text --> arrancarServicio()
    // --------------------------------------------------------------
    private void arrancarServicio(String modo) {
        if (!bluetoothListo()) {
            return;
        }
        Intent intent = new Intent(this, ServicioEscuharBeacons.class);
        intent.putExtra(ServicioEscuharBeacons.EXTRA_MODO, modo);
        startService(intent);
    }

    // --------------------------------------------------------------
    // bluetoothListo() --> B
    // Si el Bluetooth está apagado, pide al usuario que lo encienda.
    // --------------------------------------------------------------
    @SuppressLint("MissingPermission") // los permisos se piden en onCreate()
    private boolean bluetoothListo() {
        BluetoothManager gestor = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adaptador = gestor == null ? null : gestor.getAdapter();

        if (adaptador == null) {
            EstadoNodo.getInstancia().ponerEstadoBluetooth("no disponible (¿emulador?)");
            avisar("Este dispositivo no tiene Bluetooth (¿estás usando el emulador? Usa un móvil real)");
            return false;
        }
        if (!adaptador.isEnabled()) {
            EstadoNodo.getInstancia().ponerEstadoBluetooth("apagado");
            avisar("El Bluetooth está apagado: enciéndelo y vuelve a pulsar el botón");
            try {
                startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            } catch (SecurityException sinPermiso) {
                avisar("Falta el permiso de Bluetooth: acéptalo en Ajustes > Aplicaciones");
                pedirPermisosNecesarios();
            }
            return false;
        }
        return true;
    }

    // --------------------------------------------------------------
    // mensaje: Text --> avisar()   (Toast en pantalla + Logcat)
    // --------------------------------------------------------------
    private void avisar(String mensaje) {
        Log.e(ETIQUETA_LOG, mensaje);
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    // Botón 1: buscar TODOS los dispositivos BLE (solo se listan en el Log)
    public void botonBuscarDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, "botonBuscarDispositivosBTLEPulsado()");
        arrancarServicio(ServicioEscuharBeacons.MODO_TODOS);
    }

    // Botón 2: detener la búsqueda
    public void botonDetenerBusquedaDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, "botonDetenerBusquedaDispositivosBTLEPulsado()");
        stopService(new Intent(this, ServicioEscuharBeacons.class));
    }

    // Botón 3: buscar NUESTRO dispositivo y enviar sus medidas al servidor
    public void botonBuscarNuestroDispositivoBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, "botonBuscarNuestroDispositivoBTLEPulsado()");
        arrancarServicio(ServicioEscuharBeacons.MODO_NUESTRO);
    }
}
