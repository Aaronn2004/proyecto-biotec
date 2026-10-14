<?php
// ==============================================================
// Fichero:     config.example.php
// Descripción: Plantilla de la conexión a la BD. Cópialo como
//              config.php EN EL SERVIDOR (carpeta communication/)
// y pon tus datos de Plesk (Bases de datos > tu BD > usuario).
// config.php NO se sube a Git (está en .gitignore): lleva la contraseña.
// Diseño:      doc/communication_design.md
// Autor:       Aaron Blasco Blay
// Fecha:       14/10/2026
// Copyright:   (c) 2026 Aaron Blasco Blay - GTI, UPV
// ==============================================================
return [
    // En Plesk el servidor de BD suele ser "localhost"
    'dsn' => 'mysql:host=localhost;port=3306;dbname=NOMBRE_DE_TU_BD;charset=utf8mb4',
    'usuario' => 'USUARIO_DE_TU_BD',
    'clave' => 'CONTRASEÑA_DE_TU_BD',
];
