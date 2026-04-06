package com.example.lacteos_flores.models

data class DocumentosResponse(
    val ResposneDocumentos: List<docs>
)
data class docs(
    val ok: String?=null,
    val genero: String?=null,
    val naturaleza: String?=null,
    val grupo: String?=null,
    val tipo: String?=null,
    val descripcion: String?=null,
    val isr: String?=null,
    val iva: String?=null,
    val err: String?=null
)