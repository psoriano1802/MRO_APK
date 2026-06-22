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

    private val seleccionadosIds = mutableSetOf<String>()

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
        holder.cbSeleccion.isChecked = seleccionadosIds.contains(factura.docto)

        holder.cbSeleccion.setOnClickListener {
            val isChecked = (it as CheckBox).isChecked
            if (isChecked) {
                seleccionadosIds.add(factura.docto)
            } else {
                seleccionadosIds.remove(factura.docto)
            }
            // Notificamos con la lista actual de objetos que coinciden con los IDs
            onSelectionChanged(facturas.filter { seleccionadosIds.contains(it.docto) })
        }
    }

    override fun getItemCount() = facturas.size

    fun actualizarLista(nuevaLista: List<CarteraEntity>) {
        facturas = nuevaLista
        seleccionadosIds.clear()
        notifyDataSetChanged()
        onSelectionChanged(emptyList())
    }

    fun actualizarListaSoloDatos(nuevaLista: List<CarteraEntity>) {
        facturas = nuevaLista
        notifyDataSetChanged()
    }

    fun seleccionarTodo(seleccionar: Boolean) {
        if (seleccionar) {
            seleccionadosIds.addAll(facturas.map { it.docto })
        } else {
            seleccionadosIds.clear()
        }
        notifyDataSetChanged()
        onSelectionChanged(facturas.filter { seleccionadosIds.contains(it.docto) })
    }
    
    fun getSeleccionadas() = facturas.filter { seleccionadosIds.contains(it.docto) }
    
    fun getTodasFacturas() = facturas

    fun setSeleccionadosPorId(ids: List<String>) {
        seleccionadosIds.clear()
        seleccionadosIds.addAll(ids)
        notifyDataSetChanged()
    }
}
