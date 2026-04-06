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

class ManoObraAdapter (
    private val mObra: MutableList<ProductoUI>
):RecyclerView.Adapter<ManoObraAdapter.ManoObraViewHolder>() {

    // === View Holder ===
    class ManoObraViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val articulo: TextView = itemView.findViewById(R.id.tv_articulo)
        val cantidad: TextView = itemView.findViewById(R.id.tv_cantidad)//tiempo en horas
        val unidad: TextView = itemView.findViewById(R.id.tv_unidad)
        val costoUnitario: TextView = itemView.findViewById(R.id.tv_costo_unitario)//tiempo en minutos
        val importe: TextView = itemView.findViewById(R.id.tv_importe)//tiempo en horas
        val descripcion: TextView = itemView.findViewById(R.id.tv_descripcion)//clave tecnico
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManoObraViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_refacciones, parent, false)
        return ManoObraViewHolder(view)
    }

    override fun onBindViewHolder(holder: ManoObraViewHolder, position: Int) {
        val refaccion = mObra[position]
        holder.articulo.text = refaccion.cve
        holder.cantidad.text = refaccion.horas.toString()
        holder.unidad.text = refaccion.uni
        holder.costoUnitario.text = "$${refaccion.minutos}"
        //calculamos el importe

        System.out.println("refaccion123: hora"+refaccion.costuni+"minutos:"+refaccion.cant+"importe:"+refaccion.importe)
        val importe = refaccion.horas?.let { refaccion.costuni?.toDouble()?.times(it.toDouble()) }
        holder.importe.text = refaccion.importe.toString()
        holder.descripcion.text = refaccion.descripcion
        holder.itemView.setOnClickListener {
            mostrarDialogoEdicion(holder.itemView, position)
        }
    }

    override fun getItemCount(): Int = mObra.size

    // ✅ Agregar un solo ítem
    fun agregarItem(refaccion: ProductoUI) {
        mObra.add(refaccion)
        notifyItemInserted(mObra.size - 1)
    }

    // ✅ Agregar lista completa
    fun agregarLista(nuevaLista: List<ProductoUI>) {
        val startPos = mObra.size
        mObra.addAll(nuevaLista)
        notifyItemRangeInserted(startPos, nuevaLista.size)
    }
    // ✅ Eliminar con swipe
    fun eliminarItem(pos: Int) {
        if (pos in mObra.indices) {
            mObra.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }

    //obtenemos el listado de refacciones
    fun obtenerLista(): List<ProductoUI> = mObra

    // 🔹 Mostrar diálogo de edición
    private fun mostrarDialogoEdicion(view: View, position: Int) {
        val mo = mObra[position]

        val dialogView = LayoutInflater.from(view.context)
            .inflate(R.layout.dialog_editar_item, null)

        val etCantidad:  EditText = dialogView.findViewById(R.id.et_cantidad_edit)
        val etPrecio:  EditText = dialogView.findViewById(R.id.et_precio_edit)

        etCantidad.setText(mo.cant?.toString() ?: "")
        etPrecio.setText(mo.costuni?.toString() ?: "")

        AlertDialog.Builder(view.context)
            .setTitle("Editar Item")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevaCantidad = etCantidad.text.toString().toDoubleOrNull()
                val nuevoPrecio = etPrecio.text.toString().toDoubleOrNull()

                if (nuevaCantidad != null) mo.cant = nuevaCantidad
                if (nuevoPrecio != null) mo.costuni = nuevoPrecio

                notifyItemChanged(position)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}