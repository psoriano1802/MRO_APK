package com.example.lacteos_flores.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gastos")//catalogos
data class GastosEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clave: String,
    val descripcion: String
)