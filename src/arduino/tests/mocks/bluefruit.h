// ==============================================================
// Fichero:     bluefruit.h  (MOCK para los tests en el PC)
// Descripción: Sustituye a la pila Bluefruit. En vez de emitir por
//              radio, apunta el último iBeacon que se habría anunciado
//              (ultimoBeacon) para que los tests lo comprueben.
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
#pragma once
#include "Arduino.h"
#include <string.h>

struct BeaconAnunciado {
  uint8_t uuid[16];
  uint16_t major;
  uint16_t minor;
  int8_t rssi;
  uint16_t fabricante;
  bool anunciando;
};
inline BeaconAnunciado ultimoBeacon = {};

struct BLEBeacon {
  BeaconAnunciado datos;
  BLEBeacon(uint8_t const uuid[16], uint16_t major, uint16_t minor, int8_t rssi) {
    memcpy(datos.uuid, uuid, 16);
    datos.major = major;
    datos.minor = minor;
    datos.rssi = rssi;
    datos.fabricante = 0;
  }
  void setManufacturer(uint16_t fabricante) { datos.fabricante = fabricante; }
};
struct DatosAnuncioMock { void clearData() {} void addName() {} };
struct AnuncioMock : DatosAnuncioMock {
  void stop() { ultimoBeacon.anunciando = false; }
  bool isRunning() { return ultimoBeacon.anunciando; }
  void setBeacon(BLEBeacon & beacon) { ultimoBeacon = beacon.datos; }
  void restartOnDisconnect(bool) {}
  void setInterval(int, int) {}
  void start(int) { ultimoBeacon.anunciando = true; }
};
struct BluefruitMock {
  AnuncioMock Advertising;
  DatosAnuncioMock ScanResponse;
  void begin() {}
  void setTxPower(int8_t) {}
  void setName(const char *) {}
};
inline BluefruitMock Bluefruit;
