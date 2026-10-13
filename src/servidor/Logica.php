<?php
// ==============================================================
// Logica.php
// Lógica del negocio: único punto que accede a la base de datos.
// No sabe nada de HTTP: recibe la conexión ya creada (PDO), así se
// puede usar desde la API REST o desde los tests con otra BD.
// ==============================================================

// Se lanza cuando ya hay una medición del mismo dispositivo en el
// mismo instante (la clave primaria es dispositivo + fecha).
class MedicionRepetidaException extends RuntimeException {}

class Logica {

    // Las fechas se guardan en hora local de Madrid (cambia sola entre
    // verano, UTC+2, e invierno, UTC+1).
    private const ZONA_HORARIA = 'Europe/Madrid';
    private const LONGITUD_MAXIMA_DISPOSITIVO = 16;

    private PDO $laBD;
    private $reloj;

    // --------------------------------------------------------------
    // laBD: PDO, [reloj: Funcion --> DateTimeImmutable] --> Logica()
    // El reloj solo se pasa en los tests, para fijar la hora.
    // --------------------------------------------------------------
    public function __construct(PDO $laBD, ?callable $reloj = null) {
        $this->laBD = $laBD;
        $this->laBD->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        $this->reloj = $reloj ?? fn() => new DateTimeImmutable('now', new DateTimeZone(self::ZONA_HORARIA));
    }

    // --------------------------------------------------------------
    // medicion: MedicionNueva --> guardarMedicion() --> Medicion
    //
    // MedicionNueva = (dispositivo: Text, tipo: N, valor: R)
    // Medicion      = (dispositivo: Text, tipo: N, valor: R, fecha: Text)
    //   fecha en ISO 8601 con milisegundos y diferencia horaria:
    //   "2026-10-13T11:50:14.237+02:00"
    // Lanza InvalidArgumentException si la medición no es válida y
    // MedicionRepetidaException si ya existe (dispositivo, fecha).
    // --------------------------------------------------------------
    public function guardarMedicion($medicion): array {
        self::comprobarMedicionNueva($medicion);

        $ahora = ($this->reloj)()->setTimezone(new DateTimeZone(self::ZONA_HORARIA));
        $fecha = $ahora->format('Y-m-d H:i:s.v');
        try {
            $sentencia = $this->laBD->prepare(
                'INSERT INTO medicion (dispositivo, tipo, valor, fecha) VALUES (?, ?, ?, ?)');
            $sentencia->execute([$medicion['dispositivo'], $medicion['tipo'], $medicion['valor'], $fecha]);
        } catch (PDOException $error) {
            if ($error->getCode() === '23000') { // clave primaria repetida
                throw new MedicionRepetidaException('ya hay una medición de ese dispositivo en ese instante');
            }
            throw $error;
        }

        return [
            'dispositivo' => $medicion['dispositivo'],
            'tipo' => $medicion['tipo'],
            'valor' => (float) $medicion['valor'],
            'fecha' => self::fechaIso($fecha),
        ];
    }

    // --------------------------------------------------------------
    // [tipo: N] --> leerUltimaMedicion() --> Medicion | null
    // Sin tipo: la última de cualquier tipo. Con tipo: la última de ese tipo.
    // --------------------------------------------------------------
    public function leerUltimaMedicion(?int $tipo = null): ?array {
        $columnas = 'SELECT dispositivo, tipo, valor, fecha FROM medicion';
        if ($tipo === null) {
            $sentencia = $this->laBD->prepare($columnas . ' ORDER BY fecha DESC LIMIT 1');
            $sentencia->execute();
        } else {
            $sentencia = $this->laBD->prepare($columnas . ' WHERE tipo = ? ORDER BY fecha DESC LIMIT 1');
            $sentencia->execute([$tipo]);
        }
        $fila = $sentencia->fetch(PDO::FETCH_ASSOC);
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
    // medicion: MedicionNueva --> comprobarMedicionNueva()
    // --------------------------------------------------------------
    private static function comprobarMedicionNueva($medicion): void {
        if (!is_array($medicion)) {
            throw new InvalidArgumentException('la medición debe ser un objeto');
        }
        if (!isset($medicion['dispositivo']) || !is_string($medicion['dispositivo'])
                || trim($medicion['dispositivo']) === ''
                || strlen($medicion['dispositivo']) > self::LONGITUD_MAXIMA_DISPOSITIVO) {
            throw new InvalidArgumentException('dispositivo debe ser un texto de 1 a 16 caracteres');
        }
        if (!isset($medicion['tipo']) || !is_int($medicion['tipo']) || $medicion['tipo'] < 0 || $medicion['tipo'] > 255) {
            throw new InvalidArgumentException('tipo debe ser un entero entre 0 y 255');
        }
        if (!isset($medicion['valor']) || !(is_int($medicion['valor']) || is_float($medicion['valor']))
                || !is_finite((float) $medicion['valor'])) {
            throw new InvalidArgumentException('valor debe ser un número');
        }
    }

    // --------------------------------------------------------------
    // fechaBD: Text ("2026-10-13 11:50:14.237", hora de Madrid)
    //    --> fechaIso() --> Text ("2026-10-13T11:50:14.237+02:00")
    // Añadir la diferencia con UTC permite que cualquier cliente (la web,
    // un móvil en otro país) la interprete sin ambigüedad.
    // --------------------------------------------------------------
    private static function fechaIso(string $fechaBD): string {
        return (new DateTimeImmutable($fechaBD, new DateTimeZone(self::ZONA_HORARIA)))->format('Y-m-d\TH:i:s.vP');
    }
}
