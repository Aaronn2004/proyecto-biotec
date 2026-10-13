# Proyecto Biometría y Medio Ambiente — Sprint 0

Medidas **ficticias** (O3 y temperatura) salen de la placa Arduino por iBeacon, el móvil Android las recibe y las envía por REST al servidor (Plesk de la UPV), que las guarda en MariaDB; la web muestra la última de cada tipo.

```
Arduino (C++) --BLE iBeacon--> Android (Java) --POST api.php/medicion--> Plesk: api.php + Logica.php (PHP) --> MariaDB
                                       Web (HTML/JS) --GET api.php/medicion/ultima?tipo=N--^
```

## Estructura

| Carpeta | Contenido | Se abre / ejecuta con |
|---------|-----------|-----------------------|
| `src/arduino/HolaMundoIBeacon/` | Nodo sensor (medidas ficticias) | Arduino IDE |
| `src/android/` | App Android | Android Studio (*File → Open* → `src/android`) |
| `src/servidor/` | API REST (`api.php`) + lógica del negocio (`Logica.php`) + esquema de la BD + tests | Se sube a Plesk |
| `src/web/` | Página web + LogicaFake | Se sube a Plesk (misma carpeta que `api.php`) |
| `doc/` | Diseños (`*_design.md`) y criterios de aceptación | — |

## Configurar Plesk (una sola vez)

En la UPV tu hosting es `https://<tu-usuario>.upv.edu.es` y se gestiona desde el panel de Plesk.

### 1. Crear la base de datos

1. En Plesk: **Bases de datos → Añadir base de datos**.
2. Nombre, por ejemplo `pbio`. Plesk le añade tu prefijo (p. ej. `aaron_pbio`): **apunta el nombre completo**.
3. En la misma pantalla crea un **usuario** de la base de datos con su **contraseña**. Apunta usuario y contraseña.
4. Pulsa **phpMyAdmin** al lado de la base de datos → pestaña **SQL** → pega el contenido de `src/servidor/esquema.sql` → **Continuar**. Debe aparecer la tabla `medicion` con 4 columnas (`dispositivo`, `tipo`, `valor`, `fecha`). Ojo: el script borra antes la tabla `medicion` si ya existía.

### 2. Subir los ficheros

1. En Plesk: **Archivos** → entra en `httpdocs` → crea la carpeta **`biometria`**.
2. Sube a `httpdocs/biometria/` estos 4 ficheros (juntos, en la misma carpeta):
   - `src/servidor/api.php`
   - `src/servidor/Logica.php`
   - `src/web/index.html`
   - `src/web/LogicaFake.js`
3. Sube también `src/servidor/config.example.php` y, **dentro de Plesk**, renómbralo a **`config.php`** y edítalo con los datos del paso 1:
   ```php
   'dsn' => 'mysql:host=localhost;port=3306;dbname=aaron_pbio;charset=utf8mb4',
   'usuario' => 'aaron_pbio_user',
   'clave' => 'la contraseña',
   ```
   `config.php` **no** se sube a GitHub (está en `.gitignore`), porque lleva la contraseña.

### 3. Comprobar que funciona (sin móvil ni placa)

- `https://<tu-usuario>.upv.edu.es/biometria/api.php/medicion/ultima` → debe responder `{"error":"no hay mediciones"}`.
  - Si responde `no se pudo conectar con la base de datos`: revisa `config.php`.
  - Si da 404 de Plesk: prueba `.../api.php?ruta=/medicion/ultima` (algunos hostings no pasan la ruta tras `api.php`).
- Inserta una medida de prueba desde cmd:
  ```
  curl -X POST https://<tu-usuario>.upv.edu.es/biometria/api.php/medicion -H "Content-Type: application/json" -d "{\"dispositivo\":\"AARON-GTI-PBIO-1\",\"tipo\":11,\"valor\":0.235}"
  ```
- Abre `https://<tu-usuario>.upv.edu.es/biometria/`: debe mostrar **O3: 0.235 ppm**. En phpMyAdmin → tabla `medicion` → **Examinar** verás la fila.

## Puesta en marcha (prueba extremo a extremo)

1. **Medidas ficticias**: en `src/arduino/HolaMundoIBeacon/Medidor.h`, `VALOR_O3_FICTICIO = 235` (= 0.235 ppm) y `VALOR_TEMPERATURA_FICTICIO = 215` (= 21.5 °C). Sube el sketch. En el monitor serie (115200) verás `tipo=11 valor=235` y `tipo=12 valor=215`, alternando cada 2 s.
2. **App**: en `LogicaFake.java` pon tu dirección en `URL_SERVIDOR = "https://<tu-usuario>.upv.edu.es/biometria/api.php"`. Instálala en un **móvil real**, acepta los permisos y ten Bluetooth **y** ubicación activados. El móvil solo necesita **internet** (no hace falta estar en la misma WiFi que el PC).
3. Pulsa **"Buscar nuestro dispositivo BTLE"**: en la pantalla verás O₃, temperatura y "Servidor: … guardada"; en Logcat (filtro `>>>>`) `POST OK: 201`.
4. La web (`https://<tu-usuario>.upv.edu.es/biometria/`) se refresca cada 2 s y muestra **0.235 ppm** y **21.5 °C**.

## Tests automáticos

- Lógica del negocio: `cd src/servidor` → `php tests/LogicaTest.php` (8 tests, necesita PHP con pdo_sqlite).
- API REST: `php tests/ApiTest.php` (6 tests; arranca el servidor web de PHP con una BD SQLite temporal; Linux/macOS).
- LogicaFake web: `cd src/web` → `node --test` (4 tests).
- Android: en Android Studio, botón derecho sobre `app/src/test/java` → *Run 'Tests in ...'* (16 tests + el de ejemplo).

Criterios de aceptación: `doc/criterios_aceptacion.md`.
