<?php
// ==============================================================
// config.example.php  ->  cópialo como config.php EN EL SERVIDOR
// y pon tus datos de Plesk (Bases de datos > tu BD > usuario).
// config.php NO se sube a Git (está en .gitignore): lleva la contraseña.
// ==============================================================
return [
    // En Plesk el servidor de BD suele ser "localhost"
    'dsn' => 'mysql:host=localhost;port=3306;dbname=NOMBRE_DE_TU_BD;charset=utf8mb4',
    'usuario' => 'USUARIO_DE_TU_BD',
    'clave' => 'CONTRASEÑA_DE_TU_BD',
];
