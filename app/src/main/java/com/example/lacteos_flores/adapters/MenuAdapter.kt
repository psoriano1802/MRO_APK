package com.example.lacteos_flores.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.lacteos_flores.R

data class MenuOptions(val title: String, val iconRes: Int,val clave: String, val accion: Class<out AppCompatActivity>)


class MenuAdapter(private val items: List<MenuOptions>,private val onItemClick: (Class<out AppCompatActivity>) -> Unit) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    inner class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById<ImageView>(R.id.menu_icon)
        val title: TextView = view.findViewById<TextView>(R.id.menu_title)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = items[position]
        holder.icon.setImageResource(item.iconRes)
        holder.title.text = item.title
        holder.itemView.setOnClickListener {
            //solo para el item de ventas se validara primero si hay existencias en el almacen, de no haber existencias bloqueamos la accion y enviamos un mensaje para que sincronice existencias

            onItemClick(item.accion)
        }
    }

    override fun getItemCount(): Int = items.size
}
