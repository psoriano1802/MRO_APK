package com.example.lacteos_flores.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lacteos_flores.R
import com.example.lacteos_flores.models.DocumentoHistorial

class HistorialDocumentosAdapter(
    private var list: List<DocumentoHistorial>,
    private val onPrintClick: (DocumentoHistorial) -> Unit
) : RecyclerView.Adapter<HistorialDocumentosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvId: TextView = view.findViewById(R.id.tvId)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvDocumento: TextView = view.findViewById(R.id.tvDocumento)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val tvFolioKepler: TextView = view.findViewById(R.id.tvFolioKepler)
        val tvCliente: TextView = view.findViewById(R.id.tvCliente)
        val tvMonto: TextView = view.findViewById(R.id.tvMonto)
        val btnImprimir: ImageButton = view.findViewById(R.id.btnImprimir)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_documento, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvStatus.text = item.staSinc
        holder.tvStatus.setTextColor(
            if (item.staSinc == "S") holder.itemView.context.getColor(android.R.color.holo_green_dark)
            else holder.itemView.context.getColor(android.R.color.holo_red_dark)
        )
        holder.tvId.text = "ID: ${item.id}"
        holder.tvFecha.text = item.fecha
        holder.tvDocumento.text = "Doc: ${item.gen}${item.nat}${item.grp}${item.tip}"
        holder.tvDescripcion.text = item.descripcion ?: "Sin descripción"
        holder.tvFolioKepler.text = "Folio Kepler: ${item.folioKepler ?: "-"}"
        holder.tvCliente.text = "Cliente: ${item.cliente}"
        holder.tvMonto.text = "Monto: $${item.monto}"

        holder.btnImprimir.setOnClickListener { onPrintClick(item) }
        holder.itemView.setOnClickListener { onPrintClick(item) }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<DocumentoHistorial>) {
        list = newList
        notifyDataSetChanged()
    }
}
