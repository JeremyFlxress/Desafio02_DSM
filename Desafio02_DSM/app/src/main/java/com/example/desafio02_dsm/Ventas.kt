package com.example.desafio02_dsm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.desafio02_dsm.model.Cliente
import com.example.desafio02_dsm.model.Producto
import com.example.desafio02_dsm.model.Venta
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.desafio02_dsm.FirebaseManager

class Ventas : AppCompatActivity() {

    // UI Elements
    private lateinit var spinnerClientes: Spinner
    private lateinit var spinnerProductos: Spinner
    private lateinit var etCantidad: EditText
    private lateinit var btnAgregarProductoVenta: Button
    private lateinit var recyclerViewProductosVenta: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnConfirmarVenta: Button
    private lateinit var recyclerViewVentas: RecyclerView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // Data
    private val clientesList = mutableListOf<Cliente>()
    private val productosList = mutableListOf<Producto>()
    private val ventaProductosList = mutableListOf<Pair<Producto, Int>>()
    private lateinit var ventaProductosAdapter: VentaProductosAdapter
    private lateinit var ventasHistoryAdapter: FirebaseRecyclerAdapter<Venta, VentaViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ventas)

        // Initialize UI
        spinnerClientes = findViewById(R.id.spinnerClientes)
        spinnerProductos = findViewById(R.id.spinnerProductos)
        etCantidad = findViewById(R.id.etCantidad)
        btnAgregarProductoVenta = findViewById(R.id.btnAgregarProductoVenta)
        recyclerViewProductosVenta = findViewById(R.id.recyclerViewProductosVenta)
        tvTotal = findViewById(R.id.tvTotal)
        btnConfirmarVenta = findViewById(R.id.btnConfirmarVenta)
        recyclerViewVentas = findViewById(R.id.recyclerViewVentas)

        // Initialize Firebase
        database = FirebaseManager.getUserRootRef()!!

        // Setup RecyclerViews
        setupVentaProductosRecyclerView()
        setupVentasHistoryRecyclerView()

        // Load Spinners
        loadClientsSpinner()
        loadProductsSpinner()

        // Set Click Listeners
        btnAgregarProductoVenta.setOnClickListener { addProductToSale() }
        btnConfirmarVenta.setOnClickListener { confirmSale() }
    }

    private fun setupVentaProductosRecyclerView() {
        ventaProductosAdapter = VentaProductosAdapter(ventaProductosList)
        recyclerViewProductosVenta.layoutManager = LinearLayoutManager(this)
        recyclerViewProductosVenta.adapter = ventaProductosAdapter
    }

    private fun setupVentasHistoryRecyclerView() {
        recyclerViewVentas.layoutManager = LinearLayoutManager(this)
        val query = FirebaseManager.getVentasRef()
        if (query == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }
        val options = FirebaseRecyclerOptions.Builder<Venta>()
            .setQuery(query, Venta::class.java)
            .build()
        ventasHistoryAdapter = object : FirebaseRecyclerAdapter<Venta, VentaViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_venta, parent, false)
                return VentaViewHolder(view)
            }

            override fun onBindViewHolder(holder: VentaViewHolder, position: Int, model: Venta) {
                holder.bind(model)
            }
        }
        recyclerViewVentas.adapter = ventasHistoryAdapter
    }

    private fun loadClientsSpinner() {
        val clientsRef = FirebaseManager.getClientesRef()!!
        clientsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                clientesList.clear()
                val clientNames = mutableListOf<String>()
                for (clientSnapshot in snapshot.children) {
                    val cliente = clientSnapshot.getValue(Cliente::class.java)
                    if (cliente != null) {
                        clientesList.add(cliente)
                        clientNames.add(cliente.nombre ?: "")
                    }
                }
                val adapter = ArrayAdapter(this@Ventas, android.R.layout.simple_spinner_item, clientNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerClientes.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Ventas, "Error al cargar clientes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadProductsSpinner() {
        val productsRef = FirebaseManager.getProductosRef()!!
        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productosList.clear()
                val productNames = mutableListOf<String>()
                for (productSnapshot in snapshot.children) {
                    val producto = productSnapshot.getValue(Producto::class.java)
                    if (producto != null) {
                        productosList.add(producto)
                        productNames.add(producto.nombre ?: "")
                    }
                }
                val adapter = ArrayAdapter(this@Ventas, android.R.layout.simple_spinner_item, productNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerProductos.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Ventas, "Error al cargar productos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addProductToSale() {
        val selectedProductPosition = spinnerProductos.selectedItemPosition
        if (selectedProductPosition < 0 || selectedProductPosition >= productosList.size) {
            Toast.makeText(this, "Seleccione un producto", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedProduct = productosList[selectedProductPosition]
        val cantidadStr = etCantidad.text.toString()
        if (cantidadStr.isEmpty()) {
            Toast.makeText(this, "Ingrese una cantidad", Toast.LENGTH_SHORT).show()
            return
        }
        val cantidad = cantidadStr.toInt()

        if (cantidad <= 0) {
            Toast.makeText(this, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }

        if (cantidad > selectedProduct.stock!!) {
            Toast.makeText(this, "No hay suficiente stock", Toast.LENGTH_SHORT).show()
            return
        }

        ventaProductosList.add(Pair(selectedProduct, cantidad))
        ventaProductosAdapter.notifyDataSetChanged()
        calculateTotal()
        etCantidad.text.clear()
    }

    private fun calculateTotal() {
        var total = 0.0
        for ((producto, cantidad) in ventaProductosList) {
            total += producto.precio!! * cantidad
        }
        tvTotal.text = "Total: ${String.format("%.2f", total)}"
    }

    private fun confirmSale() {
        if (ventaProductosList.isEmpty()) {
            Toast.makeText(this, "Agregue productos a la venta", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedClientPosition = spinnerClientes.selectedItemPosition
        if (selectedClientPosition < 0 || selectedClientPosition >= clientesList.size) {
            Toast.makeText(this, "Seleccione un cliente", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedClient = clientesList[selectedClientPosition]

        val ventaId = FirebaseManager.getVentasRef()?.push()?.key
        if (ventaId == null) {
            Toast.makeText(this, "Error al crear la venta", Toast.LENGTH_SHORT).show()
            return
        }

        var total = 0.0
        val productosVendidos = mutableMapOf<String, Int>()
        for ((producto, cantidad) in ventaProductosList) {
            total += producto.precio!! * cantidad
            productosVendidos[producto.id!!] = cantidad
        }

        val venta = Venta(
            id = ventaId,
            clienteId = selectedClient.id,
            listaDeProductos = productosVendidos,
            total = total,
            fecha = System.currentTimeMillis()
        )

        FirebaseManager.getVentasRef()?.child(ventaId)?.setValue(venta)?.addOnCompleteListener {
            if (it.isSuccessful) {
                updateStock()
                Toast.makeText(this, "Venta registrada", Toast.LENGTH_SHORT).show()
                clearSaleData()
            } else {
                Toast.makeText(this, "Error al registrar la venta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStock() {
        for ((producto, cantidad) in ventaProductosList) {
            val newStock = producto.stock!! - cantidad
            FirebaseManager.getProductosRef()?.child(producto.id!!)
                ?.child("stock")?.setValue(newStock)
        }
    }

    private fun clearSaleData() {
        ventaProductosList.clear()
        ventaProductosAdapter.notifyDataSetChanged()
        tvTotal.text = "Total: $0.00"
    }

    override fun onStart() {
        super.onStart()
        ventasHistoryAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        ventasHistoryAdapter.stopListening()
    }
}


