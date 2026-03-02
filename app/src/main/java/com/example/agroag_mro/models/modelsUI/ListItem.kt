package com.example.agroag_mro.models.modelsUI

sealed class ListItem {
    data class Header(val titulo: String) : ListItem()
    data class Refaccion(
        val id: Int,
        val nombre: String,
        val precio: Double
    ) : ListItem()
}
