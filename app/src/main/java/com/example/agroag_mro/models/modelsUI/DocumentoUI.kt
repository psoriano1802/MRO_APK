package com.example.agroag_mro.models.modelsUI

data class DocumentoUI(
    var documento: String,
    var tipoOrden: String,
    var estatus: String,
    var activo: String,
    var descripcion: String,
    var paquete: String,
    var descripcionPaquete: String,
    var causa: String,
    var validar: Boolean,
    var comentario: String
)