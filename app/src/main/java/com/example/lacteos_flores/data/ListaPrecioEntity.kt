package com.example.lacteos_flores.data

import androidx.room.Entity


@Entity(tableName = "listaprecios", primaryKeys = ["listaid", "clave"])
data class ListaPreciosEntity(
    val listaid: String,
    val clave: String,
    val precio: String,
    val unidad: String,
    val comentario: String,
    val zona: String
)