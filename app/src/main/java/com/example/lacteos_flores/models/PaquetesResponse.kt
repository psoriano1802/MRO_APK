package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class PaquetesResponse(
    val ResponsePaquetes: List<itemPaqMO>
)
data class itemPaqMO(
    val ok: String? = null,
    @SerializedName("ClavePaq") val cvepq: String? = null,
    @SerializedName("ClaveRef") val cver: String? = null,
    @SerializedName("NombreRef") val namer: String? = null,
    @SerializedName("CantRefNecesarias") val cant: String? = null,
    @SerializedName("Unidad") val uni: String? = null,
    @SerializedName("PrecioUni") val price: String? = null,
    //solo para mano de obra
    @SerializedName("Consecutivo") val conse: String? = null,
    @SerializedName("CveManoObra") val cvem: String? = null,
    @SerializedName("NombreMO") val namemo: String? = null,
    @SerializedName("TiempoEst") val tespe: String? = null,
    @SerializedName("NumPersonas") val npers: String? = null,
    @SerializedName("Err") val err: String? = null
)