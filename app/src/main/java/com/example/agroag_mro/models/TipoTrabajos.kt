package com.example.agroag_mro.models

data class TipoTrabajos(
    val descripcion: String,
    val clave : String
){
    override fun toString(): String = descripcion
}