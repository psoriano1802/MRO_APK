package com.example.lacteos_flores.utils

import android.os.Bundle
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
    private val onItemSelected: (ProductoUI) -> Unit // Callback al seleccionar un resultado
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogBuscarRefaccionBinding
    private lateinit var apiService: ApiService
    private lateinit var user: String
    private lateinit var pass: String
    private lateinit var prefs: Prefs
    private lateinit var db: AppDatabase
    private var ultimaSeleccion: ProductoUI? = null

    private var viendoLotes = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBuscarRefaccionBinding.inflate(inflater, container, false)
        return binding.root
    }
    // 1. Nueva función para buscar lotes en la DB local
    private fun mostrarSeleccionLotes(producto: ProductoUI) {
        lifecycleScope.launch {
            // buscamos los lotes para validar si existen
            //consultamos la tabla de existencias para obtener los lotes disponibles para el producto y las cantidades para validar que se cuente con existencia disponible
            //val lotesDisponibles = db.existenciasDao().obtenerExistencia(producto.cve.toString())
            val listusr = db.usuarioDao().obtenerUsuario(user)?.lista
            val lotesDisponibles = db.listaPreciosDao().obtenerProductosConExistencia("%${producto.cve}%", listusr.toString())
            println("lotesDisponibles:"+lotesDisponibles)
            // UI: Cambios visuales
            binding.btnVolver.visibility = View.VISIBLE // Mostramos el botón volver
            binding.etBusqueda.visibility = View.GONE
            binding.btnBuscar.visibility = View.GONE
            binding.tvTitulo.text = "Seleccione Lote para ${producto.cve}"
            if (lotesDisponibles?.isNotEmpty() == true) {
                viendoLotes = true
                val listProductoAux = lotesDisponibles.map {
                    ProductoUI(
                        cve = it.clave,//clave del produccto con control auxiliar
                        cant = it.existencia,// cantidad del producto con control auxiliar
                        descripcion = "Aux: ${it.pedimento} - Stock: ${it.existencia}",//descripcion del producto con control auxiliar// lote del producto con control auxiliar
                    )
                }

                // Cambiamos el adapter por uno de lotes (o el mismo con flag)
                val adapterLotes = ResultadoBuscarRMAdapter(listProductoAux) { loteSeleccionado ->
                    // Al seleccionar el lote, actualizamos ultimaSeleccion y habilitamos el botón Agregar
                    ultimaSeleccion = producto.copy(cant = loteSeleccionado.cant, descripcion = loteSeleccionado.descripcion)
                    binding.btnAgregar.isEnabled = true
                    println("loteSeleccionadoCant:"+loteSeleccionado.costuni)
                    //Toast.makeText(requireContext(), "Lote seleccionado: ${loteSeleccionado.cve}", Toast.LENGTH_SHORT).show()
                }
                binding.recyclerResultadosRM.adapter = adapterLotes
            } else {
                // Si no hay lotes, procedemos normal como lo tenías
                ultimaSeleccion = producto
                println("ultimaSeleccionsinlote:"+ultimaSeleccion?.cve)
                Toast.makeText(requireContext(), "Producto sin lotes, ingrese cantidad", Toast.LENGTH_SHORT).show()
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
            // Si el producto requiere lote y no se ha seleccionado (y estamos en modo lotes)
            if (viendoLotes && ultimaSeleccion?.cve.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Debe seleccionar un lote", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var cantidadText = ""
            var cantidad: Double = 0.0
            var precioText: String= ""
            var costoUnitario: Double = 0.0
            var importe=""
            if(tipobusqueda == "1"){
                // Tomamos la cantidad y precio de los EditText
                cantidadText = binding.etCantidad.text.toString()
                cantidad = cantidadText.toDoubleOrNull() ?: 0.0
                precioText = ultimaSeleccion?.costuni.toString() ?: ""
                costoUnitario = ultimaSeleccion?.cant ?: 0.0
                println("lote cantiad:"+ultimaSeleccion?.cant ?: 0.0)
                println("lote:"+ultimaSeleccion?.cve)
                println("cantidad:"+cantidad)
                importe= (costoUnitario * cantidad).toString()
                if(cantidad > costoUnitario){
                    Toast.makeText(requireContext(), "Cantidad ingresada mayor a la disponible", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

            }


            // Validar que haya un producto seleccionado de la lista
            if (ultimaSeleccion == null) {
                Toast.makeText(requireContext(), "Seleccione un producto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Construimos un nuevo ProductoUI con cantidad y precio
            val productoConDatos = ultimaSeleccion!!.copy(
                cant = cantidadText.toDouble(),
                costuni = costoUnitario,
                importe = importe.toDouble()
            )
            println("productoConDatos:"+productoConDatos)
            // Enviamos el producto al callback para que se agregue al RecyclerView principal
            onItemSelected(productoConDatos)
            // Cerramos el BottomSheet
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
    //funcion para hacer la busqueda de los activos en base a la opcion seleccionada
    private fun buscar(texto: String) {
        lifecycleScope.launch {
            try {
                val prod = db.productosDao().obtenerTodosProductos("%${texto}%")
                val proExis = db.existenciasDao().obtenerExistencia("%${texto}%")
                val proExisAux = db.existenciasDao().obtenerExistencia("%${texto}%")
                //parseamos el listado de productos obtenidos desde la base
                if(proExis?.isEmpty() == true && proExisAux?.isEmpty() == true){
                    Toast.makeText(requireContext(), "Producto no cuenta con existencias para realizar la venta, Solicite una Recarga y Actualice Informacion!", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                if (prod.isNotEmpty()) {
                    val listaProductosUI = prod.map {
                        ProductoUI(
                            cve = it.clave,//clave del producto
                            uni = it.unidad,//unidad del producto
                            costuni = it.precio1.toDoubleOrNull(),//precio del producto
                            descripcion = it.descripcion
                        )
                    }
                    if(binding.recyclerResultadosRM.adapter == null){
                        binding.recyclerResultadosRM.layoutManager = LinearLayoutManager(requireContext())
                        val adapter = ResultadoBuscarRMAdapter(listaProductosUI) { seleccionado ->
                                //onItemSelected(seleccionado)
                                // ActualizarultimaSeleccion = seleccionado
                                ///dismiss()
                                mostrarSeleccionLotes(seleccionado)
                            }
                        binding.recyclerResultadosRM.adapter = adapter
                    }else{
                        // Si ya existe el adapter, actualizar los datos
                        (binding.recyclerResultadosRM.adapter as? ResultadoBuscarRMAdapter)?.let { adapter ->
                            adapter.actualizarLista(listaProductosUI)
                        }
                    }


                } else {
                    Toast.makeText(requireContext(), "Producto no encontrado", Toast.LENGTH_SHORT).show()

                }

            } catch (e: Exception) {
                System.out.println("error:"+e.message)
                Toast.makeText(requireContext(), "Error en la búsqueda", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para REGRESAR a la búsqueda de productos
    private fun restaurarModoBusqueda() {
        viendoLotes = false
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
