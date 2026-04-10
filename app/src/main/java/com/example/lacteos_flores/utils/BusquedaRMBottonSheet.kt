package com.example.lacteos_flores.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lacteos_flores.adapters.ResultadoBuscarRMAdapter
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

            } catch (e: Exception) {
                System.out.println("error:"+e.message)
                Toast.makeText(requireContext(), "Error en la búsqueda", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
