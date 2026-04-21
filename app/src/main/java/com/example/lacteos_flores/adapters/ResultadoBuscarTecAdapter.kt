package com.example.lacteos_flores.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lacteos_flores.R
import com.example.lacteos_flores.data.ClientsEntity

class ResultadoBuscarTecAdapter(
    private val lista: List<ClientsEntity>,
    private val onClick: (ClientsEntity) -> Unit
) : RecyclerView.Adapter<ResultadoBuscarTecAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txtNombre)
        val descripcion: TextView = view.findViewById(R.id.txtDescripcion)

        fun bind(resultado: ClientsEntity) {
            nombre.text = resultado.clave
            descripcion.text = resultado.nombre
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
