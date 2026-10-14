# Proyecto Biometría y Medio Ambiente — Sprint 0

Autor: **Aaron Blasco Blay** (`author.md`) · GTI, UPV · 2026

La placa Arduino genera medidas **ficticias** de O3 y temperatura y las emite por iBeacon. El móvil Android las recibe y las envía por REST al servidor (Plesk de la UPV), que las guarda en MariaDB. La web muestra la última medida de cada tipo.

```
Arduino (C++) --BLE iBeacon--> Android (Java) --POST communication/api.php/medicion--> Logica.php --> MariaDB
                                    Web: gui/index.html -> LogicaFake.js --GET .../medicion/ultima?tipo=N--^
```

## Estructura (un diseño por componente)

| Diseño (`doc/`) | Código (`src/`) | Qué es | Se abre / ejecuta con |
|---|---|---|---|
| `arduino_design.md` | `arduino/` | Nodo sensor (medidas ficticias) + tests en el PC | Arduino IDE / g++ |
| `android_design.md` | `android/` | App Android (GUI, servicio BLE, LogicaFake) | Android Studio (*File → Open* → `src/android`) |
| `communication_design.md` | `communication/` | API REST (`api.php`) | Plesk |
| `business_logic_design.md` | `business_logic/` | Lógica del negocio (`Logica.php`) | Plesk |
| `database_design.md` | `database/` | Tabla `medicion` (`esquema.sql`) | phpMyAdmin |
| `frontend_business_logic_design.md` | `frontend_business_logic/` | LogicaFake de la web | Plesk / navegador |
| `gui_design.md` | `gui/` | Página web | Plesk / navegador |

Además, en `doc/`:

- `arquitectura.md`: visión general, tipos compartidos y protocolo iBeacon.
- `criterios_aceptacion.md`: criterios de aceptación.
- `prompts/`: prompts usados con la IA y la revisión de lo que generó.

## Ramas de Git

- `main`: versión entregable del Sprint 0, siempre funcionando.
- `develop`: integración del trabajo en curso; se fusiona en `main` cuando todos los tests pasan.
- `feature/<tema>`: una rama por cambio (p. ej. `feature/estructura-agente`), que sale de `develop` y vuelve a ella.

## Configurar Plesk (una sola vez)

El hosting es `https://ablabla.upv.edu.es` y se gestiona desde el panel de Plesk.

### 1. Base de datos

1. En Plesk, ve a **Bases de datos → Añadir base de datos**: base de datos `biotec`, usuario `biotec_user` y su contraseña.
2. Pulsa **phpMyAdmin** → pestaña **SQL** → pega `src/database/esquema.sql` → **Continuar**. Debe aparecer la tabla `medicion` con 4 columnas: `dispositivo`, `tipo`, `valor` y `fecha`. Ojo: el script borra antes la tabla `medicion` si ya existía.

### 2. Subir los ficheros (misma estructura de carpetas que `src/`)

En **Archivos → `httpdocs/biometria/`**:

| Carpeta en Plesk | Ficheros |
|---|---|
| `biometria/communication/` | `src/communication/api.php` y `config.php` (copia de `config.example.php` con los datos reales) |
| `biometria/business_logic/` | `src/business_logic/Logica.php` |
| `biometria/frontend_business_logic/` | `src/frontend_business_logic/LogicaFake.js` |
| `biometria/gui/` | `src/gui/index.html` |

`config.php` **no** se sube a GitHub (está en `.gitignore`), porque lleva la contraseña:

```php
'dsn' => 'mysql:host=localhost;port=3306;dbname=biotec;charset=utf8mb4',
'usuario' => 'biotec_user',
'clave' => 'la contraseña',
```

### 3. Comprobar que funciona (sin móvil ni placa)

- Abre `https://ablabla.upv.edu.es/biometria/communication/api.php/medicion/ultima`. Debe responder `{"error":"no hay mediciones"}`.
  - Si responde `no se pudo conectar con la base de datos`, revisa `config.php`.
- Inserta una medida de prueba desde la consola:
  ```
  curl -X POST https://ablabla.upv.edu.es/biometria/communication/api.php/medicion -H "Content-Type: application/json" -d "{\"dispositivo\":\"AARON-GTI-PBIO-1\",\"tipo\":11,\"valor\":0.235}"
  ```
- Abre `https://ablabla.upv.edu.es/biometria/gui/`. Debe mostrar **O3: 0.235 ppm**.

## Puesta en marcha (prueba extremo a extremo)

1. **Medidas ficticias.** En `src/arduino/HolaMundoIBeacon/Medidor.h` pon `VALOR_O3_FICTICIO = 235` (0.235 ppm) y `VALOR_TEMPERATURA_FICTICIO = 215` (21.5 °C), y sube el sketch. En el monitor serie (115200) aparecen `tipo=11 valor=235` y `tipo=12 valor=215`, alternando cada 2 s.
2. **App.** `LogicaFake.URL_SERVIDOR` ya apunta a `https://ablabla.upv.edu.es/biometria/communication/api.php`. Instálala en un **móvil real**, acepta los permisos y activa el Bluetooth **y** la ubicación.
3. **Buscar.** Pulsa **"Buscar nuestro dispositivo BTLE"**. En la pantalla verás O₃, temperatura y "Servidor: … guardada", y en Logcat (filtro `>>>>`) `POST OK`.
4. **Web.** La web (`.../biometria/gui/`) se refresca cada 2 s y muestra **0.235 ppm** y **21.5 °C**.

## Tests automáticos

| Componente | Comando (desde la raíz del repositorio) | Tests |
|---|---|---|
| Arduino | `cd src/arduino && g++ -std=c++17 -I tests/mocks -I HolaMundoIBeacon tests/NodoSensorTest.cpp -o tests/nodo && tests/nodo` | 10 |
| Lógica del negocio | `php src/business_logic/tests/LogicaTest.php` (PHP con pdo_sqlite) | 10 |
| Comunicación (API REST) | `php src/communication/tests/ApiTest.php` (Linux/macOS) | 7 |
| LogicaFake web | `cd src/frontend_business_logic && node --test` | 5 |
| Android | Android Studio: botón derecho sobre `app/src/test/java` → *Run 'Tests in ...'* | 19 |

Criterios de aceptación: `doc/criterios_aceptacion.md`.
