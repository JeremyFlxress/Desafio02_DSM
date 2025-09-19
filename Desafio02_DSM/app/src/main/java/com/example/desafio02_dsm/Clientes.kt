package com.example.desafio02_dsm

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.desafio02_dsm.model.Cliente
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class Clientes : AppCompatActivity() {

    private lateinit var fabAgregarCliente: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyClientes: TextView
    private lateinit var adapter: ClienteAdapter
    private val clientesList = mutableListOf<Cliente>()
    private var clientesRef: DatabaseReference? = null
    private lateinit var eventListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clientes)

        fabAgregarCliente = findViewById(R.id.fabAgregarCliente)
        recyclerView = findViewById(R.id.recyclerViewClientes)
        tvEmptyClientes = findViewById(R.id.tvEmptyClientes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = ClienteAdapter(clientesList, {
            // Click en Editar
            val intent = Intent(this@Clientes, AddCliente::class.java)
            intent.putExtra("cliente_id", it.id)
            startActivity(intent)
        }, {
            // Click en Eliminar
            FirebaseManager.getClientesRef()?.child(it.id!!)?.removeValue()
            Toast.makeText(this, "Cliente eliminado", Toast.LENGTH_SHORT).show()
        })
        recyclerView.adapter = adapter

        fabAgregarCliente.setOnClickListener {
            val intent = Intent(this, AddCliente::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        setupFirebaseListener()
    }

    private fun setupFirebaseListener() {
        clientesRef = FirebaseManager.getClientesRef()
        if (clientesRef == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            checkEmptyState()
            return
        }

        eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                clientesList.clear()
                for (clienteSnapshot in snapshot.children) {
                    val cliente = clienteSnapshot.getValue(Cliente::class.java)
                    cliente?.id = clienteSnapshot.key
                    if (cliente != null) {
                        clientesList.add(cliente)
                    }
                }
                adapter.notifyDataSetChanged()
                checkEmptyState()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Clientes, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        clientesRef?.addValueEventListener(eventListener)
    }

    private fun checkEmptyState() {
        if (clientesList.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmptyClientes.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmptyClientes.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clientesRef?.removeEventListener(eventListener)
    }
}

class ClienteAdapter(
    private val clientes: List<Cliente>,
    private val onEditClick: (Cliente) -> Unit,
    private val onDeleteClick: (Cliente) -> Unit
) : RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = clientes[position]
        holder.bind(cliente, onEditClick, onDeleteClick)
    }

    override fun getItemCount() = clientes.size

    class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.cliente_nombre)
        private val correo: TextView = itemView.findViewById(R.id.cliente_correo)
        private val telefono: TextView = itemView.findViewById(R.id.cliente_telefono)
        private val btnEditar: Button = itemView.findViewById(R.id.btnEditarCliente)
        private val btnEliminar: Button = itemView.findViewById(R.id.btnEliminarCliente)

        fun bind(cliente: Cliente, onEditClick: (Cliente) -> Unit, onDeleteClick: (Cliente) -> Unit) {
            nombre.text = cliente.nombre
            correo.text = cliente.correo
            telefono.text = cliente.telefono
            btnEditar.setOnClickListener { onEditClick(cliente) }
            btnEliminar.setOnClickListener { onDeleteClick(cliente) }
        }
    }
}