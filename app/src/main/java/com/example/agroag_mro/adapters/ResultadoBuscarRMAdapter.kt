package com.example.agroag_mro.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agroag_mro.R
import com.example.agroag_mro.models.modelsUI.ProductoUI


//clase que permite mostrar los resultados de las busquedas en el recyclerview para refaciones o mano de obra

class ResultadoBuscarRMAdapter(
    private var lista: List<ProductoUI>,
    private val onItemClick: (ProductoUI) -> Unit
) : RecyclerView.Adapter<ResultadoBuscarRMAdapter.BusquedaViewHolder>() {

    private var seleccionada: ProductoUI? = null
    private var seleccionadaPos: Int = RecyclerView.NO_POSITION

    class BusquedaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvArticulo: TextView = itemView.findViewById(R.id.txtNombre)
        val tvDescripcion: TextView = itemView.findViewById(R.id.txtDescripcion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusquedaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resultado, parent, false)
        return BusquedaViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusquedaViewHolder, position: Int) {
        val refaccion = lista[position]
        holder.tvArticulo.text = refaccion.cve
        holder.tvDescripcion.text = refaccion.descripcion

        // Colorear si es el seleccionado
        if (position == seleccionadaPos) {
            holder.itemView.setBackgroundColor(Color.parseColor("#D6EAF8")) // azul claro
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE)
        }

        holder.itemView.setOnClickListener {
            val prevPos = seleccionadaPos
            seleccionada = refaccion
            seleccionadaPos = holder.adapterPosition
            // Refrescar solo los ítems necesarios
            if (prevPos != RecyclerView.NO_POSITION) {
                notifyItemChanged(prevPos)
            }
            notifyItemChanged(seleccionadaPos)
            // Callback al activity/fragment
            onItemClick(refaccion)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<ProductoUI>) {
        lista = nuevaLista
        seleccionadaPos = RecyclerView.NO_POSITION

        notifyDataSetChanged()
    }


    fun obtenerSeleccionada(): ProductoUI? = seleccionada
}
