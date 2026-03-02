package com.example.agroag_mro.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agroag_mro.R
import com.example.agroag_mro.models.OrdenItem

class OrdenesAdapter(
    private val lista: List<OrdenItem>,
    private val onVerClick: (OrdenItem  ) -> Unit,//
    private val onEditarClick: (OrdenItem) -> Unit,
    private val onEliminarClick: (OrdenItem) -> Unit
) : RecyclerView.Adapter<OrdenesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFolio: TextView = itemView.findViewById(R.id.tvFolio)
        val tvActivo: TextView = itemView.findViewById(R.id.tvActivo)
        val tvTipo: TextView = itemView.findViewById(R.id.tvTipo)
        val btnVer: Button = itemView.findViewById(R.id.btnsolicita)
        val btnEditar: Button = itemView.findViewById(R.id.btnCarga)
        val btnEliminar: Button = itemView.findViewById(R.id.btnActivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listado_ord, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.tvFolio.text = "Folio: ${item.folio}"
        holder.tvActivo.text = "Activo: ${item.nomAct}"
        holder.tvTipo.text = "Tipo: ${item.tipo}"

        holder.btnVer.setOnClickListener { onVerClick(item) }
        holder.btnEditar.setOnClickListener { onEditarClick(item) }
        holder.btnEliminar.setOnClickListener { onEliminarClick(item) }
    }

    override fun getItemCount(): Int = lista.size
}
