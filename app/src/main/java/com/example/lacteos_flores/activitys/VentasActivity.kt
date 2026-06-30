package com.example.lacteos_flores.activitys

import android.Manifest
import android.util.Log
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.text.InputFilter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lacteos_flores.R
import com.example.lacteos_flores.adapters.OrdenesAdapter
import com.example.lacteos_flores.adapters.RefaccionesAdapter
import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.data.ClientsEntity
import com.example.lacteos_flores.data.DoctosEntity
import com.example.lacteos_flores.data.ItemAuxEntity
import com.example.lacteos_flores.data.Kdm1Entity
import com.example.lacteos_flores.data.Kdm2Entity
import com.example.lacteos_flores.data.PantallasEntity
import com.example.lacteos_flores.data.ProductosEntity
import com.example.lacteos_flores.data.UsuarioDao
import com.example.lacteos_flores.interfaz.RetrofitClient
import com.example.lacteos_flores.models.Login
import com.example.lacteos_flores.models.LoginRequest
import com.example.lacteos_flores.models.OrdenItem
import com.example.lacteos_flores.models.OrdenesRequest
import com.example.lacteos_flores.models.modelsUI.ProductoUI
import com.example.lacteos_flores.utils.BusquedaRMBottomSheet
import com.example.lacteos_flores.utils.BusquedaTecBottonSheet
import com.example.lacteos_flores.utils.Globales
import com.example.lacteos_flores.utils.Prefs
import com.example.lacteos_flores.utils.TicketPrinter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.toString

class VentasActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var etAlmacen: TextView
    private lateinit var adapter: OrdenesAdapter
    private lateinit var etFecha: TextView
    private lateinit var etMoneda: EditText
    private lateinit var btnBuscarCliente: Button
    private lateinit var etCodigoCliente: EditText
    private lateinit var etNombreCliente: TextView
    private lateinit var btnBuscarProducto: Button
    private lateinit var btnGuardar: Button
    private lateinit var etProducto: EditText
    private lateinit var spTipoDoc: Spinner
    private lateinit var spTipoRfc: Spinner
    private lateinit var etSubTotal: EditText
    private lateinit var etIva: EditText
    private lateinit var etTotal: EditText

    //variables para base de datos
    private lateinit var db: AppDatabase
    private lateinit var loginUserDao: UsuarioDao
    //vairables locales
    private var usuario: String? = null
    private var pass: String? = null
    private var almID: String? = null
    private var almUsurio: String? = null
    private var filteredDoctos: List<DoctosEntity> = listOf()
    private var selectedClient: ClientsEntity? = null

    private lateinit var hproductsAdapter: RefaccionesAdapter

    // Launcher para permisos de Bluetooth
    private val requestBluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            Toast.makeText(this, "Permisos concedidos. Intente imprimir de nuevo.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Se requieren permisos de Bluetooth para imprimir.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ventas)

        relacionaView()
        Inicializa()
        setupListeners()
    }
    private fun relacionaView(){
        recyclerView = findViewById(R.id.rvProductos)
        etAlmacen = findViewById(R.id.tvAlmacen)
        etFecha = findViewById(R.id.tvFecha)
        etCodigoCliente = findViewById(R.id.etCodigoCliente)
        etNombreCliente = findViewById(R.id.tvNombreCliente)
        btnBuscarProducto = findViewById(R.id.btnBuscarProducto)
        btnGuardar = findViewById(R.id.btnAceptar)
        etProducto = findViewById(R.id.etProducto)
        spTipoDoc = findViewById(R.id.spinnerTipoDoc)
        etSubTotal = findViewById(R.id.etSubTotal)
        etIva = findViewById(R.id.etIva)
        etTotal = findViewById(R.id.etTotal)

    }

    private fun Inicializa(){
        val fecAct = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        usuario = Prefs(this).obtenerUsuario().first.toString()
        pass = Prefs(this).obtenerUsuario().second.toString()

        //inicializamos la base de datos
        db = AppDatabase.getDatabase(this)
        loginUserDao = db.usuarioDao()
        //inicializamos el adapter con callback para recalcular totales automáticamente al editar/eliminar
        hproductsAdapter = RefaccionesAdapter(mutableListOf(), listOf("Clave", "Cant", "Uni", "Precio", "Importe"), true, {
            calcularTotales()
        }, { item, pos ->
            mostrarDialogoEdicionTMC(item, pos)
        })
        recyclerView.adapter = hproductsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        etFecha.setText(fecAct)
        //evento para abrir el fdate picker
        etFecha.setOnClickListener {
            showDatePicker(etFecha)
        }
        cargarInfoLocal()


    }

    //funcion para confiurar los listeners de los botones
    private fun setupListeners() {
        etNombreCliente.setOnClickListener {
            selectedClient?.let { cliente ->
                mostrarDialogoInfoCliente(cliente)
            } ?: Toast.makeText(this, "Seleccione un cliente primero", Toast.LENGTH_SHORT).show()
        }
        etCodigoCliente.setOnClickListener {
            // Aquí puedes implementar la lógica para guardar los datos
            buscarCliente()
        }
        btnBuscarProducto.setOnClickListener {
            // Aquí puedes implementar la lógica para guardar los datos
            buscarProductos()
        }
        btnGuardar.setOnClickListener {
            GuardadDocumentosLocal()
        }
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                
                AlertDialog.Builder(this@VentasActivity)
                    .setTitle("Eliminar Producto")
                    .setMessage("¿Está seguro de que desea eliminar este producto de la lista?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        hproductsAdapter.eliminarItem(position)
                    }
                    .setNegativeButton("Cancelar") { dialog, _ ->
                        hproductsAdapter.notifyItemChanged(position)
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)


    }

    //funcionobtener la informacion lcoal de la base de datos, almacen y moneda
    private fun cargarInfoLocal(){
        lifecycleScope.launch {
            try {
                //cargamos el alamacen del usaurios en el edtAlamcen del layout
                val alm = db.usuarioDao().obtenerUsuario(usuario.toString())
                etAlmacen.setText(alm?.almacen)
                //cargamos la moneda del usaurios en el edtMoneda del layout
               // val mon = db.monedaDao().obtenerMonedas()
                //de momento se dajara a pesos solo para la venta posterioemente para versiones futuras adaptarlo a un spinner para cargar los tipode de monedas
               // etMoneda.setText(mon[0]?.moneda)
                //buscamos los documentos disponibles para ponerlo en el spinnerTipoDoc y mostrando las descripciones
                val doctos = db.doctosDao().obtenerDocumentos()
                //Filtramos por el tipo de documento a trabajar en la pantalla
                filteredDoctos = doctos.filter { it.gen == "U" && it.nat == "D" && it.grp == "45"}
                val descripciones = filteredDoctos.map { it.descripcion }

                val adapterDoctos = ArrayAdapter(this@VentasActivity, android.R.layout.simple_spinner_item, descripciones)
                adapterDoctos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spTipoDoc.adapter = adapterDoctos

            }catch (e: Exception){
                println("error:"+e)
                Toast.makeText(this@VentasActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //funcion para buscar cliente abriendo el bottom sheet de clientes y haciendo la busqieda en la tabla clientes local
    private fun buscarCliente() {
        //1.- Validar si el limite de credito o sus dias no esta exedido para permitir la venta
        //2.- Validar el rfc
        //  Si el cliente tiene rfc generico , sus ventas serian de remision para posteriormente hacer factura global,
        //	Si el cliente tiene rfc registrado, sus ventas serian facturas ya sea de contado o de credito
        //	si el cliente tiene rfc generico y a crédito, su venta seria factura a credito

        val bottomSheetCliente = BusquedaTecBottonSheet{ cli ->
            selectedClient = cli
            etNombreCliente.setText(cli.nombre)
            etCodigoCliente.setText(cli.clave)
            calcularTotales()
            val limcre = cli.limcre
            //obtenemos los movimientos del cliente para validar el limite de credito y los dias de credito
            lifecycleScope.launch {
                try {
                    val movim = db.kdm1Dao().obtenerMovimiento(cli.clave.toString()) ?: 0.0
                    if (movim > limcre.toDouble()){
                        Toast.makeText(this@VentasActivity, "El limite de credito se ha excedido", Toast.LENGTH_SHORT).show()
                        //si el limite de credito se excede se deja la venta solo de contado

                    }else{
                        Toast.makeText(this@VentasActivity, "El limite de credito no se ha excedido", Toast.LENGTH_SHORT).show()
                    }
                    //validamos loz dias de vencimiento del cliente
                    val dias = db.carteraDao().obtenerDocVence(cli.clave.toString())
                    if (dias.isNotEmpty() ){
                        Toast.makeText(this@VentasActivity, "El cliente tiene dias de vencimiento", Toast.LENGTH_SHORT).show()
                        //si el cliente tiene dias de vencimiento se deja la venta solo de contado
                        //dejamos el spinner de tipo documento fijo en contado
                        spTipoDoc.setSelection(0)
                        //bloqueadmos el selector de tipo de documento para que haga ventas a credito para el cliente
                        spTipoDoc.isEnabled = false

                    }else{
                        Toast.makeText(this@VentasActivity, "El cliente no tiene dias de vencimiento", Toast.LENGTH_SHORT).show()
                    }

                    //consultaremos el estado de cuenta del cliente, filtrando los documentos que tengan los dias de vencimiento mayor a los dias permitidos


                }catch (e: Exception){
                    println("error:"+e)
                    Toast.makeText(this@VentasActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            }



        }
        bottomSheetCliente.show(supportFragmentManager, "BusquedaTecBottomSheet")

    }
    //funcion que abrira un dialog con la informacion del cliente
    private fun mostrarDialogoInfoCliente(cliente: ClientsEntity) {
        val direccion = "${cliente.calle}, ${cliente.colo}, ${cliente.pobl}, CP: ${cliente.cp}"
        val limiteFormateado = String.format(Locale.US, "%,.2f", cliente.limcre.toDoubleOrNull() ?: 0.0)
        
        AlertDialog.Builder(this)
            .setTitle("Información del Cliente")
            .setMessage("""
                Nombre: ${cliente.nombre}
                RFC: ${cliente.rfc}
                Límite de Crédito: $ $limiteFormateado
                Dirección: $direccion
            """.trimIndent())
            .setPositiveButton("Cerrar", null)
            .show()
    }

    //funcion para validaciones de los clientes limite de credito y dias de credito
    private fun validarCliente(){

    }

    //funcion para reallizar la busqueda de productos
    private fun buscarProductos() {
        val yaAgregados = hproductsAdapter.obtenerLista()
        val bottomSheet = BusquedaRMBottomSheet("1", false, yaAgregados) { seleccionado ->
            
            lifecycleScope.launch {
                val productoBase = db.productosDao().obtenerProducto(seleccionado.cve ?: "")
                
                // Mostrar diálogo para capturar cantidad y atributos (Talla, Modelo, Color)
                val dialogView = LayoutInflater.from(this@VentasActivity).inflate(R.layout.dialog_devolucion_item, null)
                val tvInfo: TextView = dialogView.findViewById(R.id.tv_producto_info)
                val etCant: EditText = dialogView.findViewById(R.id.et_cantidad_dev)
                val etLote: EditText = dialogView.findViewById(R.id.et_lote_dev)
                val etPrecio: EditText = dialogView.findViewById(R.id.et_precio_dev)
                val etTalla: EditText = dialogView.findViewById(R.id.et_talla_dev)
                val etModelo: EditText = dialogView.findViewById(R.id.et_modelo_dev)
                val etColor: EditText = dialogView.findViewById(R.id.et_color_dev)

                // En VENTAS el lote es AUTOMÁTICO, ocultamos el campo
                etLote.visibility = View.GONE
                
                // Forzar MAYUSCULAS en los campos TMC
                etTalla.filters = arrayOf(InputFilter.AllCaps())
                etModelo.filters = arrayOf(InputFilter.AllCaps())
                etColor.filters = arrayOf(InputFilter.AllCaps())

                // Configurar visibilidad según el campo 'tmc' de la DB
                when (productoBase?.tmc) {
                    "1" -> {
                        etTalla.visibility = View.VISIBLE
                        etModelo.visibility = View.GONE
                        etColor.visibility = View.GONE
                        configurarSeleccionCatalogo(etTalla, seleccionado.cve ?: "", "TALLA")
                    }
                    "2" -> {
                        etTalla.visibility = View.VISIBLE
                        etModelo.visibility = View.VISIBLE
                        etColor.visibility = View.GONE
                        configurarSeleccionCatalogo(etTalla, seleccionado.cve ?: "", "TALLA")
                        configurarSeleccionCatalogo(etModelo, seleccionado.cve ?: "", "MODELO")
                    }
                    "3" -> {
                        etTalla.visibility = View.VISIBLE
                        etModelo.visibility = View.VISIBLE
                        etColor.visibility = View.VISIBLE
                        configurarSeleccionCatalogo(etTalla, seleccionado.cve ?: "", "TALLA")
                        configurarSeleccionCatalogo(etModelo, seleccionado.cve ?: "", "MODELO")
                        configurarSeleccionCatalogo(etColor, seleccionado.cve ?: "", "COLOR")
                    }
                    else -> {
                        etTalla.visibility = View.GONE
                        etModelo.visibility = View.GONE
                        etColor.visibility = View.GONE
                    }
                }

                tvInfo.text = "${seleccionado.cve} - ${seleccionado.descripcion}"
                etCant.setText("0.0")
                /*val costUn = seleccionado.costuni ?: 0.0
                val descuP = selectedClient?.descuentop
                val descuento = descuP?.toDoubleOrNull() // Convierte a Double?; si falla, devuelve null

                val prec = if (descuento != null && descuento != 0.0) {
                    costUn - descuento   // Aplica el descuento
                } else {
                    costUn               // Sin descuento
                }*/
                etPrecio.setText(seleccionado.costuni.toString())
                etPrecio.isEnabled = false // Precio no editable
                etPrecio.isVisible = false

                val dialog = AlertDialog.Builder(this@VentasActivity)
                    .setTitle("Detalle de Producto")
                    .setView(dialogView)
                    .setPositiveButton("Agregar", null) // Lo configuramos después para validar sin cerrar
                    .setNegativeButton("Cancelar", null)
                    .create()

                dialog.show()

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val cant = etCant.text.toString().toDoubleOrNull() ?: 0.0
                    val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
                    val currentTalla = etTalla.text.toString().ifEmpty { "-" }.uppercase()
                    val currentModelo = etModelo.text.toString().ifEmpty { "-" }.uppercase()
                    val currentColor = etColor.text.toString().ifEmpty { "-" }.uppercase()

                    // Validaciones según TMC
                    val tmc = productoBase?.tmc ?: "0"
                    if (tmc >= "1" && currentTalla == "-") {
                        Toast.makeText(this@VentasActivity, "La Talla es obligatoria", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (tmc >= "2" && currentModelo == "-") {
                        Toast.makeText(this@VentasActivity, "El Modelo es obligatorio", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (tmc >= "3" && currentColor == "-") {
                        Toast.makeText(this@VentasActivity, "El Color es obligatorio", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (cant <= 0) {
                        Toast.makeText(this@VentasActivity, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    lifecycleScope.launch {
                        // Obtener lista actualizada directamente del adapter
                        val listaActual = hproductsAdapter.obtenerLista()

                        // Validar stock total para esta combinación específica de TMC
                        val lotesDisponibles = db.existenciasDao().obtenerLotesDisponibles(seleccionado.cve ?: "").filter {
                            it.talla.equals(currentTalla, ignoreCase = true) &&
                            it.modelo.equals(currentModelo, ignoreCase = true) &&
                            it.color.equals(currentColor, ignoreCase = true)
                        }
                        
                        var stockDisponibleTMC = lotesDisponibles.sumOf { it.existencias.toDoubleOrNull() ?: 0.0 }
                        
                        // Restar lo ya agregado en la tabla para esta variante exacta
                        val yaAgregado = listaActual.filter { 
                            it.cve == seleccionado.cve && 
                            it.talla.equals(currentTalla, ignoreCase = true) && 
                            it.modelo.equals(currentModelo, ignoreCase = true) && 
                            it.color.equals(currentColor, ignoreCase = true)
                        }.sumOf { it.cant ?: 0.0 }
                        
                        stockDisponibleTMC -= yaAgregado

                        if (cant > stockDisponibleTMC) {
                            Toast.makeText(this@VentasActivity, "Stock insuficiente para esta variante. Disponible: $stockDisponibleTMC", Toast.LENGTH_LONG).show()
                            return@launch
                        }

                        val refaccion = ProductoUI(
                            cve = seleccionado.cve, 
                            cant = cant, 
                            uni = seleccionado.uni, 
                            costuni = precio, 
                            costbase = precio, 
                            importe = cant * precio, 
                            descripcion = seleccionado.descripcion,
                            talla = currentTalla,
                            modelo = currentModelo,
                            color = currentColor,
                            lote = "MULTIPLE" // Indica que se usará FIFO al guardar
                        )

                        hproductsAdapter.agregarItem(refaccion)
                        calcularTotales()
                        dialog.dismiss()
                    }
                }
            }
        }
        bottomSheet.show(supportFragmentManager, "BusquedaRMBottomSheet")
    }

    private fun mostrarDialogoEdicionTMC(item: ProductoUI, position: Int) {
        lifecycleScope.launch {
            val productoBase = db.productosDao().obtenerProducto(item.cve ?: "") ?: return@launch
            
            val dialogView = LayoutInflater.from(this@VentasActivity).inflate(R.layout.dialog_editar_item, null)
            val etCant: EditText = dialogView.findViewById(R.id.et_cantidad_edit)
            val etPrecio: EditText = dialogView.findViewById(R.id.et_precio_edit)
            val etTalla: EditText = dialogView.findViewById(R.id.et_talla_edit)
            val etModelo: EditText = dialogView.findViewById(R.id.et_modelo_edit)
            val etColor: EditText = dialogView.findViewById(R.id.et_color_edit)

            val costUn = item.costuni ?: 0.0
            val descuP = selectedClient?.descuentop
            val descuento = descuP?.toDoubleOrNull()
            val prec = if (descuento != null && descuento != 0.0 ){
                costUn - descuento
            }else{costUn}
            etCant.setText(item.cant.toString())
            etPrecio.setText(prec.toString())
            etPrecio.isEnabled = false // Precio no editable
            etTalla.setText(item.talla)
            etModelo.setText(item.modelo)
            etColor.setText(item.color)

            // Configurar visibilidad según el campo 'tmc'
            when (productoBase.tmc) {
                "1" -> {
                    etTalla.visibility = View.VISIBLE
                    configurarSeleccionCatalogo(etTalla, item.cve ?: "", "TALLA")
                }
                "2" -> {
                    etTalla.visibility = View.VISIBLE
                    etModelo.visibility = View.VISIBLE
                    configurarSeleccionCatalogo(etTalla, item.cve ?: "", "TALLA")
                    configurarSeleccionCatalogo(etModelo, item.cve ?: "", "MODELO")
                }
                "3" -> {
                    etTalla.visibility = View.VISIBLE
                    etModelo.visibility = View.VISIBLE
                    etColor.visibility = View.VISIBLE
                    configurarSeleccionCatalogo(etTalla, item.cve ?: "", "TALLA")
                    configurarSeleccionCatalogo(etModelo, item.cve ?: "", "MODELO")
                    configurarSeleccionCatalogo(etColor, item.cve ?: "", "COLOR")
                }
            }

            val dialog = AlertDialog.Builder(this@VentasActivity)
                .setTitle("Editar Item")
                .setView(dialogView)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create()

            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nuevaCant = etCant.text.toString().toDoubleOrNull() ?: 0.0
                val nuevoPrecio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
                val currentTalla = etTalla.text.toString().ifEmpty { "-" }.uppercase()
                val currentModelo = etModelo.text.toString().ifEmpty { "-" }.uppercase()
                val currentColor = etColor.text.toString().ifEmpty { "-" }.uppercase()

                // Validaciones TMC
                val tmc = productoBase.tmc
                if (tmc >= "1" && currentTalla == "-") {
                    Toast.makeText(this@VentasActivity, "La Talla es obligatoria", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (tmc >= "2" && currentModelo == "-") {
                    Toast.makeText(this@VentasActivity, "El Modelo es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (tmc >= "3" && currentColor == "-") {
                    Toast.makeText(this@VentasActivity, "El Color es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (nuevaCant <= 0) {
                    Toast.makeText(this@VentasActivity, "Cantidad inválida", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    // Validar Stock para la nueva variante/cantidad
                    val lotesDisponibles = db.existenciasDao().obtenerLotesDisponibles(item.cve ?: "").filter {
                        it.talla.equals(currentTalla, ignoreCase = true) &&
                        it.modelo.equals(currentModelo, ignoreCase = true) &&
                        it.color.equals(currentColor, ignoreCase = true)
                    }
                    
                    var stockDisponibleTMC = lotesDisponibles.sumOf { it.existencias.toDoubleOrNull() ?: 0.0 }
                    
                    // Restar otros items del mismo producto (excepto el que estamos editando)
                    val yaAgregado = hproductsAdapter.obtenerLista().filterIndexed { idx, p -> 
                        idx != position && p.cve == item.cve && 
                        p.talla.equals(currentTalla, ignoreCase = true) && 
                        p.modelo.equals(currentModelo, ignoreCase = true) && 
                        p.color.equals(currentColor, ignoreCase = true)
                    }.sumOf { it.cant ?: 0.0 }
                    
                    stockDisponibleTMC -= yaAgregado

                    if (nuevaCant > stockDisponibleTMC) {
                        Toast.makeText(this@VentasActivity, "Stock insuficiente. Disponible: $stockDisponibleTMC", Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    // Aplicar cambios
                    item.cant = nuevaCant
                    item.costuni = nuevoPrecio
                    item.importe = nuevaCant * nuevoPrecio
                    item.talla = currentTalla
                    item.modelo = currentModelo
                    item.color = currentColor

                    hproductsAdapter.notifyItemChanged(position + 1)
                    calcularTotales()
                    dialog.dismiss()
                }
            }
        }
    }

    private fun configurarSeleccionCatalogo(editText: EditText, claveProd: String, tipo: String) {
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
                    val arrayOpciones = opciones.toTypedArray()
                    AlertDialog.Builder(this@VentasActivity)
                        .setTitle("Seleccione $tipo")
                        .setItems(arrayOpciones) { _, which ->
                            editText.setText(arrayOpciones[which])
                        }
                        .show()
                } else {
                    Toast.makeText(this@VentasActivity, "No hay catálogo disponible para $tipo", Toast.LENGTH_SHORT).show()
                    editText.isFocusableInTouchMode = true
                    editText.requestFocus()
                }
            }
        }
    }

    private fun showDatePicker(campoFecha: TextView) {
        val calendario = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val fechaSeleccionada = "$year-${month + 1}-$dayOfMonth"
                campoFecha.setText(fechaSeleccionada)
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun calcularTotales() {
        val lista = hproductsAdapter.obtenerLista()
        var subtotal = 0.0
        var totalIva = 0.0
        val descuentoCliente = selectedClient?.descuentop?.toDoubleOrNull() ?: 0.0

        for (item in lista) {
            val cant = item.cant ?: 0.0
            val costbase = item.costbase ?: 0.0
            
            // Aplicar descuento al costo unitario
            // El descuento se resta directamente de cada unidad
            val costUniEfectivo = if (descuentoCliente > 0 && descuentoCliente < costbase) {
                costbase - descuentoCliente
            } else {
                costbase
            }
            
            item.costuni = costUniEfectivo
            item.importe = cant * costUniEfectivo
            
            subtotal += item.importe!!
        }
        val total = subtotal + totalIva
        
        // Notificar al adapter que los precios cambiaron (costuni e importe)
        hproductsAdapter.notifyDataSetChanged()

        etSubTotal.setText(String.format(Locale.US, "%.2f", subtotal))
        etIva.setText(String.format(Locale.US, "%.2f", totalIva))
        etTotal.setText(String.format(Locale.US, "%.2f", total))
    }

    private fun GuardadDocumentosLocal() {
        // Primero verificamos permisos antes de proceder con el guardado si queremos imprimir
        if (!tienePermisosBluetooth()) {
            solicitarPermisosBluetooth()
            return
        }

        val cliente = etCodigoCliente.text.toString()
        val listaPartidas = hproductsAdapter.obtenerLista()
        val subtotalValue = etSubTotal.text.toString().toDoubleOrNull() ?: 0.0

        // Validaciones
        if (cliente.isEmpty()) {
            Toast.makeText(this, "Debe seleccionar un cliente", Toast.LENGTH_SHORT).show()
            return
        }
        if (listaPartidas.isEmpty()) {
            Toast.makeText(this, "Debe agregar al menos un producto", Toast.LENGTH_SHORT).show()
            return
        }
        if (subtotalValue <= 0) {
            Toast.makeText(this, "El monto total debe ser mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val selectedDocPos = spTipoDoc.selectedItemPosition
                if (selectedDocPos < 0 || filteredDoctos.isEmpty()) {
                    Toast.makeText(this@VentasActivity, "Tipo de documento no válido", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                // 1. Obtener datos del usuario
                val userKey = Globales.usuario ?: ""
                val usuario = db.usuarioDao().obtenerUsuario(userKey)
                if (usuario == null) {
                    Globales.showToast(this@VentasActivity, "Error: Usuario no encontrado")
                    return@launch
                }
                val docConfig = filteredDoctos[selectedDocPos]
                val fecha = etFecha.text.toString()
                val almacen = etAlmacen.text.toString()
                val montoTotalVenta = etTotal.text.toString().toDoubleOrNull() ?: 0.0

                // 1. Validar Límite de Crédito antes de guardar (si es crédito)
                val clienteActual = db.clientsDao().obtenerCliente(cliente)
                val condicionPago = spTipoDoc.selectedItem.toString()
                var tipoCon = "Pago De Contado"
                if (condicionPago.uppercase().contains("CREDITO") || condicionPago.uppercase().contains("CREDÍTO")) {
                    val limiteDisponible = clienteActual?.limcre?.toDoubleOrNull() ?: 0.0
                    if (montoTotalVenta > limiteDisponible) {
                        Toast.makeText(this@VentasActivity, "Crédito insuficiente. Disponible: $limiteDisponible", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    tipoCon= clienteActual?.plazo +" dias de factura"
                }

                // Header (Kdm1)
                val kdm1 = Kdm1Entity(
                    suc = usuario.cve_suc,
                    alm = usuario.cve_alma,
                    gen = docConfig.gen,
                    nat = docConfig.nat,
                    grp = docConfig.grp,
                    tip = docConfig.tipo,
                    fecha = fecha,
                    cliente = cliente,
                    moneda = "PESOS",
                    pari = "1",
                    rfc = selectedClient?.rfc ?: "",
                    venc = fecha,
                    condi = tipoCon,
                    agent = usuario.usuario ?: "",
                    lati = selectedClient?.latitud ?: "0.0",
                    long = selectedClient?.longitud ?: "0.0",
                    subtotal = etSubTotal.text.toString(),
                    iva = etIva.text.toString(),
                    monto = etTotal.text.toString(),
                    staSinc = "N"
                )

                val idDoc = db.kdm1Dao().insertaDocumento(kdm1)

                // Partidas (Kdm2) e inventario
                val partidas = mutableListOf<Kdm2Entity>()
                val partidasAux = mutableListOf<ItemAuxEntity>()

                listaPartidas.forEachIndexed { index, item ->
                    val partidaNum = (index + 1).toString()
                    var cantidadRestante = item.cant ?: 0.0


                    // 1. Crear Partida Kdm2 (Encabezado de la partida)
                    val originalImportePartida = item.importe ?: 0.0

                    partidas.add(Kdm2Entity(
                        iddoc = idDoc,
                        suc = usuario.cve_suc,
                        alm = usuario.cve_alma,
                        gen = docConfig.gen,
                        nat = docConfig.nat,
                        grp = docConfig.grp,
                        tip = docConfig.tipo,
                        partida = partidaNum,
                        producto = item.cve ?: "",
                        cantidad = cantidadRestante.toString(),
                        descrip = item.descripcion ?: "",
                        unidad = item.uni ?: "",
                        precio = item.costuni.toString(),
                        importe = String.format(Locale.US, "%.2f", originalImportePartida),
                        iva = String.format(Locale.US, "%.2f", 0.0)
                    ))

                    // 2. Lógica FIFO para descontar de múltiples lotes si es necesario
                    // Filtramos por los atributos seleccionados (Talla, Modelo, Color)
                    val lotesDisponibles = db.existenciasDao().obtenerLotesDisponibles(item.cve ?: "").filter {
                        (item.talla == "-" || it.talla == item.talla) &&
                        (item.modelo == "-" || it.modelo == item.modelo) &&
                        (item.color == "-" || it.color == item.color)
                    }
                    
                    for (loteEntity in lotesDisponibles) {
                        if (cantidadRestante <= 0) break

                        val stockEnLote = loteEntity.existencias.toDoubleOrNull() ?: 0.0
                        if (stockEnLote <= 0) continue

                        val cantATomar = if (cantidadRestante <= stockEnLote) cantidadRestante else stockEnLote
                        
                        // Registro en ItemAux para este lote
                        partidasAux.add(ItemAuxEntity(
                            iddoc = idDoc,
                            suc = usuario.cve_suc,
                            alm = usuario.cve_alma,
                            gen = docConfig.gen,
                            nat = docConfig.nat,
                            grp = docConfig.grp,
                            tip = docConfig.tipo,
                            auxiliar = loteEntity.auxiliar,
                            partida = partidaNum,
                            producto = item.cve ?: "",
                            cantidad = cantATomar.toString(),
                            talla = loteEntity.talla,
                            modelo = loteEntity.modelo,
                            color = loteEntity.color
                        ))

                        // Actualización de Existencias en la base de datos local
                        val nuevoStock = stockEnLote - cantATomar
                        db.existenciasDao().actualizarExistencia(
                            item.cve ?: "",
                            loteEntity.auxiliar,
                            loteEntity.talla,
                            loteEntity.modelo,
                            loteEntity.color,
                            String.format(Locale.US, "%.2f", nuevoStock)
                        )

                        cantidadRestante -= cantATomar
                    }
                    
                    // Si después de recorrer lotes aún queda cantidadRestante, 
                    // significa que se vendió más de lo que había en lotes (o no había lotes)
                    if (cantidadRestante > 0) {
                        Log.w("Ventas", "Atención: El producto ${item.cve} se vendió con saldo negativo en lotes por $cantidadRestante")
                    }
                }

                db.kdm2Dao().insertaPartidas(partidas)
                if (partidasAux.isNotEmpty()) {
                    db.itemAuxDao().insertaPartidasAux(partidasAux)
                }

                // 3. Actualizar Límite de Crédito del Cliente (Control de saldo disponible)
                if (clienteActual != null) {
                    val limiteActual = clienteActual.limcre.toDoubleOrNull() ?: 0.0
                    val nuevoLimite = limiteActual - montoTotalVenta
                    
                    db.clientsDao().actualizarLimiteCredito(
                        cliente, 
                        String.format(Locale.US, "%.2f", nuevoLimite)
                    )
                    Log.d("Ventas", "Nuevo límite de crédito para $cliente: $nuevoLimite")
                }

                Toast.makeText(this@VentasActivity, "Documento guardado localmente", Toast.LENGTH_SHORT).show()
                
                // Imprimir ticket después de guardar
                imprimirTicketVenta(kdm1, listaPartidas)

                // Intentar sincronizar en segundo plano por ahora se queda a envio manual desde el sincronizador
               // sincronizarDocumentoKepler(idDoc)

                finish()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@VentasActivity, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun tienePermisosBluetooth(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION)
        }
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun solicitarPermisosBluetooth() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION)
        }
        requestBluetoothPermissionLauncher.launch(permissions)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    private fun sincronizarDocumentoKepler(iddoc: Long) {
        if (!isNetworkAvailable()) {
            Log.w("Ventas", "Sin conexión a internet. Sincronización pendiente.")
            return
        }

        // Usamos GlobalScope para que la tarea sobreviva al cierre de la Activity (finish())
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val catalogosManager = com.example.lacteos_flores.controllers.CatalogosManager(db)
                val login = com.example.lacteos_flores.models.Login(usuario.toString(), pass.toString())
                
                catalogosManager.enviarVenta(iddoc, login)
                Log.d("Ventas", "Sincronización exitosa en segundo plano para ID: $iddoc")
            } catch (e: Exception) {
                Log.e("Ventas", "Error al sincronizar en segundo plano: ${e.message}")
            }
        }
    }

    private fun imprimirTicketVenta(header: Kdm1Entity, partidas: List<ProductoUI>) {
        val printer = TicketPrinter(this)
        // Actualizado con el nombre real de tu impresora: Printer001-664B
            printer.connectAndPrint("Printer001") {
            setAlignCenter()
            setBold(true)
            setLargeFont(false)
            printText("PRODUCTOS LACTEOS FLORES\n")

            setLargeFont(false)
            setBold(false)
            printText("R.F.C.: PLF010228TC3\n")
            printText("Calle: NICOLAS BRAVO\n")
            printText("Colonia: CENTRO\n")
            printText("Municipio: JIQUILPAN\n")
            printText("Telefono: 3535330998\n")
            printText("\n")
            printText("TICKET DE VENTA\n")
            printText("Impresion:${etFecha.text}\n")
            printDivider()

            setAlignLeft()
            printText("Forma de Venta: ${header.condi}\n")
            printText("Cliente: ${header.cliente}\n")
            printText("Nombre: ${etNombreCliente.text}\n")
            printDivider()

            // Formato de columnas para 32 caracteres (58mm)
            // CLAVE(8) CANT(5) PRECIO(9) TOTAL(10)
            val headerRow = String.format(Locale.US, "%-8s %-25s %-5s %-9s %-10s\n", "Clave","Producto" ,"Cant", "Precio", "Total")
            printText(headerRow)
            printDivider()

            for (item in partidas) {
                val line = String.format(Locale.US, "%-8s %-25s  %-5.1f %-9.2f %-10.2f\n",
                    item.cve?.take(8) ?: "",
                    item.descripcion ?: "",
                    item.cant ?: 0.0,
                    item.costuni ?: 0.0,
                    item.importe ?: 0.0
                )
                printText(line)
                
                // Imprimir atributos TMC si existen
                val atributos = mutableListOf<String>()
                if (item.talla != "-") atributos.add("T: ${item.talla}")
                if (item.modelo != "-") atributos.add("M: ${item.modelo}")
                if (item.color != "-") atributos.add("C: ${item.color}")
                
                if (atributos.isNotEmpty()) {
                    printText("   ${atributos.joinToString(" ")}\n")
                }
            }
            printDivider()

            setAlignRight()
            printText("Subtotal: $ ${header.subtotal}\n")
            printText("Impuesto: $ ${header.iva}\n")
            setBold(true)
            printText("TOTAL: $ ${header.monto}\n")
            setBold(false)

            setAlignCenter()
            printText("\n¡Gracias por su prefencia!\n")
        }
    }
}
