// -*- mode: c++ -*-
#ifndef PUERTO_SERIE_H_INCLUIDO
#define PUERTO_SERIE_H_INCLUIDO

#include <Arduino.h>
#include <Adafruit_TinyUSB.h> // Serial por USB en el nRF52840

class PuertoSerie {
public:
  // --------------------------------------------------------------
  // baudios: N --> PuertoSerie()
  // --------------------------------------------------------------
  PuertoSerie( unsigned long baudios ) {
    Serial.begin( baudios );
  }

  // --------------------------------------------------------------
  // esperarDisponible() -->
  // Solo para depurar con el PC: bloquea si no hay USB.
  // --------------------------------------------------------------
  void esperarDisponible() {
    while ( !Serial ) {
      delay( 10 );
    }
  }

  // --------------------------------------------------------------
  // mensaje: T --> escribir() -->
  // --------------------------------------------------------------
  template <typename T>
  void escribir( T mensaje ) {
    Serial.print( mensaje );
  }
};

#endif
