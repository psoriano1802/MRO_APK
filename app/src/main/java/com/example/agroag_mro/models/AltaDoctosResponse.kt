package com.example.agroag_mro.models

data class AltaDoctosResponse (
    val ResponseAltaMR: List<DoctosRes>
)
data class DoctosRes (
    val ok: String?,
    val msn: String?,
    val doc: String?,
    val folio: String?,
    val err: String?,
    val noerr: String?
)