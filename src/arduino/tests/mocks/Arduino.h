// ==============================================================
// Fichero:     Arduino.h  (MOCK para los tests en el PC)
// Descripción: Sustituye a la API de Arduino para poder compilar y
//              probar el código de la placa con g++ en el ordenador.
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
#pragma once
#include <stdint.h>
#define HIGH 1
#define LOW 0
#define OUTPUT 1
#define LED_BUILTIN 7
#define A0 14
inline void pinMode(int, int) {}
inline void digitalWrite(int, int) {}
inline void delay(unsigned long) {}
inline long random(long minimo, long) { return minimo; }
inline void randomSeed(unsigned long) {}
inline int analogRead(int) { return 0; }
struct SerialMock {
  void begin(unsigned long) {}
  operator bool() { return true; }
  template <class T> void print(T) {}
};
inline SerialMock Serial;
