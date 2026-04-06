package com.example.lacteos_flores.models

data class ReporteFallaResponse (
    val ResponseReporteFalla: List<reporteFallaItem>? = null
    )
data class reporteFallaItem(
    val folio: String? = null,
    val fecha: String? = null,
    val activo: String? = null,
    val ok: String? = null,
    val err: String? = null
)
