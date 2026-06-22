package com.example.lacteos_flores.models

data class AltaDoctosResponse (
    val ResponseAlta: List<DoctosRes>
)

data class DoctosRes (
    val ok: String?,
    val msn: String?,
    val doc: String?,
    val folio: String?
)
