package com.example.desafio02_dsm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.desafio02_dsm.model.Cliente


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
        private val btnEditar: ImageButton = itemView.findViewById(R.id.btnEditarCliente)
        private val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminarCliente)

        fun bind(
            cliente: Cliente,
            onEditClick: (Cliente) -> Unit,
            onDeleteClick: (Cliente) -> Unit
        ) {
            nombre.text = cliente.nombre
            correo.text = cliente.correo
            telefono.text = cliente.telefono
            btnEditar.setOnClickListener { onEditClick(cliente) }
            btnEliminar.setOnClickListener { onDeleteClick(cliente) }
        }
    }
}