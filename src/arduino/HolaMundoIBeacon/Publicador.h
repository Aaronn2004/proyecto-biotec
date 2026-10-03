// -*- mode: c++ -*-
#ifndef PUBLICADOR_H_INCLUIDO
#define PUBLICADOR_H_INCLUIDO

// ==============================================================
// Publicador: sabe CÓMO se codifica una medida en un iBeacon.
//   major = (tipoMedida << 8) + contador
//   minor = valor
// ==============================================================
class Publicador {
private:
  // UUID PROPIO (16 caracteres exactos). Si en clase todos usáis el
  // mismo UUID, vuestros móviles captarán los beacons de los demás.
  // Debe coincidir con UUID_NUESTRO_BEACON de la app Android.
  uint8_t beaconUUID[16] = {
    'A', 'A', 'R', 'O', 'N', '-', 'G', 'T',
    'I', '-', 'P', 'B', 'I', 'O', '-', '1'
  };

  const int8_t RSSI_A_1_METRO = -53;

public:
  EmisoraBLE laEmisora {
    "Aaron_GTI", // nombre visible del dispositivo
    0x004c,      // fabricante: Apple (obligatorio en iBeacon)
    4            // txPower en dBm
  };

  enum MedicionesID {
    O3 = 11,
    TEMPERATURA = 12,
    RUIDO = 13
  };

  // --------------------------------------------------------------
  // Publicador()
  // --------------------------------------------------------------
  Publicador() {
  }

  // --------------------------------------------------------------
  // encenderEmisora() -->
  // --------------------------------------------------------------
  void encenderEmisora() {
    laEmisora.encenderEmisora();
  }

  // --------------------------------------------------------------
  // valor: Z, contador: N, tiempoEspera: N --> publicarO3() -->
  // --------------------------------------------------------------
  void publicarO3( int16_t valor, uint8_t contador, long tiempoEspera ) {
    uint16_t major = ( MedicionesID::O3 << 8 ) + contador;

    laEmisora.emitirAnuncioIBeacon( beaconUUID, major, (uint16_t) valor, RSSI_A_1_METRO );

    Globales::elPuerto.escribir( "   publicarO3(): valor=" );
    Globales::elPuerto.escribir( valor );
    Globales::elPuerto.escribir( " contador=" );
    Globales::elPuerto.escribir( contador );
    Globales::elPuerto.escribir( " major=" );
    Globales::elPuerto.escribir( major );
    Globales::elPuerto.escribir( "\n" );

    delay( tiempoEspera );
    laEmisora.detenerAnuncio();
  }
};

#endif
