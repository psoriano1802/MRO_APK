package com.example.lacteos_flores.models

data class ReporteFallaRequest(
    val Login: Login,
    val folio: String,
    val tipo_manto: String,
    val tipo_trabajo: String,
    val activo: String,
    val falla: String,
    val detalle: String,
    val observacion: String,
    val fecha: String,
    val imagenes: List<imagen>?  //validar si se manda por separado o en el mismo request
)

data class imagen(
    val img: String

)