// -*- mode: c++ -*-
// ==============================================================
// Fichero:     Medidor.h
// Descripción: Medidor FICTICIO de O3 y temperatura (Sprint 0): valores
//              editables en el código.
// Diseño:      doc/arduino_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
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

// Temperatura FICTICIA en décimas de grado (215 = 21.5 °C).
// Puede ser negativa (-35 = -3.5 °C): viaja como entero con signo.
const int16_t VALOR_TEMPERATURA_FICTICIO = 215;

// Rango del modo aleatorio de la temperatura (décimas de °C: 15.0 .. 30.0 °C)
const int16_t TEMPERATURA_MIN = 150;
const int16_t TEMPERATURA_MAX = 300;

class Medidor {
public:
  // --------------------------------------------------------------
  // Medidor() -->
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

  // --------------------------------------------------------------
  // medirTemperatura() --> Z   (décimas de °C)
  // --------------------------------------------------------------
  int16_t medirTemperatura() {
    if ( USAR_VALOR_ALEATORIO ) {
      return (int16_t) random( TEMPERATURA_MIN, TEMPERATURA_MAX + 1 );
    }
    return VALOR_TEMPERATURA_FICTICIO;
  }
};

#endif
