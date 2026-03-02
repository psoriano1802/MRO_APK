package com.example.agroag_mro.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agroag_mro.R
import com.example.agroag_mro.models.Activos

class ResultadoAdapter(
    private val lista: List<Activos>,
    private val onClick: (Activos) -> Unit
) : RecyclerView.Adapter<ResultadoAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txtNombre)
        val descripcion: TextView = view.findViewById(R.id.txtDescripcion)

        fun bind(resultado: Activos) {
            nombre.text = resultado.cve
            descripcion.text = resultado.name
            view.setOnClickListener { onClick(resultado) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resultado, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(lista[position])
    }
}
