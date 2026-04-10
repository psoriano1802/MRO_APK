package com.example.lacteos_flores.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bancos")//catalogos
data class BancoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clave: String,
    val banco: String
)