package com.example.lacteos_flores.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lacteos_flores.R
import com.example.lacteos_flores.data.CarteraEntity

class FacturasAdapter(
    private var facturas: List<CarteraEntity>,
    private val onSelectionChanged: (List<CarteraEntity>) -> Unit
) : RecyclerView.Adapter<FacturasAdapter.FacturaViewHolder>() {

    private val seleccionadas = mutableSetOf<CarteraEntity>()

    class FacturaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cbSeleccion: CheckBox = view.findViewById(R.id.cb_seleccion_factura)
        val tvFolio: TextView = view.findViewById(R.id.tv_folio_factura)
        val tvFecha: TextView = view.findViewById(R.id.tv_fecha_factura)
        val tvSaldo: TextView = view.findViewById(R.id.tv_saldo_factura)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_factura_cobro, parent, false)
        return FacturaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacturaViewHolder, position: Int) {
        val factura = facturas[position]
        holder.tvFolio.text = factura.docto
        holder.tvFecha.text = factura.fecha
        holder.tvSaldo.text = "$${factura.saldo}"

        holder.cbSeleccion.setOnCheckedChangeListener(null)
        holder.cbSeleccion.isChecked = seleccionadas.contains(factura)

        holder.cbSeleccion.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                seleccionadas.add(factura)
            } else {
                seleccionadas.remove(factura)
            }
            onSelectionChanged(seleccionadas.toList())
        }
    }

    override fun getItemCount() = facturas.size

    fun actualizarLista(nuevaLista: List<CarteraEntity>) {
        facturas = nuevaLista
        seleccionadas.clear()
        notifyDataSetChanged()
        onSelectionChanged(emptyList())
    }

    fun seleccionarTodo(seleccionar: Boolean) {
        if (seleccionar) {
            seleccionadas.addAll(facturas)
        } else {
            seleccionadas.clear()
        }
        notifyDataSetChanged()
        onSelectionChanged(seleccionadas.toList())
    }
    
    fun getSeleccionadas() = seleccionadas.toList()
    
    fun getTodasFacturas() = facturas

    fun setSeleccionadas(lista: List<CarteraEntity>) {
        seleccionadas.clear()
        seleccionadas.addAll(lista)
        notifyDataSetChanged()
        onSelectionChanged(seleccionadas.toList())
    }
}
