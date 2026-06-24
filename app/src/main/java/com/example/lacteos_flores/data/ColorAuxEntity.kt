package com.example.lacteos_flores.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "color_aux")
data class ColorAuxEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clave: String,
    val descripcion: String
)
