package com.example.lacteos_flores.models

data class DocumentoHistorial(
    val id: Long,
    val gen: String,
    val nat: String,
    val grp: String,
    val tip: String,
    val staSinc: String,
    val folioKepler: String?,
    val fecha: String,
    val cliente: String,
    val monto: String,
    val descripcion: String?
)
