package com.example.lacteos_flores.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")//catalogos
data class ClientsEntity(
    @PrimaryKey val clave: String,
    val nombre: String,
    val rfc: String,
    val limcre: String,
    val plazo: String,
    val calle: String,
    val colo: String,
    val pobl: String,
    val tel: String,
    val cp: String,
    val agente: String,
    val latitud: String,
    val longitud: String,
    val flunes: String,
    val fmartes: String,
    val fmiercoles: String,
    val fjueves: String,
    val fviernes: String,
    val fsabado: String,
    val fdomingo: String,
    val lunes: String,
    val martes: String,
    val miercoles: String,
    val jueves: String,
    val viernes: String,
    val sabado: String,
    val domingo: String,
    val descuentop: String,
    val comentarios: String,
    val listaprecio: String
    )