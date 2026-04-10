package com.example.lacteos_flores.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "existencias", primaryKeys = ["clave", "auxiliar"])
data class ExistenciaEntity(
    val clave: String,
    val auxiliar: String,
    val existencias: String

)