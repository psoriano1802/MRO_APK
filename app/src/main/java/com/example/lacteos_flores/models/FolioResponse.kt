package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class FolioResponse(
    val ResponseFolios: List<folio>? = null
)
data class folio(
    val ok: String? = null,
    @SerializedName("Folio") val folio: String? = null,
    @SerializedName("Err") val err: String? = null
)