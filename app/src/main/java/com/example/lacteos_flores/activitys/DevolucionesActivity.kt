package com.example.lacteos_flores.activitys

import android.Manifest
import android.util.Log
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.example.lacteos_flores.data.ExistenciaEntity
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

class DevolucionesActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_devoluciones)

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
        
        // Ocultar campos financieros
        etSubTotal.visibility = View.GONE
        etIva.visibility = View.GONE
        etTotal.visibility = View.GONE

    }

    private fun Inicializa(){
        val fecAct = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        usuario = Prefs(this).obtenerUsuario().first.toString()
        pass = Prefs(this).obtenerUsuario().second.toString()

        //inicializamos la base de datos
        db = AppDatabase.getDatabase(this)
        loginUserDao = db.usuarioDao()
        
        //inicializamos el adapter OCULTANDO PRECIOS
        hproductsAdapter = RefaccionesAdapter(mutableListOf(), listOf("Clave", "Cant", "Uni"), false, {
            // No necesitamos calcular totales monetarios
        }, { item, pos ->
            mostrarDialogoEdicionTMC(item, pos)
        })
        recyclerView.adapter = hproductsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        etFecha.setText(fecAct)
        // Campo fecha no editable, solo muestra la actual
        etFecha.setOnClickListener(null)
        
        cargarInfoLocal()


    }

    //funcion para confiurar los listeners de los botones
    private fun setupListeners() {
        btnBuscarProducto.setOnClickListener {
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
                
                AlertDialog.Builder(this@DevolucionesActivity)
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
                
                //buscamos los documentos disponibles
                val doctos = db.doctosDao().obtenerDocumentos()
                
                // Filtrar específicamente por Entrada por Devolución UA101
                filteredDoctos = doctos.filter { 
                    (it.gen == "U" && it.nat == "A" && it.grp=="10" &&it.tipo == "1") ||
                    it.descripcion.uppercase().contains("UA101") ||
                    it.descripcion.uppercase().contains("DEVOLUCION")
                }
                
                if(filteredDoctos.isEmpty()){
                    filteredDoctos = doctos.filter { it.gen == "U" } // Fallback
                }
                
                val descripciones = filteredDoctos.map { it.descripcion }

                val adapterDoctos = ArrayAdapter(this@DevolucionesActivity, android.R.layout.simple_spinner_item, descripciones)
                adapterDoctos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spTipoDoc.adapter = adapterDoctos
                
                // Bloquear el spinner para que quede fijo
                if (filteredDoctos.isNotEmpty()) {
                    spTipoDoc.setSelection(0)
                    spTipoDoc.isEnabled = false
                }

            } catch (e: Exception){
                println("error:"+e)
                Toast.makeText(this@DevolucionesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //funcion para reallizar la busqueda de productos
    private fun buscarProductos() {
        // Usamos tipobusqueda "" y esDevolucion = true para permitir entrada sin validar stock previo
        val bottomSheet = BusquedaRMBottomSheet("", esDevolucion = true, emptyList()) { seleccionado ->
            
            lifecycleScope.launch {
                val productoBase = db.productosDao().obtenerProducto(seleccionado.cve ?: "")
                
                if (productoBase?.lotesf == "S" || productoBase?.lotesf == "F") {
                    // Mostrar diálogo para capturar cantidad y lote si el producto REQUIERE lote
                    val dialogView = LayoutInflater.from(this@DevolucionesActivity).inflate(R.layout.dialog_devolucion_item, null)
                    val tvInfo: TextView = dialogView.findViewById(R.id.tv_producto_info)
                    val etCant: EditText = dialogView.findViewById(R.id.et_cantidad_dev)
                    val etLote: EditText = dialogView.findViewById(R.id.et_lote_dev)
                    val etPrecio: EditText = dialogView.findViewById(R.id.et_precio_dev)
                    val etTalla: EditText = dialogView.findViewById(R.id.et_talla_dev)
                    val etModelo: EditText = dialogView.findViewById(R.id.et_modelo_dev)
                    val etColor: EditText = dialogView.findViewById(R.id.et_color_dev)

                    // Forzar MAYUSCULAS en el teclado y en el texto mientras se escribe
                    etLote.filters = arrayOf(InputFilter.AllCaps())
                    etTalla.filters = arrayOf(InputFilter.AllCaps())
                    etModelo.filters = arrayOf(InputFilter.AllCaps())
                    etColor.filters = arrayOf(InputFilter.AllCaps())

                    // Mostrar/Ocultar campos Talla, Modelo, Color basándose en el campo 'tmc' de la DB
                    when (productoBase.tmc) {
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
                        else -> { // Caso "0" o cualquier otro
                            etTalla.visibility = View.GONE
                            etModelo.visibility = View.GONE
                            etColor.visibility = View.GONE
                        }
                    }

                    // Ocultar precio
                    etPrecio.visibility = View.GONE

                    tvInfo.text = "${seleccionado.cve} - ${seleccionado.descripcion}"
                    etCant.setText(seleccionado.cant?.toString() ?: "1.0")

                    val dialog = AlertDialog.Builder(this@DevolucionesActivity)
                        .setTitle("Datos de Devolución")
                        .setView(dialogView)
                        .setPositiveButton("Agregar", null) // Configuramos después para validar
                        .setNegativeButton("Cancelar", null)
                        .create()

                    dialog.show()

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val cant = etCant.text.toString().toDoubleOrNull() ?: 0.0
                        val loteText = etLote.text.toString().uppercase()
                        val tallaText = etTalla.text.toString().uppercase()
                        val modeloText = etModelo.text.toString().uppercase()
                        val colorText = etColor.text.toString().uppercase()
                        
                        // Validaciones según TMC
                        val tmc = productoBase.tmc
                        if (tmc >= "1" && (tallaText.isEmpty() || tallaText == "-")) {
                            Toast.makeText(this@DevolucionesActivity, "La Talla es obligatoria", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        if (tmc >= "2" && (modeloText.isEmpty() || modeloText == "-")) {
                            Toast.makeText(this@DevolucionesActivity, "El Modelo es obligatorio", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        if (tmc >= "3" && (colorText.isEmpty() || colorText == "-")) {
                            Toast.makeText(this@DevolucionesActivity, "El Color es obligatorio", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (loteText.isEmpty()) {
                            Toast.makeText(this@DevolucionesActivity, "El lote es obligatorio", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (cant <= 0) {
                            Toast.makeText(this@DevolucionesActivity, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val refaccion = ProductoUI(
                            seleccionado.cve, 
                            cant, 
                            seleccionado.uni, 
                            0.0, 
                            0.0, 
                            seleccionado.descripcion,
                            lote = loteText,
                            talla = tallaText.ifEmpty { "-" },
                            modelo = modeloText.ifEmpty { "-" },
                            color = colorText.ifEmpty { "-" }
                        )

                        hproductsAdapter.agregarItem(refaccion)
                        dialog.dismiss()
                    }
                } else {
                    // Si NO requiere lote, lo agregamos directamente con la cantidad de la búsqueda
                    val refaccion = ProductoUI(
                        seleccionado.cve, 
                        seleccionado.cant ?: 1.0, 
                        seleccionado.uni, 
                        0.0, 
                        0.0, 
                        seleccionado.descripcion,
                        lote = "" // Sin lote
                    )
                    hproductsAdapter.agregarItem(refaccion)
                    Toast.makeText(this@DevolucionesActivity, "Producto agregado", Toast.LENGTH_SHORT).show()
                }
            }
        }
        bottomSheet.show(supportFragmentManager, "BusquedaRMBottomSheet")
    }

    private fun GuardadDocumentosLocal() {
        if (!tienePermisosBluetooth()) {
            solicitarPermisosBluetooth()
            return
        }

        val listaPartidas = hproductsAdapter.obtenerLista()
        if (listaPartidas.isEmpty()) {
            Toast.makeText(this, "Debe agregar al menos un producto", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val selectedDocPos = spTipoDoc.selectedItemPosition
                if (selectedDocPos < 0 || filteredDoctos.isEmpty()) {
                    Toast.makeText(this@DevolucionesActivity, "Tipo de documento no válido", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                // 1. Obtener datos del usuario
                val userKey = Globales.usuario ?: ""
                val usuarioEntity = db.usuarioDao().obtenerUsuario(userKey)
                if (usuarioEntity == null) {
                    Globales.showToast(this@DevolucionesActivity, "Error: Usuario no encontrado")
                    return@launch
                }
                
                val docConfig = filteredDoctos[selectedDocPos]
                val fecha = etFecha.text.toString()
                val almacen = etAlmacen.text.toString()

                // Header (Kdm1)
                val kdm1 = Kdm1Entity(
                    suc = usuarioEntity.cve_suc,
                    alm = usuarioEntity.cve_alma,
                    gen = docConfig.gen,
                    nat = docConfig.nat,
                    grp = docConfig.grp,
                    tip = docConfig.tipo,
                    fecha = fecha,
                    cliente = "", 
                    moneda = "PESOS",
                    pari = "1.0",
                    rfc = "",
                    venc = fecha,
                    condi = spTipoDoc.selectedItem.toString(),
                    agent = usuarioEntity.usuario,
                    lati = "0.0",
                    long = "0.0",
                    subtotal = "0.00",
                    iva = "0.00",
                    monto = "0.00",
                    staSinc = "N"
                )

                val idDoc = db.kdm1Dao().insertaDocumento(kdm1)

                val partidas = mutableListOf<Kdm2Entity>()
                val partidasAux = mutableListOf<ItemAuxEntity>()

                listaPartidas.forEachIndexed { index, item ->
                    val partidaNum = (index + 1).toString()
                    val cantidadTotalPartida = item.cant ?: 0.0

                    // 1. Crear Partida Kdm2 (Encabezado de la partida con el total)
                    partidas.add(Kdm2Entity(
                        iddoc = idDoc,
                        suc = usuarioEntity.cve_suc,
                        alm = almacen,
                        gen = docConfig.gen,
                        nat = docConfig.nat,
                        grp = docConfig.grp,
                        tip = docConfig.tipo,
                        partida = partidaNum,
                        producto = item.cve ?: "",
                        cantidad = cantidadTotalPartida.toString(),
                        descrip = item.descripcion ?: "",
                        unidad = item.uni ?: "",
                        precio = "0.00",
                        importe = "0.00",
                        iva = "0.00"
                    ))

                    // 2. Registro en ItemAux recorriendo el DESGLOSE DE LOTES
                    if (item.desgloseLotes.isNotEmpty()) {
                        item.desgloseLotes.forEach { detalle ->
                            partidasAux.add(ItemAuxEntity(
                                iddoc = idDoc,
                                suc = usuarioEntity.cve_suc,
                                alm = almacen,
                                gen = docConfig.gen,
                                nat = docConfig.nat,
                                grp = docConfig.grp,
                                tip = docConfig.tipo,
                                auxiliar = detalle.lote,
                                partida = partidaNum,
                                producto = item.cve ?: "",
                                cantidad = detalle.cantidad.toString(),
                                talla = detalle.talla,
                                modelo = detalle.modelo,
                                color = detalle.color
                            ))
                        }
                    } else if (!item.lote.isNullOrEmpty()) {
                        // Fallback por si acaso no se llenó el desglose pero hay un lote principal
                        partidasAux.add(ItemAuxEntity(
                            iddoc = idDoc,
                            suc = usuarioEntity.cve_suc,
                            alm = almacen,
                            gen = docConfig.gen,
                            nat = docConfig.nat,
                            grp = docConfig.grp,
                            tip = docConfig.tipo,
                            auxiliar = item.lote!!,
                            partida = partidaNum,
                            producto = item.cve ?: "",
                            cantidad = cantidadTotalPartida.toString()
                        ))
                    }

                    // 3. Omitimos la actualización de existencias locales para Devoluciones 
                    // ya que el producto puede estar defectuoso y no debe considerarse para la venta.
                }

                db.kdm2Dao().insertaPartidas(partidas)
                if (partidasAux.isNotEmpty()) {
                    db.itemAuxDao().insertaPartidasAux(partidasAux)
                }

                Toast.makeText(this@DevolucionesActivity, "Devolución guardada localmente", Toast.LENGTH_SHORT).show()
                
                // Imprimir ticket de devolución
                imprimirTicketDevolucion(kdm1, listaPartidas)

                finish()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@DevolucionesActivity, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun imprimirTicketDevolucion(header: Kdm1Entity, partidas: List<ProductoUI>) {
        val printer = TicketPrinter(this)
        printer.connectAndPrint("Printer001") {
            setAlignCenter()
            setBold(true)
            printText("PRODUCTOS LACTEOS FLORES\n")
            printText("TICKET DE DEVOLUCION\n")
            setBold(false)
            printText("Fecha: ${header.fecha}\n")
            printDivider()

            setAlignLeft()
            printText("Almacen: ${header.alm}\n")
            printText("Tipo Doc: ${header.tip}\n")
            printDivider()

            // Header columnas
            val headerRow = String.format(Locale.US, "%-8s %-15s %5s\n", "Clave", "Producto", "Cant")
            printText(headerRow)
            printDivider()

            for (item in partidas) {
                val line = String.format(Locale.US, "%-8s %-15s %5.1f\n",
                    item.cve?.take(8) ?: "",
                    item.descripcion?.take(15) ?: "",
                    item.cant ?: 0.0
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
                
                if (!item.lote.isNullOrEmpty()) {
                    printText("   Lote: ${item.lote}\n")
                }
            }
            printDivider()

            setAlignCenter()
            printText("\n¡Devolución Procesada!\n")
        }
    }

    private fun mostrarDialogoEdicionTMC(item: ProductoUI, position: Int) {
        lifecycleScope.launch {
            val productoBase = db.productosDao().obtenerProducto(item.cve ?: "") ?: return@launch
            
            val dialogView = LayoutInflater.from(this@DevolucionesActivity).inflate(R.layout.dialog_editar_item, null)
            val etCant: EditText = dialogView.findViewById(R.id.et_cantidad_edit)
            val etPrecio: EditText = dialogView.findViewById(R.id.et_precio_edit)
            val etTalla: EditText = dialogView.findViewById(R.id.et_talla_edit)
            val etModelo: EditText = dialogView.findViewById(R.id.et_modelo_edit)
            val etColor: EditText = dialogView.findViewById(R.id.et_color_edit)

            etPrecio.visibility = View.GONE // Devoluciones no muestran precio
            etCant.setText(item.cant.toString())
            etTalla.setText(item.talla)
            etModelo.setText(item.modelo)
            etColor.setText(item.color)

            // Configurar visibilidad según el campo 'tmc' de la DB
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

            val dialog = AlertDialog.Builder(this@DevolucionesActivity)
                .setTitle("Editar Item")
                .setView(dialogView)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create()

            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nuevaCant = etCant.text.toString().toDoubleOrNull() ?: 0.0
                val tallaText = etTalla.text.toString().uppercase()
                val modeloText = etModelo.text.toString().uppercase()
                val colorText = etColor.text.toString().uppercase()

                // Validaciones TMC
                val tmc = productoBase.tmc
                if (tmc >= "1" && (tallaText.isEmpty() || tallaText == "-")) {
                    Toast.makeText(this@DevolucionesActivity, "La Talla es obligatoria", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (tmc >= "2" && (modeloText.isEmpty() || modeloText == "-")) {
                    Toast.makeText(this@DevolucionesActivity, "El Modelo es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (tmc >= "3" && (colorText.isEmpty() || colorText == "-")) {
                    Toast.makeText(this@DevolucionesActivity, "El Color es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (nuevaCant <= 0) {
                    Toast.makeText(this@DevolucionesActivity, "Cantidad inválida", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Actualizar item
                item.cant = nuevaCant
                item.talla = tallaText.ifEmpty { "-" }
                item.modelo = modeloText.ifEmpty { "-" }
                item.color = colorText.ifEmpty { "-" }
                
                // Si la devolución tenía desglose de lotes (consolidado), ajustamos proporcionalmente o lo que sea necesario
                if (item.desgloseLotes.size == 1) {
                    item.desgloseLotes[0].cantidad = nuevaCant
                    item.desgloseLotes[0].talla = item.talla
                    item.desgloseLotes[0].modelo = item.modelo
                    item.desgloseLotes[0].color = item.color
                }

                hproductsAdapter.notifyItemChanged(position + 1)
                dialog.dismiss()
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
                    AlertDialog.Builder(this@DevolucionesActivity)
                        .setTitle("Seleccione $tipo")
                        .setItems(arrayOpciones) { _, which ->
                            editText.setText(arrayOpciones[which])
                        }
                        .show()
                } else {
                    Toast.makeText(this@DevolucionesActivity, "No hay catálogo disponible para $tipo", Toast.LENGTH_SHORT).show()
                    // Si no hay catálogo, permitir escribir manualmente o dejar como está
                    editText.isFocusableInTouchMode = true
                    editText.requestFocus()
                }
            }
        }
    }
}
