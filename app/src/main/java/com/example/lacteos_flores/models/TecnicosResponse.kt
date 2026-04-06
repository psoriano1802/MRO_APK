package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class TecnicosResponse(
    val ResponseTecnicos: List<itemTecnico>
)
data class itemTecnico(
    val ok: String?= null,
    @SerializedName("Clave") val cve: String?= null,
    @SerializedName("ApellidoP") val ap: String?= null,
    @SerializedName("ApellidoM") val am: String?= null,
    @SerializedName("Nombre") val name: String?= null,
    @SerializedName("TecnicoServicio") val tecser: String?= null,
    @SerializedName("PermiteSacarRef") val permiteref: String?= null,
    @SerializedName("Sucursal") val suc: String?= null,
    @SerializedName("Almacen") val alm: String?= null,
    @SerializedName("Departamento") val depto: String?= null,
    @SerializedName("Err") val err: String?= null
)