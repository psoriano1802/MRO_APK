package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class IniciaDiaResponse (
    val IniciaDiaResponse: List<IniDia>? = null
)
data class IniDia (
    val ok: String? = null,
    @SerializedName("msn") val msn: String? = null,
    @SerializedName("ID_ACTUAL") val idac: String? = null,
    @SerializedName("FECHA_ACTUAL") val feac: String? = null,
    @SerializedName("Err") val err: String? = null
)

//terminar el dia o jornada
data class TerminaDiaResponse (
    val TerminaDiaResponse: List<TerDia>? = null
)
data class TerDia (
    val ok: String? = null,
    @SerializedName("msn") val msn: String? = null,
    @SerializedName("Fecha_Termina") val err: String? = null
)