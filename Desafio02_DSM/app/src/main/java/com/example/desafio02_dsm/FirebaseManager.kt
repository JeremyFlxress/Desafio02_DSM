package com.example.desafio02_dsm

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getUserRootRef(): DatabaseReference? {
        val userId = getCurrentUserId()
        return if (userId != null) {
            database.reference.child("usuarios").child(userId)
        } else {
            null
        }
    }

    fun getProductosRef(): DatabaseReference? {
        return getUserRootRef()?.child("productos")
    }

    fun getClientesRef(): DatabaseReference? {
        return getUserRootRef()?.child("clientes")
    }

    fun getVentasRef(): DatabaseReference? {
        return getUserRootRef()?.child("ventas")
    }
}
