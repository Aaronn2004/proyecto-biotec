// -*- mode: c++ -*-
#ifndef MEDIDOR_H_INCLUIDO
#define MEDIDOR_H_INCLUIDO

// ==============================================================
// Medidor FICTICIO (Sprint 0). No lee ningún sensor.
//
//  >>> Cambia aquí la medida que quieres ver en la web <<<
//
// El valor se expresa en ppm * 1000 (es decir, 235 = 0.235 ppm)
// porque el campo minor del iBeacon es un entero de 16 bits.
// ==============================================================
const int16_t VALOR_O3_FICTICIO = 235;

// Si lo pones a true, se ignora el valor fijo y se genera uno
// aleatorio entre VALOR_MIN y VALOR_MAX en cada medida.
const bool USAR_VALOR_ALEATORIO = false;
const int16_t VALOR_MIN = 100;
const int16_t VALOR_MAX = 400;

class Medidor {
public:
  // --------------------------------------------------------------
  // Medidor()
  // --------------------------------------------------------------
  Medidor() {
  }

  // --------------------------------------------------------------
  // iniciarMedidor() -->
  // --------------------------------------------------------------
  void iniciarMedidor() {
    randomSeed( analogRead( A0 ) ); // semilla para el modo aleatorio
  }

  // --------------------------------------------------------------
  // medirO3() --> Z   (ppm * 1000)
  // --------------------------------------------------------------
  int16_t medirO3() {
    if ( USAR_VALOR_ALEATORIO ) {
      return (int16_t) random( VALOR_MIN, VALOR_MAX + 1 );
    }
    return VALOR_O3_FICTICIO;
  }
};

#endif
