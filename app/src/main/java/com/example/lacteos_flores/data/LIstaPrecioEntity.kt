package com.example.lacteos_flores.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "listaprecios", primaryKeys = ["listaid", "clave"])
data class ListaPreciosEntity(
    val listaid: String,
    val clave: String,
    val precio: String
)