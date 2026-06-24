package com.example.lacteos_flores.utils

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lacteos_flores.adapters.ResultadoBuscarRMAdapter
import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.databinding.DialogBuscarRefaccionBinding
import com.example.lacteos_flores.interfaz.ApiService
import com.example.lacteos_flores.interfaz.RetrofitClient
import com.example.lacteos_flores.models.modelsUI.ProductoUI
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import java.util.Locale

class BusquedaRMBottomSheet(
    private val tipobusqueda: String,
    private val esDevolucion: Boolean = false, 
    private val productosYaAgregados: List<ProductoUI> = emptyList(), 
    private val onItemSelected: (ProductoUI) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogBuscarRefaccionBinding
    private lateinit var apiService: ApiService
    private lateinit var user: String
    private lateinit var pass: String
    private lateinit var prefs: Prefs
    private lateinit var db: AppDatabase
    private var ultimaSeleccion: ProductoUI? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBuscarRefaccionBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun seleccionarConLoteFIFO(producto: ProductoUI) {
        lifecycleScope.launch {
            try {
                // UNIFICACIÓN: Siempre cerramos el buscador al seleccionar el producto base
                // y dejamos que la Activity maneje la captura de detalles (TMC/Lote)
                onItemSelected(producto)
                dismiss()
            } catch (e: Exception) {
                Log.e("BusquedaRM", "Error en selección", e)
            }
        }
    }

    private suspend fun actualizarStockTotal(producto: ProductoUI) {
        val lotesDisponibles = db.existenciasDao().obtenerLotesDisponibles(producto.cve ?: "")
        
        var stockTotal = lotesDisponibles.filter {
            (producto.talla == "-" || it.talla == producto.talla) &&
            (producto.modelo == "-" || it.modelo == producto.modelo) &&
            (producto.color == "-" || it.color == producto.color)
        }.sumOf { it.existencias.toDoubleOrNull() ?: 0.0 }
        
        val yaAgregado = productosYaAgregados.filter { 
            it.cve == producto.cve && it.talla == producto.talla && it.modelo == producto.modelo && it.color == producto.color
        }.sumOf { it.cant ?: 0.0 }
        
        stockTotal -= yaAgregado

        ultimaSeleccion = producto.copy(cant = stockTotal)
        binding.btnAgregar.isEnabled = true
        
        if(tipobusqueda == "1"){
            binding.etPrecio.setText(producto.costuni?.toString() ?: "0.0")
        }
        
        val mensaje = if (yaAgregado > 0) "Stock Real: $stockTotal" else "Stock Total: $stockTotal"
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun configurarSeleccionCatalogo(editText: EditText, claveProd: String, tipo: String) {
        editText.setText("-")
        editText.isFocusable = false
        editText.isClickable = true
        editText.setOnClickListener {
            lifecycleScope.launch {
                val opciones = when (tipo) {
                    "TALLA" -> db.existenciasDao().obtenerTallasPorProducto(claveProd)
                    "MODELO" -> db.existenciasDao().obtenerModelosPorProducto(claveProd)
                    "COLOR" -> db.existenciasDao().obtenerColoresPorProducto(claveProd)
                    else -> emptyList()
                }

                if (opciones.isNotEmpty()) {
                    val listConGuion = listOf("-") + opciones
                    AlertDialog.Builder(requireContext())
                        .setTitle("Seleccione $tipo")
                        .setItems(listConGuion.toTypedArray()) { _, which ->
                            editText.setText(listConGuion[which])
                            lifecycleScope.launch {
                                val prodAct = ultimaSeleccion?.copy(
                                    talla = binding.etTallaSel.text.toString(),
                                    modelo = binding.etModeloSel.text.toString(),
                                    color = binding.etColorSel.text.toString()
                                )
                                prodAct?.let { actualizarStockTotal(it) }
                            }
                        }
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Sin catálogo para $tipo", Toast.LENGTH_SHORT).show()
                    // Si no hay catálogo, permitir entrada manual si se desea
                    editText.isFocusableInTouchMode = true
                    editText.requestFocus()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.apiService
        prefs = Prefs(requireContext())
        user = prefs.obtenerUsuario().first.toString()
        pass = prefs.obtenerUsuario().second.toString()
        db = AppDatabase.getDatabase(requireContext())

        if(tipobusqueda =="1" || esDevolucion){
            binding.etPrecio.isEnabled = false // Siempre bloqueado
            binding.etCantidad.isEnabled = false
            //binding.etPrecio.visibility = if (esDevolucion) View.GONE else View.VISIBLE
        } else {
            binding.etCantidad.isEnabled = false
        }

        binding.btnBuscar.setOnClickListener {
            val texto = binding.etBusqueda.text.toString()
            if (texto.length < 0) {
                binding.etBusqueda.error = "Mínimo 3 caracteres"
                return@setOnClickListener
            }
            buscar(texto)
        }

        binding.btnAgregar.setOnClickListener {
            if (!validarCampos()) return@setOnClickListener

            val seleccion = ultimaSeleccion ?: return@setOnClickListener
            val cantidadIngresada = binding.etCantidad.text.toString().toDoubleOrNull() ?: 0.0
            
            if (cantidadIngresada <= 0) {
                binding.etCantidad.error = "Cantidad inválida"
                return@setOnClickListener
            }

            if (!esDevolucion && cantidadIngresada > (seleccion.cant ?: 0.0)) {
                Toast.makeText(requireContext(), "Stock insuficiente (${seleccion.cant})", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val precioFinal = if (tipobusqueda == "1" && !esDevolucion) {
                binding.etPrecio.text.toString().toDoubleOrNull() ?: seleccion.costuni ?: 0.0
            } else {
                seleccion.costuni ?: 0.0
            }

            val productoFinal = seleccion.copy(
                cant = cantidadIngresada,
                costuni = precioFinal,
                importe = cantidadIngresada * precioFinal,
                talla = if (binding.layoutTMC.visibility == View.VISIBLE) binding.etTallaSel.text.toString() else "-",
                modelo = if (binding.layoutTMC.visibility == View.VISIBLE) binding.etModeloSel.text.toString() else "-",
                color = if (binding.layoutTMC.visibility == View.VISIBLE) binding.etColorSel.text.toString() else "-"
            )
            
            onItemSelected(productoFinal)
            dismiss()
        }

        binding.btnVolver.setOnClickListener {
            restaurarModoBusqueda()
        }
    }

    private fun validarCampos(): Boolean {
        if (binding.etCantidad.text.toString().isEmpty()) {
            binding.etCantidad.error = "Requerido"
            return false
        }
        return true
    }

    private fun buscar(texto: String) {
        lifecycleScope.launch {
            try {
                val usuarioInfo = db.usuarioDao().obtenerUsuario(user)
                val listaId = usuarioInfo?.lista ?: ""
                val prod = db.listaPreciosDao().obtenerProductosConExistencia("%${texto}%", listaId)
                
                if(prod.isEmpty()){
                    Toast.makeText(requireContext(), "Sin resultados", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val listaProductosUI = prod.map {
                    val precio = it.precio1.toDoubleOrNull() ?: 0.0
                    ProductoUI(
                        cve = it.clave,
                        uni = it.unidad,
                        costuni = precio,
                        costbase = precio, // Guardamos el precio base original del catálogo
                        descripcion = it.descripcion,
                        tmc = it.tmc
                    )
                }
                
                if(binding.recyclerResultadosRM.adapter == null){
                    binding.recyclerResultadosRM.layoutManager = LinearLayoutManager(requireContext())
                    binding.recyclerResultadosRM.adapter = ResultadoBuscarRMAdapter(listaProductosUI) { seleccion ->
                        seleccionarConLoteFIFO(seleccion)
                    }
                } else {
                    (binding.recyclerResultadosRM.adapter as? ResultadoBuscarRMAdapter)?.actualizarLista(listaProductosUI)
                }

            } catch (e: Exception) {
                Log.e("BusquedaRM", "Error búsqueda", e)
            }
        }
    }

    private fun restaurarModoBusqueda() {
        ultimaSeleccion = null
        binding.btnVolver.visibility = View.GONE
        binding.etBusqueda.visibility = View.VISIBLE
        binding.btnBuscar.visibility = View.VISIBLE
        binding.tvTitulo.text = "Buscar Producto"
        binding.layoutTMC.visibility = View.GONE
        binding.recyclerResultadosRM.adapter = null
        val texto = binding.etBusqueda.text.toString()
        if (texto.isNotEmpty()) buscar(texto)
    }
}
