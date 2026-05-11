package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class CarteraResponse (
    val CarteraResponse: List<itemCartera>
    )

data class itemCartera(
    val ok: String? = null,
    @SerializedName("Cliente") val cli: String? = null,
    @SerializedName("Cargo_Docto") val docto: String? = null,
    @SerializedName("Monto_Original") val monto: String? = null,
    @SerializedName("Saldo") val saldo: String? = null,
    @SerializedName("Fecha_Pago") val fecha: String? = null,
    @SerializedName("Credito") val credito: String? = null,
    @SerializedName("Abono") val abono: String? = null,
    @SerializedName("Dias") val dias: String? = null


)