package com.example.desafio02_dsm.model

data class Venta(
    val id: String? = null,
    val clienteId: String? = null,
    val listaDeProductos: Map<String, Int>? = null, // Map<ProductoId, Cantidad>
    val total: Double? = null,
    val fecha: Long? = null // Timestamp
)
