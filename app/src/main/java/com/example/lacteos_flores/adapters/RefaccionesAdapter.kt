package com.example.lacteos_flores.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lacteos_flores.R
import com.example.lacteos_flores.models.modelsUI.ProductoUI

class RefaccionesAdapter (
    private val refacciones: MutableList<ProductoUI>,
    private var headers: List<String>, //headers dinamicos
    private val onItemChanged: () -> Unit // Callback para notificar cambios (edición/eliminación)
):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //--tipos de vista---
    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    //---viewholder de headers---
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val col1: TextView = itemView.findViewById(R.id.tv_header_col1)
        val col2: TextView = itemView.findViewById(R.id.tv_header_col2)
        val col3: TextView = itemView.findViewById(R.id.tv_header_col3)
        val col4: TextView = itemView.findViewById(R.id.tv_header_col4)
        val col5: TextView = itemView.findViewById(R.id.tv_header_col5)
        val col6: TextView = itemView.findViewById(R.id.tv_header_col6)
    }

    // === View Holder ===
    class RefaccionesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val articulo: TextView = itemView.findViewById(R.id.tv_articulo)
        val cantidad: TextView = itemView.findViewById(R.id.tv_cantidad)
        val unidad: TextView = itemView.findViewById(R.id.tv_unidad)
        val costoUnitario: TextView = itemView.findViewById(R.id.tv_costo_unitario)
        val importe: TextView = itemView.findViewById(R.id.tv_importe)
        val descripcion: TextView = itemView.findViewById(R.id.tv_descripcion)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {TYPE_HEADER} else {TYPE_ITEM}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_refacciones_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_refacciones, parent, false)
            RefaccionesViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is HeaderViewHolder){
            holder.col1.text = headers.getOrNull(0) ?: "Clave"
            holder.col2.text = headers.getOrNull(1) ?: "Min"
            holder.col3.text = headers.getOrNull(2) ?: "Hr"
            holder.col4.text = headers.getOrNull(3) ?: ""
            holder.col5.text = headers.getOrNull(4) ?: ""
            holder.col6.text = headers.getOrNull(5) ?: ""
        }else if(holder is RefaccionesViewHolder){
            val refaccion = refacciones[position-1]
            holder.articulo.text = refaccion.cve
            holder.cantidad.text = refaccion.cant.toString()
            holder.unidad.text = refaccion.uni
            holder.costoUnitario.text = "$${refaccion.costuni}"
            
            val totalImporte = (refaccion.cant ?: 0.0) * (refaccion.costuni ?: 0.0)
            holder.importe.text = "$${String.format("%.2f", totalImporte)}"
            holder.descripcion.text = refaccion.descripcion

            holder.itemView.setOnClickListener {
                mostrarDialogoEdicion(holder.itemView, position - 1 )
            }
        }
    }

    override fun getItemCount(): Int = refacciones.size + 1

    //Cambiar headers dinamicamente
    fun actualizarHeader(nvoHeaders: List<String>){
        headers = nvoHeaders
        notifyItemChanged(0)
    }

    fun agregarItem(refaccion: ProductoUI) {
        refacciones.add(refaccion)
        notifyItemInserted(refacciones.size)
        onItemChanged()
    }

    fun eliminarItem(adapterPos: Int) {
        val listPos = adapterPos - 1
        if (listPos in refacciones.indices) {
            refacciones.removeAt(listPos)
            notifyItemRemoved(adapterPos)
            onItemChanged()
        }
    }

    fun obtenerLista(): List<ProductoUI> = refacciones

    private fun mostrarDialogoEdicion(view: View, listPosition: Int) {
        val refaccion = refacciones[listPosition]

        val dialogView = LayoutInflater.from(view.context)
            .inflate(R.layout.dialog_editar_item, null)

        val etCantidad: EditText = dialogView.findViewById(R.id.et_cantidad_edit)
        val etPrecio: EditText = dialogView.findViewById(R.id.et_precio_edit)

        etCantidad.setText(refaccion.cant?.toString() ?: "")
        etPrecio.setText(refaccion.costuni?.toString() ?: "")

        AlertDialog.Builder(view.context)
            .setTitle("Editar Item")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevaCantidad = etCantidad.text.toString().toDoubleOrNull()
                val nuevoPrecio = etPrecio.text.toString().toDoubleOrNull()

                if (nuevaCantidad != null) refaccion.cant = nuevaCantidad
                if (nuevoPrecio != null) refaccion.costuni = nuevoPrecio
                
                refaccion.importe = (refaccion.cant ?: 0.0) * (refaccion.costuni ?: 0.0)

                notifyItemChanged(listPosition + 1)
                onItemChanged()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
