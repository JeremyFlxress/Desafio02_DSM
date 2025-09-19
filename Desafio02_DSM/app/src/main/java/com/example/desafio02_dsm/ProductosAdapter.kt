package com.example.desafio02_dsm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.desafio02_dsm.model.Producto


class ProductoAdapter(
    private val productos: List<Producto>,
    private val onEditClick: (Producto) -> Unit,
    private val onDeleteClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        holder.bind(producto, onEditClick, onDeleteClick)
    }

    override fun getItemCount() = productos.size

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.producto_nombre)
        private val precio: TextView = itemView.findViewById(R.id.producto_precio)
        private val stock: TextView = itemView.findViewById(R.id.producto_stock)
        private val btnEditar: ImageButton = itemView.findViewById(R.id.btnEditarProducto)
        private val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminarProducto)

        fun bind(producto: Producto, onEditClick: (Producto) -> Unit, onDeleteClick: (Producto) -> Unit) {
            nombre.text = producto.nombre
            precio.text = "Precio: ${producto.precio}"
            stock.text = "Stock: ${producto.stock}"
            btnEditar.setOnClickListener { onEditClick(producto) }
            btnEliminar.setOnClickListener { onDeleteClick(producto) }
        }
    }
}