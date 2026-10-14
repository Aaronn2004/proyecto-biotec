// -*- mode: c++ -*-
// ==============================================================
// Fichero:     LED.h
// Descripción: Control del LED de la placa.
// Diseño:      doc/arduino_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
#ifndef LED_H_INCLUIDO
#define LED_H_INCLUIDO

#include <Arduino.h>

class LED {
private:
  int numeroPin;

public:
  // --------------------------------------------------------------
  // pin: N --> LED()
  // --------------------------------------------------------------
  LED( int pin ) : numeroPin( pin ) {
    pinMode( numeroPin, OUTPUT );
    apagar();
  }

  // --------------------------------------------------------------
  // encender() -->
  // --------------------------------------------------------------
  void encender() { digitalWrite( numeroPin, HIGH ); }

  // --------------------------------------------------------------
  // apagar() -->
  // --------------------------------------------------------------
  void apagar() { digitalWrite( numeroPin, LOW ); }

  // --------------------------------------------------------------
  // tiempoMs: N --> brillar() -->
  // --------------------------------------------------------------
  void brillar( long tiempoMs ) {
    encender();
    delay( tiempoMs );
    apagar();
  }
};

#endif
