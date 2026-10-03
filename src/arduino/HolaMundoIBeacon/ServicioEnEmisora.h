#ifndef SERVICIO_EN_EMISORA_H_INCLUDED
#define SERVICIO_EN_EMISORA_H_INCLUDED

#include <Arduino.h>

class ServicioEnEmisora {
public:
  using CallbackCaracteristicaEscrita = void ( uint16_t conn_handle, uint16_t attr_handle, uint8_t * value, uint16_t length );

private:
  uint16_t uuidServicio;

public:
  ServicioEnEmisora(uint16_t uuid) : uuidServicio(uuid) {}

  void escribirUUID() {
    // Lógica para registrar el UUID del servicio BLE
  }
};

#endif // SERVICIO_EN_EMISORA_H_INCLUDED