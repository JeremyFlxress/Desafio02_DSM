package com.example.desafio02_dsm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistrarEmpleadoActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmar: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var tvIrLogin: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_empleado)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Referencias UI
        etNombre = findViewById(R.id.etNombreEmpleado)
        etEmail = findViewById(R.id.etEmailEmpleado)
        etPassword = findViewById(R.id.etPasswordEmpleado)
        etConfirmar = findViewById(R.id.etConfirmarPassword)
        btnRegistrar = findViewById(R.id.btnRegistrarEmpleado)
        tvIrLogin = findViewById(R.id.tvIrLogin)

        // Acción registrar
        btnRegistrar.setOnClickListener {
            registrarEmpleado()
        }

        // Volver a Login
        tvIrLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun registrarEmpleado() {
        val nombre = etNombre.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val pass = etPassword.text.toString().trim()
        val confirm = etConfirmar.text.toString().trim()

        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        if (pass != confirm) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                user?.let {
                    val userMap = hashMapOf(
                        "nombre" to nombre,
                        "email" to email
                    )
                    database.reference.child("usuarios").child(it.uid).setValue(userMap)
                }
                Toast.makeText(this, "Empleado registrado correctamente", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Error al registrar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}