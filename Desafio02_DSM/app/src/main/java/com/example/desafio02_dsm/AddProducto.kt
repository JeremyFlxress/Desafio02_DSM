package com.example.desafio02_dsm

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.desafio02_dsm.model.Producto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.desafio02_dsm.FirebaseManager

class AddProducto : AppCompatActivity() {

    private lateinit var nombreEditText: EditText
    private lateinit var descripcionEditText: EditText
    private lateinit var precioEditText: EditText
    private lateinit var stockEditText: EditText
    private lateinit var btnGuardar: Button

    private var database: DatabaseReference? = null
    private var productoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_producto)

        database = FirebaseManager.getProductosRef()
        if (database == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        if (database == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        nombreEditText = findViewById(R.id.etNombre)
        descripcionEditText = findViewById(R.id.etDescripcion)
        precioEditText = findViewById(R.id.etPrecio)
        stockEditText = findViewById(R.id.etStock)
        btnGuardar = findViewById(R.id.btnGuardar)

        productoId = intent.getStringExtra("producto_id")

        if (productoId != null) {
            supportActionBar?.title = "Editar Producto"
            loadProductData()
        } else {
            supportActionBar?.title = "Agregar Producto"
        }

        btnGuardar.setOnClickListener {
            saveProduct()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun loadProductData() {
        productoId?.let {
            database?.child(it)?.get()?.addOnSuccessListener { snapshot ->
                val producto = snapshot.getValue(Producto::class.java)
                if (producto != null) {
                    nombreEditText.setText(producto.nombre)
                    descripcionEditText.setText(producto.descripcion)
                    precioEditText.setText(producto.precio.toString())
                    stockEditText.setText(producto.stock.toString())
                }
            }?.addOnFailureListener{
                Toast.makeText(this, "Error al cargar el producto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProduct() {
        val nombre = nombreEditText.text.toString().trim()
        val descripcion = descripcionEditText.text.toString().trim()
        val precioStr = precioEditText.text.toString().trim()
        val stockStr = stockEditText.text.toString().trim()

        if (nombre.isEmpty() || descripcion.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val precio = precioStr.toDoubleOrNull()
        val stock = stockStr.toIntOrNull()

        if (precio == null || stock == null) {
            Toast.makeText(this, "Precio o stock inválido", Toast.LENGTH_SHORT).show()
            return
        }

        val producto = Producto(nombre = nombre, descripcion = descripcion, precio = precio, stock = stock)

        if (productoId != null) {
            // Actualizar producto existente
            productoId?.let {
                database?.child(it)?.setValue(producto)?.addOnCompleteListener {
                    Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        } else {
            // Guardar nuevo producto
            val newProductoId = database?.push()?.key
            if (newProductoId != null) {
                database?.child(newProductoId)?.setValue(producto.copy(id = newProductoId))?.addOnCompleteListener {
                    Toast.makeText(this, "Producto guardado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Error al crear el producto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}