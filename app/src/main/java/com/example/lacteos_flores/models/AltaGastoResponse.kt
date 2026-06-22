package com.example.lacteos_flores.models

data class AltaGastoResponse(
    val Registra_GastosResponse: List<GastoRes>? = null
)

data class GastoRes(
    val ok: String?
)
