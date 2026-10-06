// -*- mode: c++ -*-
#ifndef PUBLICADOR_H_INCLUIDO
#define PUBLICADOR_H_INCLUIDO

// ==============================================================
// Publicador: sabe CÓMO se codifica una medida en un iBeacon.
//   major = (tipoMedida << 8) + contador
//   minor = valor (O3: ppm*1000 ; temperatura: décimas de °C, con signo)
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
    publicarMedida( MedicionesID::O3, valor, contador, tiempoEspera );
  }

  // --------------------------------------------------------------
  // valor: Z, contador: N, tiempoEspera: N --> publicarTemperatura() -->
  // --------------------------------------------------------------
  void publicarTemperatura( int16_t valor, uint8_t contador, long tiempoEspera ) {
    publicarMedida( MedicionesID::TEMPERATURA, valor, contador, tiempoEspera );
  }

private:
  // --------------------------------------------------------------
  // tipo: N, valor: Z, contador: N, tiempoEspera: N --> publicarMedida() -->
  //   major = (tipo << 8) + contador ; minor = valor (16 bits, con signo)
  // --------------------------------------------------------------
  void publicarMedida( uint8_t tipo, int16_t valor, uint8_t contador, long tiempoEspera ) {
    uint16_t major = ( tipo << 8 ) + contador;

    laEmisora.emitirAnuncioIBeacon( beaconUUID, major, (uint16_t) valor, RSSI_A_1_METRO );

    Globales::elPuerto.escribir( "   publicarMedida(): tipo=" );
    Globales::elPuerto.escribir( tipo );
    Globales::elPuerto.escribir( " valor=" );
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
