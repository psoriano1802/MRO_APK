package com.example.lacteos_flores.models

data class TipoTrabajos(
    val descripcion: String,
    val clave : String
){
    override fun toString(): String = descripcion
}