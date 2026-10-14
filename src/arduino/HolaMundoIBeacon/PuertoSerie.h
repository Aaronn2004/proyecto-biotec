// -*- mode: c++ -*-
// ==============================================================
// Fichero:     PuertoSerie.h
// Descripción: Escritura por el puerto serie USB (depuración).
// Diseño:      doc/arduino_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
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
  // mensaje: Text | Z --> escribir() -->
  // --------------------------------------------------------------
  template <typename T>
  void escribir( T mensaje ) {
    Serial.print( mensaje );
  }
};

#endif
