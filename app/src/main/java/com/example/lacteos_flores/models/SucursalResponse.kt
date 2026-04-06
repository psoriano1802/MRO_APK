package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class SucursalResponse(
    val ResponseSucursal: List<SucursalItem>

)
data class SucursalItem(
    val ok: String?,
    @SerializedName("Clave")val cve: String?,
    @SerializedName("Sucursal")val suc: String?,
    @SerializedName("Almacenes")val alma: List<AlmacenItem>?
)
data class AlmacenItem(
    @SerializedName("Clave")val cvea: String?,
    @SerializedName("Almacen")val alm: String?
)