package com.example.lacteos_flores.utils

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lacteos_flores.adapters.ResultadoBuscarRMAdapter
import com.example.lacteos_flores.adapters.ResultadoBuscarTecAdapter
import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.data.ExistenciaEntity
import com.example.lacteos_flores.data.ProductosEntity
import com.example.lacteos_flores.databinding.DialogBuscarRefaccionBinding
import com.example.lacteos_flores.interfaz.ApiService
import com.example.lacteos_flores.interfaz.RetrofitClient
import com.example.lacteos_flores.models.Login
import com.example.lacteos_flores.models.ProductosRequest
import com.example.lacteos_flores.models.modelsUI.ProductoUI
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class BusquedaRMBottomSheet(
    private val tipobusqueda: String,
    private val esDevolucion: Boolean = false, // Nuevo parámetro para omitir validación de stock
    private val onItemSelected: (ProductoUI) -> Unit // Callback al seleccionar un resultado
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
    // 1. Función para calcular existencias totales y preparar selección FIFO
    private fun seleccionarConLoteFIFO(producto: ProductoUI) {
        lifecycleScope.launch {
            try {
                // Buscamos todos los lotes con existencia > 0 ordenados por fecha
                val lotesDisponibles = db.existenciasDao().obtenerLotesDisponibles(producto.cve ?: "")
                
                if (lotesDisponibles.isNotEmpty()) {
                    // Calculamos la existencia total sumando todos los lotes
                    val stockTotal = lotesDisponibles.sumOf { it.existencias.toDoubleOrNull() ?: 0.0 }
                    
                    // Guardamos la información en ultimaSeleccion
                    // Usamos el stock total como límite máximo de venta
                    ultimaSeleccion = producto.copy(
                        cant = stockTotal, 
                        lote = "MULTIPLE", // Marcador para que VentasActivity sepa que debe prorratear lotes
                        descripcion = producto.descripcion
                    )
                    
                    binding.btnAgregar.isEnabled = true
                    binding.etCantidad.requestFocus()
                    
                    // Pre-llenamos el precio si está habilitado
                    if(tipobusqueda == "1"){
                        binding.etPrecio.setText(producto.costuni?.toString() ?: "0.0")
                    }
                    
                    val mensaje = "Stock Total: $stockTotal (${lotesDisponibles.size} lotes disponibles)"
                    Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                } else {
                    ultimaSeleccion = producto.copy(cant = 0.0)
                    if (!esDevolucion) {
                        Toast.makeText(requireContext(), "Producto sin existencias en inventario local", Toast.LENGTH_LONG).show()
                    } else {
                        binding.btnAgregar.isEnabled = true
                        binding.etCantidad.requestFocus()
                        if(tipobusqueda == "1"){
                            binding.etPrecio.setText(producto.costuni?.toString() ?: "0.0")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BusquedaRM", "Error calculando stock FIFO", e)
                Toast.makeText(requireContext(), "Error al obtener existencias", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.apiService
        prefs = Prefs(requireContext())
        //obtenemos el usuario y la contraseña de las preferencias
        user = prefs.obtenerUsuario().first.toString()
        pass = prefs.obtenerUsuario().second.toString()
        //inicializamos la base de datos
        db = AppDatabase.getDatabase(requireContext())
        if(tipobusqueda =="1"){
            binding.etPrecio.isEnabled = true
        }else{
            binding.etCantidad.isEnabled = false//para captura de la cantidad(refacciones) o horas (mano de obra)
        }
        binding.btnBuscar.setOnClickListener {
            val texto = binding.etBusqueda.text.toString()
            println("texto:"+texto)
            if (texto.length < 3) {
                binding.etBusqueda.error = "Busqueda"
                return@setOnClickListener
            }
            buscar(texto)
            //validarCampos()
        }

        binding.btnAgregar.setOnClickListener {
            if (!validarCampos()) {
                return@setOnClickListener
            }

            // Validar que haya un producto seleccionado
            val seleccion = ultimaSeleccion
            if (seleccion == null) {
                Toast.makeText(requireContext(), "Seleccione un producto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cantidadIngresada = binding.etCantidad.text.toString().toDoubleOrNull() ?: 0.0
            val stockDisponible = seleccion.cant ?: 0.0 // En el BottomSheet, cant se usa para el stock disponible
            
            if (cantidadIngresada <= 0) {
                binding.etCantidad.error = "Ingrese una cantidad válida"
                return@setOnClickListener
            }

            if (!esDevolucion && cantidadIngresada > stockDisponible) {
                Toast.makeText(requireContext(), "Cantidad ingresada ($cantidadIngresada) mayor a la disponible ($stockDisponible)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // El precio se toma del EditText si es editable, o de la selección
            val precioFinal = if (tipobusqueda == "1") {
                binding.etPrecio.text.toString().toDoubleOrNull() ?: seleccion.costuni ?: 0.0
            } else {
                seleccion.costuni ?: 0.0
            }

            val importeFinal = cantidadIngresada * precioFinal

            // Construimos el ProductoUI final para enviar al callback
            val productoParaVenta = seleccion.copy(
                cant = cantidadIngresada,
                costuni = precioFinal,
                importe = importeFinal
            )
            
            println("Agregando producto a venta: $productoParaVenta")
            onItemSelected(productoParaVenta)
            dismiss()
        }

        // Dentro de onViewCreated
        binding.btnVolver.setOnClickListener {
            restaurarModoBusqueda()
        }
    }



    //validamos que los campos de cantidad y precio no esten vacios, el campo precio solo se habilitara si el valor del precios es 0
    private fun validarCampos(): Boolean {
        val cantidad = binding.etCantidad.text.toString()
        val precio = binding.etPrecio.text.toString()
        if (cantidad.isEmpty()) {
            binding.etCantidad.error = "Cantidad"
            return false
        }
        //validamos que la cantidad ingresada no sea mayor a la disponible validando es decir la (cantidad disponible - cantidad en el recyclerview) - cantidad ingresada por lote


        return true
    }
    //funcion para hacer la busqueda de los productos en base a la opcion seleccionada
    private fun buscar(texto: String) {
        lifecycleScope.launch {
            try {
                // 1. Obtenemos la lista de precios asignada al usuario
                val usuarioInfo = db.usuarioDao().obtenerUsuario(user)
                val listaId = usuarioInfo?.lista ?: ""
                
                // 2. Buscamos los productos con el precio de la lista correspondiente
                // Usamos la función en ListaPrecioDao que ya hace el JOIN con el precio correcto
                val prod = db.listaPreciosDao().obtenerProductosConExistencia("%${texto}%", listaId)
                
                if(prod.isEmpty()){
                    Toast.makeText(requireContext(), "No se encontraron productos con existencias o en la lista de precios", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val listaProductosUI = prod.map {
                    ProductoUI(
                        cve = it.clave,//clave del producto
                        uni = it.unidad,//unidad del producto
                        costuni = it.precio1.toDoubleOrNull(),//precio de la tabla listaprecios (mapeado a precio1 en el query)
                        descripcion = it.descripcion
                    )
                }
                
                if(binding.recyclerResultadosRM.adapter == null){
                    binding.recyclerResultadosRM.layoutManager = LinearLayoutManager(requireContext())
                    val adapter = ResultadoBuscarRMAdapter(listaProductosUI) { seleccionado ->
                            seleccionarConLoteFIFO(seleccionado)
                        }
                    binding.recyclerResultadosRM.adapter = adapter
                }else{
                    // Si ya existe el adapter, actualizar los datos
                    (binding.recyclerResultadosRM.adapter as? ResultadoBuscarRMAdapter)?.let { adapter ->
                        adapter.actualizarLista(listaProductosUI)
                    }
                }

            } catch (e: Exception) {
                Log.e("BusquedaRM", "Error en la búsqueda: ${e.message}")
                Toast.makeText(requireContext(), "Error en la búsqueda", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para REGRESAR a la búsqueda de productos
    private fun restaurarModoBusqueda() {
        ultimaSeleccion = null // Limpiamos selección previa

        // UI: Restauramos visibilidad
        binding.btnVolver.visibility = View.GONE
        binding.etBusqueda.visibility = View.VISIBLE
        binding.btnBuscar.visibility = View.VISIBLE
        binding.tvTitulo.text = "Buscar Producto"

        // IMPORTANTE: Limpiar el adapter para que al buscar de nuevo se recree el flujo
        binding.recyclerResultadosRM.adapter = null
        // Volvemos a ejecutar la búsqueda anterior si hay texto, o limpiamos el recycler
        val texto = binding.etBusqueda.text.toString()
        if (texto.isNotEmpty()) {
            buscar(texto)
        } else {
            binding.recyclerResultadosRM.adapter = null
        }
    }
}
