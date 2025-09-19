package com.example.desafio02_dsm.model

data class Producto(
    var id: String? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val precio: Double? = null,
    val stock: Int? = null
)
