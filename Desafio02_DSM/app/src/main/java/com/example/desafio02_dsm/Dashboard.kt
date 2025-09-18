package com.example.desafio02_dsm

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Dashboard : AppCompatActivity() {

    private lateinit var cardProductos: LinearLayout
    private lateinit var cardClientes: LinearLayout
    private lateinit var cardVentas: LinearLayout
    private lateinit var sectionProductos: TextView
    private lateinit var sectionClientes: TextView
    private lateinit var sectionVentas: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        // Referencias UI
        cardProductos = findViewById(R.id.cardProductos)
        cardClientes = findViewById(R.id.cardClientes)
        cardVentas = findViewById(R.id.cardVentas)
        sectionProductos = findViewById(R.id.sectionProductos)
        sectionClientes = findViewById(R.id.sectionClientes)
        sectionVentas = findViewById(R.id.sectionVentas)

        // Click listeners para las cards de estadísticas
        cardProductos.setOnClickListener {
            startActivity(Intent(this, Productos::class.java))
        }

        cardClientes.setOnClickListener {
            startActivity(Intent(this, Clientes::class.java))
        }

        cardVentas.setOnClickListener {
            startActivity(Intent(this, Ventas::class.java))
        }

        // Click listeners para las secciones
        sectionProductos.setOnClickListener {
            startActivity(Intent(this, Productos::class.java))
        }

        sectionClientes.setOnClickListener {
            startActivity(Intent(this, Clientes::class.java))
        }

        sectionVentas.setOnClickListener {
            startActivity(Intent(this, Ventas::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}