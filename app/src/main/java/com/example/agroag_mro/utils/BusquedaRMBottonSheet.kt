package com.example.agroag_mro.utils

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agroag_mro.adapters.ResultadoAdapter
import com.example.agroag_mro.adapters.ResultadoBuscarRMAdapter
import com.example.agroag_mro.databinding.DialogBuscarRefaccionBinding
import com.example.agroag_mro.databinding.DialogBusquedaBinding
import com.example.agroag_mro.interfaz.ApiService
import com.example.agroag_mro.interfaz.RetrofitClient
import com.example.agroag_mro.models.ActivoRequest
import com.example.agroag_mro.models.ItemProductos
import com.example.agroag_mro.models.Login
import com.example.agroag_mro.models.LoginRequest
import com.example.agroag_mro.models.PaquetesRequest
import com.example.agroag_mro.models.ProductosRequest
import com.example.agroag_mro.models.TiposActivoResponse
import com.example.agroag_mro.models.itemPaqMO
import com.example.agroag_mro.models.modelsUI.ProductoUI
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
    private var ultimaSeleccion: ProductoUI? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBuscarRefaccionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiService = RetrofitClient.apiService
        prefs = Prefs(requireContext())
        //obtenemos el usuario y la contraseña de las preferencias
        user = prefs.obtenerUsuario().first.toString()
        pass = prefs.obtenerUsuario().second.toString()
        //cargarOpciones()

        if(tipobusqueda =="1"){
            binding.etPrecio.isEnabled = true
            binding.etCantidad.isEnabled = true
        }else{
            //binding.etPrecio.isEnabled = false // para captura del precio(refacciones) o minutos (mano de obra)
            binding.etCantidad.isEnabled = false//para captura de la cantidad(refacciones) o horas (mano de obra)

        }
        binding.btnBuscar.setOnClickListener {
            val texto = binding.etBusqueda.text.toString()
            if (texto.length < 3) {
                binding.etBusqueda.error = "Busqueda"
                return@setOnClickListener
            }
            buscar(tipobusqueda, texto)
            //validarCampos()

        }
        binding.btnAgregar.setOnClickListener {
            if (!validarCampos()) {
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
                precioText = binding.etPrecio.text.toString()
                costoUnitario = if (precioText.isNotEmpty()) {
                    precioText.toDoubleOrNull() ?: 0.0
                } else {
                    // Si el precio no se escribió, tomar el que viene del último resultado
                    (ultimaSeleccion?.costuni!!)
                }

                importe= (costoUnitario * cantidad).toString()
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

            // Enviamos el producto al callback para que se agregue al RecyclerView principal
            onItemSelected(productoConDatos)

            // Cerramos el BottomSheet
            dismiss()
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
        if (precio.isEmpty() && binding.etPrecio.isEnabled) {
            binding.etPrecio.error = "Precio"
            return false
        }
        return true
    }
    //funcion para hacer la busqueda de los activos en base a la opcion seleccionada
    private fun buscar(opcion: String, texto: String) {
        lifecycleScope.launch {
            try {

                val request = ProductosRequest(Login(user, pass), opcion, texto)
                System.out.println("productosrequest:"+request)
                val response = apiService.getProductos(request)
                System.out.println("productosresponse:"+response)
                if (response.isSuccessful) {
                    val resultados = response.body()
                    System.out.println("productoresultados:"+resultados)
                    resultados?.let {prod ->
                        val okItem = prod?.ResponseProductos?.find{ it.ok != null }
                        if (okItem?.ok == "1") {
                            if(tipobusqueda =="1"){binding.etPrecio.isEnabled = okItem.costun != "0"}
                            val productosValidos = prod.ResponseProductos?.filter { it.cve != null } ?: emptyList()
                            if (productosValidos.isNotEmpty()) {
                                val lista = mutableListOf<ProductoUI>()
                                for (item in productosValidos) {
                                    var cantidadText = binding.etCantidad.text.toString()
                                    var cantidad = cantidadText.toDoubleOrNull() ?: 0.0
                                    var precioText = binding.etPrecio.text.toString()
                                    var costoUnitario = item.costun?.toDoubleOrNull() ?: precioText.toDoubleOrNull() ?: 0.0
                                    var importe = (costoUnitario * cantidad).toString()
                                    //ifpara carga mano de obra, si es tipo 2 se cargan tiempo y se haceuncalculopara minutos
                                    var hr = 0.0
                                    var min = 0.0
                                    if(tipobusqueda == "2"){
                                        hr = item.tst?.toDoubleOrNull() ?: 0.0
                                        min =hr * 60.0
                                        cantidad = hr
                                        importe = "0.0"

                                        println("Entro al if")
                                    }

                                    val productoUI = ProductoUI(
                                        cve = item.cve,
                                        cant = cantidad,
                                        uni = item.uni,
                                        costuni = costoUnitario,
                                        importe = importe.toDouble(),
                                        descripcion = item.name,
                                        minutos = min,
                                        horas = hr
                                    )
                                    System.out.println("productoUI:"+productoUI)
                                    lista.add(productoUI)
                                }
                                if (binding.recyclerResultadosRM.adapter == null) {
                                    binding.recyclerResultadosRM.layoutManager = LinearLayoutManager(requireContext())
                                    val adapter = ResultadoBuscarRMAdapter(lista) { seleccionado ->

                                        if(tipobusqueda == "2"){
                                            binding.etPrecio.setText(seleccionado.cant.toString())
                                            binding.etCantidad.setText(seleccionado.horas.toString())
                                            println("costo:"+seleccionado.costuni+"cant:"+seleccionado.cant)
                                            ultimaSeleccion = seleccionado
                                            onItemSelected(seleccionado)
                                            dismiss()
                                        }else{
                                            ultimaSeleccion = seleccionado

                                        }
                                        System.out.println("seleccionadoBRMBottom:"+seleccionado)

                                        //cambiamos el color del item seleccionado

                                        //Toast.makeText(requireContext(), "Producto seleccionado: ${seleccionado.descripcion}", Toast.LENGTH_SHORT).show()


                                    }
                                    binding.recyclerResultadosRM.adapter = adapter
                                } else {
                                    // Si ya existe el adapter, actualizar los datos
                                    (binding.recyclerResultadosRM.adapter as? ResultadoBuscarRMAdapter)?.let { adapter ->
                                        adapter.actualizarLista(lista)
                                    }
                                }
                            }

                        } else {
                            // Mostrar error específico o mensaje de no resultados
                            val errorMessage = prod.ResponseProductos?.firstOrNull()?.error ?: "No se encontraron resultados"
                            Toast.makeText(requireContext(), "Error: $errorMessage", Toast.LENGTH_SHORT).show()

                            // Limpiar RecyclerView si no hay resultados
                            (binding.recyclerResultadosRM.adapter as? ResultadoBuscarRMAdapter)?.actualizarLista(emptyList())
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                System.out.println("error:"+e.message)
                Toast.makeText(requireContext(), "Error en la búsqueda", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
