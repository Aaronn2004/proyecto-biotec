// -*- mode: c++ -*-
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
