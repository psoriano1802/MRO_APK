package com.example.lacteos_flores.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lacteos_flores.R
import com.example.lacteos_flores.models.modelsUI.GastosUI

class GastosAdapter(
    private val listaGastos: MutableList<GastosUI>
) : RecyclerView.Adapter<GastosAdapter.GastoViewHolder>() {

    class GastoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTipo: TextView = view.findViewById(R.id.tv_tipo_gasto)
        val tvMonto: TextView = view.findViewById(R.id.tv_monto)
        val tvComentario: TextView = view.findViewById(R.id.tv_comentario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GastoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gasto, parent, false)
        return GastoViewHolder(view)
    }

    override fun onBindViewHolder(holder: GastoViewHolder, position: Int) {
        val gasto = listaGastos[position]
        holder.tvTipo.text = gasto.tipoGasto
        holder.tvMonto.text = "$${String.format("%.2f", gasto.monto ?: 0.0)}"
        holder.tvComentario.text = gasto.comentario
    }

    override fun getItemCount(): Int = listaGastos.size

    fun agregarGasto(gasto: GastosUI) {
        listaGastos.add(gasto)
        notifyItemInserted(listaGastos.size - 1)
    }

    fun eliminarGasto(posicion: Int) {
        if (posicion in listaGastos.indices) {
            listaGastos.removeAt(posicion)
            notifyItemRemoved(posicion)
        }
    }

    fun obtenerLista(): List<GastosUI> = listaGastos
}
