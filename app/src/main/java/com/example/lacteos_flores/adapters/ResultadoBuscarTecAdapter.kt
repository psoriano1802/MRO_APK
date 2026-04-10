package com.example.lacteos_flores.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lacteos_flores.R
import com.example.lacteos_flores.models.itemsDoc

class ResultadoBuscarTecAdapter(
    private val lista: List<itemsDoc>,
    private val onClick: (itemsDoc) -> Unit
) : RecyclerView.Adapter<ResultadoBuscarTecAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txtNombre)
        val descripcion: TextView = view.findViewById(R.id.txtDescripcion)

        fun bind(resultado: itemsDoc) {
            nombre.text = resultado.cant
            descripcion.text = resultado.uni
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
