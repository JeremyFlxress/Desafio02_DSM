package com.example.desafio02_dsm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.desafio02_dsm.model.Cliente
import com.example.desafio02_dsm.model.Producto
import com.example.desafio02_dsm.model.Venta

// ViewHolder for current sale products
class VentaProductosAdapter(private val productos: List<Pair<Producto, Int>>) :
    RecyclerView.Adapter<VentaProductosAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.producto_nombre)
        val cantidad: TextView = view.findViewById(R.id.producto_cantidad)
        val precio: TextView = view.findViewById(R.id.producto_precio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto_venta, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (producto, cantidad) = productos[position]
        holder.nombre.text = producto.nombre
        holder.cantidad.text = "Cantidad: $cantidad"
        holder.precio.text = "Precio: ${producto.precio}"
    }

    override fun getItemCount() = productos.size
}

// ViewHolder for sales history
class VentaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val fecha: TextView = itemView.findViewById(R.id.venta_fecha)
    private val cliente: TextView = itemView.findViewById(R.id.venta_cliente)
    private val total: TextView = itemView.findViewById(R.id.venta_total)

    fun bind(venta: Venta) {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val date = java.util.Date(venta.fecha!!)
        fecha.text = sdf.format(date)
        total.text = "Total: ${String.format("%.2f", venta.total)}"

        val clientRef = FirebaseManager.getClientesRef()?.child(venta.clienteId!!)
        clientRef?.get()?.addOnSuccessListener { dataSnapshot ->
            val clienteData = dataSnapshot.getValue(Cliente::class.java)
            if (clienteData != null) {
                cliente.text = "Cliente: ${clienteData.nombre}"
            }
        }
    }
}