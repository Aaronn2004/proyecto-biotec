<?php
// ==============================================================
// Fichero:     Logica.php
// Descripción: Lógica del negocio. Único punto que accede a la base
//              de datos (tabla medicion, ver doc/database_design.md).
//              Recibe la conexión ya creada (PDO): se usa igual en
//              producción que desde los tests con otra BD.
// Diseño:      doc/business_logic_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================

// Errores del dominio (ver "Errores" en business_logic_design.md)
class DatosNoValidosException extends InvalidArgumentException {}
class MedicionRepetidaException extends RuntimeException {}
class ErrorDeAlmacenamientoException extends RuntimeException {}

class Logica {

    // Las fechas se guardan en hora local de Madrid (cambia sola entre
    // verano, UTC+2, e invierno, UTC+1).
    private const ZONA_HORARIA = 'Europe/Madrid';
    // TipoMedicion = { O3 = 11, TEMPERATURA = 12, RUIDO = 13 }
    private const TIPOS_VALIDOS = [11, 12, 13];
    private const LONGITUD_MAXIMA_DISPOSITIVO = 16;
    private const CODIGO_CLAVE_REPETIDA = '23000';

    private PDO $laBD;
    private $reloj;

    // --------------------------------------------------------------
    // laBD: ConexionBD, reloj: Reloj | null --> Logica()
    // Reloj = ahora() --> Text ("2026-10-14 10:45:12.345", hora de Madrid).
    // El reloj solo se pasa en los tests, para fijar la hora.
    // --------------------------------------------------------------
    public function __construct(PDO $laBD, ?callable $reloj = null) {
        $this->laBD = $laBD;
        $this->laBD->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        $this->reloj = $reloj ?? fn() =>
            (new DateTimeImmutable('now', new DateTimeZone(self::ZONA_HORARIA)))->format('Y-m-d H:i:s.v');
    }

    // --------------------------------------------------------------
    // m: MedicionNueva --> guardarMedicion() --> Medicion
    //
    // MedicionNueva = (dispositivo: Text, tipo: N, valor: R)
    // Medicion      = (dispositivo: Text, tipo: N, valor: R, fecha: Text)
    //   fecha en ISO 8601 con milisegundos y diferencia horaria:
    //   "2026-10-14T10:45:12.345+02:00"
    // Lanza DatosNoValidos, MedicionRepetida o ErrorDeAlmacenamiento.
    // --------------------------------------------------------------
    public function guardarMedicion($m): array {
        self::comprobarMedicionNueva($m);

        $fecha = ($this->reloj)();
        try {
            $sentencia = $this->laBD->prepare(
                'INSERT INTO medicion (dispositivo, tipo, valor, fecha) VALUES (?, ?, ?, ?)');
            $sentencia->execute([$m['dispositivo'], $m['tipo'], $m['valor'], $fecha]);
        } catch (PDOException $error) {
            if ($error->getCode() === self::CODIGO_CLAVE_REPETIDA) {
                throw new MedicionRepetidaException('ya hay una medición de ese dispositivo en ese instante');
            }
            throw new ErrorDeAlmacenamientoException('no se pudo guardar la medición', 0, $error);
        }

        return [
            'dispositivo' => $m['dispositivo'],
            'tipo' => $m['tipo'],
            'valor' => (float) $m['valor'],
            'fecha' => self::fechaIso($fecha),
        ];
    }

    // --------------------------------------------------------------
    // tipo: N | null --> leerUltimaMedicion() --> Medicion | null
    // Sin tipo (null): la última de cualquier tipo. Con tipo: la última de ese tipo.
    // Lanza ErrorDeAlmacenamiento si falla la BD.
    // --------------------------------------------------------------
    public function leerUltimaMedicion(?int $tipo = null): ?array {
        $columnas = 'SELECT dispositivo, tipo, valor, fecha FROM medicion';
        try {
            if ($tipo === null) {
                $sentencia = $this->laBD->prepare($columnas . ' ORDER BY fecha DESC LIMIT 1');
                $sentencia->execute();
            } else {
                $sentencia = $this->laBD->prepare($columnas . ' WHERE tipo = ? ORDER BY fecha DESC LIMIT 1');
                $sentencia->execute([$tipo]);
            }
            $fila = $sentencia->fetch(PDO::FETCH_ASSOC);
        } catch (PDOException $error) {
            throw new ErrorDeAlmacenamientoException('no se pudo leer de la base de datos', 0, $error);
        }
        if ($fila === false) {
            return null;
        }
        return [
            'dispositivo' => $fila['dispositivo'],
            'tipo' => (int) $fila['tipo'],
            'valor' => (float) $fila['valor'],
            'fecha' => self::fechaIso($fila['fecha']),
        ];
    }

    // --------------------------------------------------------------
    // m: MedicionNueva --> comprobarMedicionNueva()   (privado, estático)
    // Lanza DatosNoValidos si m no es válida.
    // --------------------------------------------------------------
    private static function comprobarMedicionNueva($m): void {
        if (!is_array($m)) {
            throw new DatosNoValidosException('la medición debe ser un objeto');
        }
        if (!isset($m['dispositivo']) || !is_string($m['dispositivo'])
                || trim($m['dispositivo']) === ''
                || strlen($m['dispositivo']) > self::LONGITUD_MAXIMA_DISPOSITIVO) {
            throw new DatosNoValidosException('dispositivo debe ser un texto de 1 a 16 caracteres');
        }
        if (!isset($m['tipo']) || !in_array($m['tipo'], self::TIPOS_VALIDOS, true)) {
            throw new DatosNoValidosException('tipo debe ser 11 (O3), 12 (temperatura) o 13 (ruido)');
        }
        if (!isset($m['valor']) || !(is_int($m['valor']) || is_float($m['valor']))
                || !is_finite((float) $m['valor'])) {
            throw new DatosNoValidosException('valor debe ser un número');
        }
    }

    // --------------------------------------------------------------
    // fechaBD: Text --> fechaIso() --> Text   (privado, estático)
    //   "2026-10-14 10:45:12.345" (hora de Madrid) -> "2026-10-14T10:45:12.345+02:00"
    // Añadir la diferencia con UTC permite que cualquier cliente la
    // interprete sin ambigüedad.
    // --------------------------------------------------------------
    private static function fechaIso(string $fechaBD): string {
        return (new DateTimeImmutable($fechaBD, new DateTimeZone(self::ZONA_HORARIA)))->format('Y-m-d\TH:i:s.vP');
    }
}
