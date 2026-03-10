package com.example.agroag_mro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import androidx.recyclerview.widget.RecyclerView
import com.example.agroag_mro.databinding.ItemDocumentoBinding
import com.example.agroag_mro.databinding.ItemDocumentoHeaderBinding
import com.example.agroag_mro.models.modelsUI.DocumentoUI

class DocumentosAdapter(
    private val lista: MutableList<DocumentoUI>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
    }

    // Scrolls registrados
    private val scrollViews = mutableListOf<HorizontalScrollView>()

    // posición actual
    private var scrollXPosition = 0

    // control para evitar loops
    private var isSyncing = false

    class HeaderViewHolder(val binding: ItemDocumentoHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ItemViewHolder(val binding: ItemDocumentoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(
                ItemDocumentoHeaderBinding.inflate(inflater, parent, false)
            )
        } else {
            ItemViewHolder(
                ItemDocumentoBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val scrollView = when (holder) {
            is HeaderViewHolder -> holder.binding.horizontalScroll
            is ItemViewHolder -> holder.binding.horizontalScroll
            else -> null
        }

        scrollView?.let { hsv ->

            if (!scrollViews.contains(hsv)) {
                scrollViews.add(hsv)
            }

            hsv.scrollTo(scrollXPosition, 0)

            hsv.setOnScrollChangeListener { v, scrollX, _, _, _ ->

                if (isSyncing) return@setOnScrollChangeListener

                isSyncing = true
                scrollXPosition = scrollX

                for (view in scrollViews) {
                    if (view != v) {
                        view.scrollTo(scrollX, 0)
                    }
                }

                isSyncing = false
            }
        }

        if (holder is ItemViewHolder) {

            val item = lista[position - 1]

            with(holder.binding) {

                tvDocumento.text = item.documento
                tvTipoOrden.text = item.tipoOrden
                tvEstatus.text = item.estatus
                tvCveActivo.text = item.activo
                tvDescripcion.text = item.descripcion
                tvCvePaquete.text = item.paquete
                tvDescPaquete.text = item.descripcionPaquete
                tvCausaMantto.text = item.causa

                etComentario.setText(item.comentario)

                chValidar.setOnCheckedChangeListener(null)
                chValidar.isChecked = item.validar
                chValidar.setOnCheckedChangeListener { _, isChecked ->
                    item.validar = isChecked
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return lista.size + 1
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        val scrollView = when (holder) {
            is HeaderViewHolder -> holder.binding.horizontalScroll
            is ItemViewHolder -> holder.binding.horizontalScroll
            else -> null
        }

        scrollView?.let {
            scrollViews.remove(it)
        }
    }
}