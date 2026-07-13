package com.example.agroag_mro.models

import android.R
import com.google.gson.annotations.SerializedName

data class OrdenesRequest (
    val login: Login,
    @SerializedName("Fecha_Inicio") val fi: String,
    @SerializedName("Fecha_Fin") val ff: String,
    @SerializedName("Sucursal") val suc: String,
    @SerializedName("atrasadas") val atras: String,
    @SerializedName("Browser") val bus: String
)

// para solo traerordenes del activo delusuario
data class OrdenesRequestActivo (
    val login: Login,
    @SerializedName("Activo") val activo: String,
)

// para envio de ordenes porusuario en activity validaordenes
data class OrdenesRequestUsuario (
    val login: Login,
    val validacion: String? = null,
   val ordenes: List<SendOrdenes>
)
data class SendOrdenes (
   val docto: String,
    val comentario: String
)

//para la busqeuda de documento
data class DoctosMROrequest(
    val login: Login,
    val suc: String,
    val orden: String
)