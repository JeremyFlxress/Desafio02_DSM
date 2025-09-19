package com.example.desafio02_dsm

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class Dashboard : AppCompatActivity() {

    // UI References
    private lateinit var tvUserName: TextView
    private lateinit var tvProductosCount: TextView
    private lateinit var tvClientesCount: TextView
    private lateinit var tvVentasCount: TextView
    private lateinit var cardProductos: CardView
    private lateinit var cardClientes: CardView
    private lateinit var cardVentas: CardView

    // Firebase References
    private var userRef: DatabaseReference? = null
    private var productosRef: DatabaseReference? = null
    private var clientesRef: DatabaseReference? = null
    private var ventasRef: DatabaseReference? = null

    // Listeners
    private val listeners = mutableMapOf<DatabaseReference, ValueEventListener>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Init UI
        tvUserName = findViewById(R.id.tvUserName)
        tvProductosCount = findViewById(R.id.tvProductosCount)
        tvClientesCount = findViewById(R.id.tvClientesCount)
        tvVentasCount = findViewById(R.id.tvVentasCount)
        cardProductos = findViewById(R.id.cardProductos)
        cardClientes = findViewById(R.id.cardClientes)
        cardVentas = findViewById(R.id.cardVentas)

        // Setup Click Listeners
        cardProductos.setOnClickListener { startActivity(Intent(this, Productos::class.java)) }
        cardClientes.setOnClickListener { startActivity(Intent(this, Clientes::class.java)) }
        cardVentas.setOnClickListener { startActivity(Intent(this, Ventas::class.java)) }

        loadDashboardData()
    }

    private fun loadDashboardData() {
        val user = FirebaseManager.getCurrentUserId()
        if (user == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_LONG).show()
            return
        }

        // Get references
        userRef = FirebaseManager.getUserRootRef()
        productosRef = FirebaseManager.getProductosRef()
        clientesRef = FirebaseManager.getClientesRef()
        ventasRef = FirebaseManager.getVentasRef()

        // Load User Name
        attachListener(userRef) { snapshot ->
            val nombre = snapshot.child("nombre").getValue(String::class.java)
            tvUserName.text = nombre ?: "Sin Nombre"
        }

        // Load Products Count
        attachListener(productosRef) { snapshot ->
            tvProductosCount.text = snapshot.childrenCount.toString()
        }

        // Load Clients Count
        attachListener(clientesRef) { snapshot ->
            tvClientesCount.text = snapshot.childrenCount.toString()
        }

        // Load Sales Count
        attachListener(ventasRef) { snapshot ->
            tvVentasCount.text = snapshot.childrenCount.toString()
        }
    }

    private fun attachListener(dbRef: DatabaseReference?, onDataChange: (DataSnapshot) -> Unit) {
        dbRef?.let {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onDataChange(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Dashboard, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
            it.addValueEventListener(listener)
            listeners[it] = listener
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detach all listeners to prevent memory leaks
        listeners.forEach { (ref, listener) ->
            ref.removeEventListener(listener)
        }
    }
}