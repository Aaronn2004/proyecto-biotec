// -*- mode: c++ -*-
#ifndef EMISORA_BLE_H_INCLUIDO
#define EMISORA_BLE_H_INCLUIDO

#include <bluefruit.h>

// ==============================================================
// EmisoraBLE: envoltorio sobre la pila Bluefruit para emitir
// anuncios iBeacon.
// ==============================================================
class EmisoraBLE {
private:
  const char * nombreEmisora;
  const uint16_t fabricanteID;
  const int8_t txPower; // dBm. Valores válidos nRF52840: -40,-20,-16,-12,-8,-4,0,2..8

public:
  // --------------------------------------------------------------
  // nombre: Text, fabricanteID: N, txPower: Z --> EmisoraBLE()
  // --------------------------------------------------------------
  EmisoraBLE( const char * nombre, uint16_t fabID, int8_t power )
    : nombreEmisora( nombre ), fabricanteID( fabID ), txPower( power ) {
  }

  // --------------------------------------------------------------
  // encenderEmisora() -->
  // --------------------------------------------------------------
  void encenderEmisora() {
    Bluefruit.begin();
    detenerAnuncio();
  }

  // --------------------------------------------------------------
  // detenerAnuncio() -->
  // --------------------------------------------------------------
  void detenerAnuncio() {
    if ( estaAnunciando() ) {
      Bluefruit.Advertising.stop();
    }
  }

  // --------------------------------------------------------------
  // estaAnunciando() <-- B
  // --------------------------------------------------------------
  bool estaAnunciando() {
    return Bluefruit.Advertising.isRunning();
  }

  // --------------------------------------------------------------
  // uuid: [N]_16, major: N, minor: N, rssi: Z --> emitirAnuncioIBeacon() -->
  // --------------------------------------------------------------
  void emitirAnuncioIBeacon( uint8_t * uuid, uint16_t major, uint16_t minor, int8_t rssi ) {
    detenerAnuncio();

    Bluefruit.Advertising.clearData();
    Bluefruit.ScanResponse.clearData();

    BLEBeacon elBeacon( uuid, major, minor, rssi );
    elBeacon.setManufacturer( fabricanteID );

    Bluefruit.setTxPower( txPower );
    Bluefruit.setName( nombreEmisora );
    Bluefruit.ScanResponse.addName();

    Bluefruit.Advertising.setBeacon( elBeacon );
    Bluefruit.Advertising.restartOnDisconnect( true );
    Bluefruit.Advertising.setInterval( 100, 100 ); // unidades de 0.625 ms

    Bluefruit.Advertising.start( 0 ); // 0 = anunciar indefinidamente
  }
};

#endif
