package com.example.agroag_mro.data


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class UsuarioEntity(
    @PrimaryKey val usuario: String,
    val cve_suc: String,
    val sucursal: String,
    val cve_alma: String,
    val almacen: String

)
