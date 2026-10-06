// -*- mode: c++ -*-
// ==============================================================
// HolaMundoIBeacon.ino
// Nodo sensor (SparkFun Pro nRF52840 Mini) - Sprint 0
//
// Emite medidas FICTICIAS (O3 y temperatura) como tramas iBeacon:
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

// Cada cuánto sale un dato nuevo: cada medida se anuncia este tiempo
// y luego se pasa a la siguiente (O3, temperatura, O3, ...)
const long TIEMPO_ENTRE_DATOS_MS = 2000;

void loop() {
  using namespace Loop;
  using namespace Globales;

  contador++;

  int16_t valorO3 = elMedidor.medirO3();
  int16_t valorTemperatura = elMedidor.medirTemperatura();

  elPuerto.escribir( "\n---- loop() #" );
  elPuerto.escribir( contador );
  elPuerto.escribir( "  O3 (ppm*1000) = " );
  elPuerto.escribir( valorO3 );
  elPuerto.escribir( "  T (decimas C) = " );
  elPuerto.escribir( valorTemperatura );
  elPuerto.escribir( "\n" );

  elLED.brillar( 100 );

  // Un dato nuevo cada TIEMPO_ENTRE_DATOS_MS: primero O3 y luego temperatura
  elPublicador.publicarO3( valorO3, contador, TIEMPO_ENTRE_DATOS_MS );
  elPublicador.publicarTemperatura( valorTemperatura, contador, TIEMPO_ENTRE_DATOS_MS );
}
