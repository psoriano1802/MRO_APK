package com.example.lacteos_flores.models

data class ValidaRecargaResponse(
    val ValidaRecargaResponse: List<ValidaRecargaResult>? = null
)

data class ValidaRecargaResult(
    val ok: String? = null,
    val msn: String? = null
)