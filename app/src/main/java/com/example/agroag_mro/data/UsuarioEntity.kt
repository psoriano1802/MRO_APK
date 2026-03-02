package com.example.agroag_mro.data


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class UsuarioEntity(
    @PrimaryKey val usuario: String,
    val nombre: String,
    val perfil_mro: String,
    val sucursal: String

)
