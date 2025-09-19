package com.example.desafio02_dsm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.desafio02_dsm.model.Producto
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class Productos : AppCompatActivity() {

    private lateinit var fabAgregarProducto: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyProductos: TextView
    private lateinit var adapter: ProductoAdapter
    private val productosList = mutableListOf<Producto>()
    private var productosRef: DatabaseReference? = null
    private lateinit var eventListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos)

        try {
            fabAgregarProducto = findViewById(R.id.fabAgregarProducto)
            recyclerView = findViewById(R.id.recyclerViewProductos)
            tvEmptyProductos = findViewById(R.id.tvEmptyProductos)
            recyclerView.layoutManager = LinearLayoutManager(this)
            
            adapter = ProductoAdapter(productosList, {
                // Click en Editar
                val intent = Intent(this@Productos, AddProducto::class.java)
                intent.putExtra("producto_id", it.id)
                startActivity(intent)
            }, {
                // Click en Eliminar
                FirebaseManager.getProductosRef()?.child(it.id!!)?.removeValue()
                Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
            })
            recyclerView.adapter = adapter

            fabAgregarProducto.setOnClickListener {
                val intent = Intent(this, AddProducto::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }

            setupFirebaseListener()

        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun setupFirebaseListener() {
        productosRef = FirebaseManager.getProductosRef()
        if (productosRef == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            checkEmptyState()
            return
        }

        eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productosList.clear()
                for (productoSnapshot in snapshot.children) {
                    val producto = productoSnapshot.getValue(Producto::class.java)
                    producto?.id = productoSnapshot.key
                    if (producto != null) {
                        productosList.add(producto)
                    }
                }
                adapter.notifyDataSetChanged()
                checkEmptyState()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Productos, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        productosRef?.addValueEventListener(eventListener)
    }

    private fun checkEmptyState() {
        if (productosList.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmptyProductos.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmptyProductos.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        productosRef?.removeEventListener(eventListener)
    }
}
