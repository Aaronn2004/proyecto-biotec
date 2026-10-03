// -*- mode: c++ -*-
// ==============================================================
// HolaMundoIBeacon.ino
// Nodo sensor (SparkFun Pro nRF52840 Mini) - Sprint 0
//
// Emite una medida FICTICIA de O3 como trama iBeacon:
//   UUID  = identificador de NUESTRO dispositivo (16 bytes ASCII)
//   major = (tipoMedida << 8) + contador   -> byte alto: tipo, byte bajo: contador
//   minor = valor de la medida (entero, ppm * 1000)
//
// Para cambiar la medida: edita VALOR_O3_FICTICIO_PPM en Medidor.h
// ==============================================================

#include <bluefruit.h>

#undef min
#undef max

#include "LED.h"
#include "PuertoSerie.h"

namespace Globales {
  LED elLED( LED_BUILTIN );
  PuertoSerie elPuerto( 115200 );
};

#include "EmisoraBLE.h"
#include "Medidor.h"
#include "Publicador.h"

namespace Globales {
  Publicador elPublicador;
  Medidor elMedidor;
};

// --------------------------------------------------------------
// setup()
// --------------------------------------------------------------
void setup() {
  // OJO: NO esperamos al puerto serie (while(!Serial)).
  // Si la placa va con batería y sin USB, se quedaría bloqueada aquí para siempre.

  Globales::elPublicador.encenderEmisora();
  Globales::elMedidor.iniciarMedidor();

  Globales::elLED.brillar( 500 );
  Globales::elPuerto.escribir( "---- setup(): fin ----\n" );
}

// --------------------------------------------------------------
// loop()
// --------------------------------------------------------------
namespace Loop {
  uint8_t contador = 0;
};

void loop() {
  using namespace Loop;
  using namespace Globales;

  contador++;

  int16_t valorO3 = elMedidor.medirO3();

  elPuerto.escribir( "\n---- loop() #" );
  elPuerto.escribir( contador );
  elPuerto.escribir( "  O3 (ppm*1000) = " );
  elPuerto.escribir( valorO3 );
  elPuerto.escribir( "\n" );

  elLED.brillar( 100 );

  // Anuncia la medida durante 3 s y luego para la emisión
  elPublicador.publicarO3( valorO3, contador, 3000 );
}
