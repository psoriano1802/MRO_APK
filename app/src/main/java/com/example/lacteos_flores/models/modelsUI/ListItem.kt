package com.example.lacteos_flores.models.modelsUI

sealed class ListItem {
    data class Header(val titulo: String) : ListItem()
    data class Refaccion(
        val id: Int,
        val nombre: String,
        val precio: Double
    ) : ListItem()
}
