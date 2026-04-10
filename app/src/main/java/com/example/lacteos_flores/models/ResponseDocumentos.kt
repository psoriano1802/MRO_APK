package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class ResponseDocumentos(
    val ResponseDocumentos: List<documentos>? = null
)
data class documentos(
    val ok: String? = null,
    @SerializedName("Genero") val gen: String? = null,
    @SerializedName("Naturaleza") val nat: String? = null,
    @SerializedName("Grupo") val grp: String? = null,
    @SerializedName("Tipo") val tipo: String? = null,
    @SerializedName("Descripcion") val descrp: String? = null,
    @SerializedName("ISR") val isr: String? = null,
    @SerializedName("IVA") val iva: String? = null,
    @SerializedName("Retenido") val ret: String? = null
)