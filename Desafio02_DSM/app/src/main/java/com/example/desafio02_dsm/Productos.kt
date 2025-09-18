package com.example.desafio02_dsm

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Productos : AppCompatActivity() {

    private lateinit var fabAgregarProducto: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_productos)

        try {
            // Referencias UI
            fabAgregarProducto = findViewById(R.id.fabAgregarProducto)

            // Click listener para el botón flotante
            fabAgregarProducto.setOnClickListener {
                try {
                    val intent = Intent(this, AddProducto::class.java)
                    // Evitar que se acumulen actividades en el stack
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al abrir AddProducto: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace() // Para ver el error en el logcat
                }
            }

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar la pantalla: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace() // Para ver el error en el logcat
        }
    }

    override fun onResume() {
        super.onResume()
        // Asegurarnos de que estamos en la actividad correcta
        if (!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
            finish()
            return
        }
    }
}