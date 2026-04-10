package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class ProductosResponse(
    val ResponseProductos: List<ItemProductos>? = null
)
data class ItemProductos(
    val ok: String? = null,
    @SerializedName("Clave") val cve: String? = null,
    @SerializedName("Descripcion") val name: String? = null,
    @SerializedName("CodigoBarras") val uni: String? = null,
    @SerializedName("Unidad") val price: String? = null,
    @SerializedName("PrimerPrecio") val nat: String? = null,
    @SerializedName("IVA") val tst: String? = null,
    @SerializedName("UbicacionAlmacen") val cxh: String? = null,
    @SerializedName("BanderaSustituto") val cxo: String? = null,
    @SerializedName("Serie") val npers: String? = null,
    @SerializedName("LoteSF") val costun: String? = null,
    @SerializedName("TMC") val tmc: String? = null,
    @SerializedName("Ubicacion") val ubicacion: String? = null,
    @SerializedName("Pedimento") val pedimento: String? = null,
    @SerializedName("Err") val error: String? = null
)