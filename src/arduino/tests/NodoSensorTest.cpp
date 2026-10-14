// ==============================================================
// Fichero:     NodoSensorTest.cpp
// Descripción: Tests automáticos del nodo sensor (Medidor y
//              Publicador) compilados en el PC con mocks de Arduino
//              y Bluefruit (carpeta mocks/). Comprueban que la trama
//              iBeacon lleva el UUID, el major y el minor del diseño.
//              Ejecutar desde src/arduino:
//                g++ -std=c++17 -I tests/mocks -I HolaMundoIBeacon tests/NodoSensorTest.cpp -o tests/nodo && tests/nodo
// Diseño:      doc/arduino_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
#include <cstdio>
#include <cstring>
#include <bluefruit.h>
#include "LED.h"
#include "PuertoSerie.h"

namespace Globales {
  PuertoSerie elPuerto( 115200 );
};

#include "EmisoraBLE.h"
#include "Medidor.h"
#include "Publicador.h"

static int testsOk = 0;
static int testsFallidos = 0;

// --------------------------------------------------------------
// condicion: B, nombre: Text --> comprobar()
// --------------------------------------------------------------
void comprobar( bool condicion, const char * nombre ) {
  if ( condicion ) {
    testsOk++;
    printf( "ok     - %s\n", nombre );
  } else {
    testsFallidos++;
    printf( "FALLO  - %s\n", nombre );
  }
}

// --------------------------------------------------------------
// main() --> Z   (0 si pasan todos los tests)
// --------------------------------------------------------------
int main() {
  Medidor elMedidor;
  Publicador elPublicador;
  elPublicador.encenderEmisora();

  comprobar( elMedidor.medirO3() == VALOR_O3_FICTICIO, "medirO3() devuelve el valor ficticio" );
  comprobar( elMedidor.medirTemperatura() == VALOR_TEMPERATURA_FICTICIO, "medirTemperatura() devuelve el valor ficticio" );

  elPublicador.publicarO3( 235, 7, 0 );
  comprobar( memcmp( ultimoBeacon.uuid, "AARON-GTI-PBIO-1", 16 ) == 0, "el UUID del iBeacon es AARON-GTI-PBIO-1" );
  comprobar( ultimoBeacon.major == ( ( 11 << 8 ) + 7 ), "O3: major = (11 << 8) + contador" );
  comprobar( ultimoBeacon.minor == 235, "O3: minor = valor (ppm * 1000)" );
  comprobar( ultimoBeacon.fabricante == 0x004c, "fabricante Apple (0x004C), obligatorio en iBeacon" );
  comprobar( !ultimoBeacon.anunciando, "al acabar el tiempo de espera deja de anunciar" );

  elPublicador.publicarTemperatura( -35, 9, 0 );
  comprobar( ultimoBeacon.major == ( ( 12 << 8 ) + 9 ), "temperatura: major = (12 << 8) + contador" );
  comprobar( ultimoBeacon.minor == 0xFFDD, "temperatura negativa (-3.5 C) viaja con signo: 0xFFDD" );

  elPublicador.publicarO3( 100, 255, 0 );
  comprobar( ultimoBeacon.major == ( ( 11 << 8 ) + 255 ), "contador 255 cabe en el byte bajo del major" );

  printf( "\n# pass %d\n# fail %d\n", testsOk, testsFallidos );
  return testsFallidos == 0 ? 0 : 1;
}
