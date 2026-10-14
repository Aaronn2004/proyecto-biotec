# Uso de la IA — prompts y revisión

Método de trabajo (el que pide la asignatura):

1. Primero se escribe el **diseño** del componente en `doc/xxx_design.md`, con la notación de la asignatura.
2. Después se pide a la IA que lo implemente con un **prompt detallado**: el de esta carpeta más el diseño adjunto.
3. Por último se **revisa** lo que genera la IA:
   - ¿coincide con el diseño?
   - ¿pasan los tests?
   - ¿funciona en la placa, el móvil y Plesk?

   Cuando no coincidía, se corrigió el diseño o el código; queda anotado en `revision_ia.md`.

| Prompt | Diseño que se adjunta | Genera |
|---|---|---|
| `00_contexto_comun.md` | `arquitectura.md` | (se pega delante de todos los demás) |
| `01_arduino.md` | `arduino_design.md` | `src/arduino/` |
| `02_database.md` | `database_design.md` | `src/database/esquema.sql` |
| `03_business_logic.md` | `business_logic_design.md` + `database_design.md` | `src/business_logic/` |
| `04_communication.md` | `communication_design.md` + `business_logic_design.md` | `src/communication/` |
| `05_frontend_business_logic.md` | `frontend_business_logic_design.md` + `communication_design.md` | `src/frontend_business_logic/` |
| `06_gui.md` | `gui_design.md` + `frontend_business_logic_design.md` | `src/gui/` |
| `07_android.md` | `android_design.md` + `communication_design.md` + `arduino_design.md` (protocolo) | `src/android/` |

Herramienta: Claude (Anthropic). Los prompts están escritos para que **otra IA, sin contexto previo**, genere el mismo componente.
