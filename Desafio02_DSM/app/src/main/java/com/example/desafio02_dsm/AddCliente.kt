package com.example.desafio02_dsm

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.desafio02_dsm.model.Cliente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.desafio02_dsm.FirebaseManager

class AddCliente : AppCompatActivity() {

    private lateinit var nombreEditText: EditText
    private lateinit var correoEditText: EditText
    private lateinit var telefonoEditText: EditText
    private lateinit var btnGuardar: Button

    private var database: DatabaseReference? = null
    private var clienteId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_cliente)

        database = FirebaseManager.getClientesRef()
        if (database == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        nombreEditText = findViewById(R.id.etNombreCliente)
        correoEditText = findViewById(R.id.etCorreoCliente)
        telefonoEditText = findViewById(R.id.etTelefonoCliente)
        btnGuardar = findViewById(R.id.btnGuardarCliente)

        clienteId = intent.getStringExtra("cliente_id")

        if (clienteId != null) {
            supportActionBar?.title = "Editar Cliente"
            loadClientData()
        } else {
            supportActionBar?.title = "Agregar Cliente"
        }

        btnGuardar.setOnClickListener {
            saveClient()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun loadClientData() {
        clienteId?.let {
            database?.child(it)?.get()?.addOnSuccessListener { snapshot ->
                val cliente = snapshot.getValue(Cliente::class.java)
                if (cliente != null) {
                    nombreEditText.setText(cliente.nombre)
                    correoEditText.setText(cliente.correo)
                    telefonoEditText.setText(cliente.telefono)
                }
            }?.addOnFailureListener{
                Toast.makeText(this, "Error al cargar el cliente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveClient() {
        val nombre = nombreEditText.text.toString().trim()
        val correo = correoEditText.text.toString().trim()
        val telefono = telefonoEditText.text.toString().trim()

        if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val cliente = Cliente(nombre = nombre, correo = correo, telefono = telefono)

        if (clienteId != null) {
            // Actualizar cliente existente
            clienteId?.let {
                database?.child(it)?.setValue(cliente)?.addOnCompleteListener {
                    Toast.makeText(this, "Cliente actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        } else {
            // Guardar nuevo cliente
            val newClientId = database?.push()?.key
            if (newClientId != null) {
                database?.child(newClientId)?.setValue(cliente.copy(id = newClientId))?.addOnCompleteListener {
                    Toast.makeText(this, "Cliente guardado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Error al crear el cliente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}