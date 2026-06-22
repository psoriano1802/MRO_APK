package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class AltaGastoRequest(
    val login: Login,
    @SerializedName("RFC") val rfc: String? = "PLF010228TC3",
    @SerializedName("Vendedor") val vendedor: String,
    @SerializedName("Gasto") val gasto: String,
    @SerializedName("Monto") val monto: String,
    @SerializedName("Observaciones") val observaciones: String
)
