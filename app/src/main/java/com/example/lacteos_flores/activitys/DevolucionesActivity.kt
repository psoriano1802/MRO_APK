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

    }

    private fun Inicializa(){
        val fecAct = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        usuario = Prefs(this).obtenerUsuario().first.toString()
        pass = Prefs(this).obtenerUsuario().second.toString()

        //inicializamos la base de datos
        db = AppDatabase.getDatabase(this)
        loginUserDao = db.usuarioDao()
        //inicializamos el adapter con callback para recalcular totales automáticamente al editar/eliminar
        hproductsAdapter = RefaccionesAdapter(mutableListOf(), listOf("Clave", "Cant", "Uni", "Precio", "Importe")) {
            calcularTotales()
        }
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
        /*etCodigoCliente.setOnClickListener {
            // Aquí puedes implementar la lógica para guardar los datos
            buscarCliente()
        }*/
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
                
                //buscamos los documentos disponibles para ponerlo en el spinnerTipoDoc y mostrando las descripciones
                val doctos = db.doctosDao().obtenerDocumentos()
                //Filtramos por el tipo de documento a trabajar en la pantalla
                filteredDoctos = doctos.filter { it.gen == "U" && it.nat == "E" && it.grp == "25"}
                if(filteredDoctos.isEmpty()){
                    filteredDoctos = doctos.filter { it.gen == "U" } // Fallback
                }
                
                val descripciones = filteredDoctos.map { it.descripcion }

                val adapterDoctos = ArrayAdapter(this@DevolucionesActivity, android.R.layout.simple_spinner_item, descripciones)
                adapterDoctos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spTipoDoc.adapter = adapterDoctos

            } catch (e: Exception){
                println("error:"+e)
                Toast.makeText(this@DevolucionesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //funcion para buscar cliente abriendo el bottom sheet de clientes y haciendo la busqieda en la tabla clientes local
    private fun buscarCliente() {
        /*val bottomSheetCliente = BusquedaTecBottonSheet{ cli ->
            selectedClient = cli
            etNombreCliente.setText(cli.nombre)
            etCodigoCliente.setText(cli.clave)
        }
        bottomSheetCliente.show(supportFragmentManager, "BusquedaTecBottomSheet")*/
    }

    //funcion para reallizar la busqueda de productos
    private fun buscarProductos() {
        val bottomSheet = BusquedaRMBottomSheet("1", esDevolucion = true) { resultadoSeleccionado ->
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_devolucion_item, null)
            val tvInfo: TextView = dialogView.findViewById(R.id.tv_producto_info)
            val etCant: EditText = dialogView.findViewById(R.id.et_cantidad_dev)
            val etLote: EditText = dialogView.findViewById(R.id.et_lote_dev)
            val etPrecio: EditText = dialogView.findViewById(R.id.et_precio_dev)

            tvInfo.text = "${resultadoSeleccionado.cve} - ${resultadoSeleccionado.descripcion}"
            etCant.setText(resultadoSeleccionado.cant?.toString() ?: "1.0")
            etPrecio.setText(resultadoSeleccionado.costuni?.toString() ?: "0.0")

            AlertDialog.Builder(this)
                .setTitle("Datos de Devolución")
                .setView(dialogView)
                .setPositiveButton("Agregar") { _, _ ->
                    val cant = etCant.text.toString().toDoubleOrNull() ?: 0.0
                    val lote = etLote.text.toString()
                    val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
                    
                    if (lote.isEmpty()) {
                        Toast.makeText(this, "El lote es requerido", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val impo = precio * cant
                    val refaccion = ProductoUI(
                        resultadoSeleccionado.cve, 
                        cant, 
                        resultadoSeleccionado.uni, 
                        precio, 
                        impo, 
                        resultadoSeleccionado.descripcion,
                        lote = lote
                    )

                    hproductsAdapter.agregarItem(refaccion)
                    calcularTotales()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        bottomSheet.show(supportFragmentManager, "BusquedaRMBottomSheet")
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
        for (item in lista) {
            val importe = (item.cant ?: 0.0) * (item.costuni ?: 0.0)
            subtotal += importe
            totalIva += importe * 0.16 // IVA 16%
        }
        val total = subtotal + totalIva

        etSubTotal.setText(String.format(Locale.US, "%.2f", subtotal))
        etIva.setText(String.format(Locale.US, "%.2f", totalIva))
        etTotal.setText(String.format(Locale.US, "%.2f", total))
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
                    suc = "1",
                    alm = usuarioEntity.cve_alma,
                    gen = docConfig.gen,
                    nat = docConfig.nat,
                    grp = docConfig.grp,
                    tip = docConfig.tipo,
                    fecha = fecha,
                    cliente = "", // No requerido según instrucciones de ocultar cliente
                    moneda = "PESOS",
                    pari = "1.0",
                    rfc = "",
                    venc = fecha,
                    condi = spTipoDoc.selectedItem.toString(),
                    agent = usuarioEntity.usuario,
                    lati = "0.0",
                    long = "0.0",
                    subtotal = etSubTotal.text.toString(),
                    iva = etIva.text.toString(),
                    monto = etTotal.text.toString(),
                    staSinc = "N"
                )

                val idDoc = db.kdm1Dao().insertaDocumento(kdm1)

                val partidas = mutableListOf<Kdm2Entity>()
                val partidasAux = mutableListOf<ItemAuxEntity>()

                listaPartidas.forEachIndexed { index, item ->
                    val partidaNum = (index + 1).toString()
                    val cantidad = item.cant ?: 0.0
                    val lote = item.lote ?: ""

                    // 1. Crear Partida Kdm2
                    partidas.add(Kdm2Entity(
                        iddoc = idDoc,
                        suc = "1",
                        alm = almacen,
                        gen = docConfig.gen,
                        nat = docConfig.nat,
                        grp = docConfig.grp,
                        tip = docConfig.tipo,
                        partida = partidaNum,
                        producto = item.cve ?: "",
                        cantidad = cantidad.toString(),
                        descrip = item.descripcion ?: "",
                        unidad = item.uni ?: "",
                        precio = item.costuni.toString(),
                        importe = item.importe.toString(),
                        iva = (cantidad * (item.costuni ?: 0.0) * 0.16).toString()
                    ))

                    // 2. Registro en ItemAux para la entrada (lote)
                    partidasAux.add(ItemAuxEntity(
                        iddoc = idDoc,
                        suc = "1",
                        alm = almacen,
                        gen = docConfig.gen,
                        nat = docConfig.nat,
                        grp = docConfig.grp,
                        tip = docConfig.tipo,
                        auxiliar = lote,
                        partida = partidaNum,
                        producto = item.cve ?: "",
                        cantidad = cantidad.toString()
                    ))

                    // 3. Actualización o creación de Existencias en la base de datos local
                    val loteExistente = db.existenciasDao().obtenerLoteEspecifico(item.cve ?: "", lote)
                    if (loteExistente != null) {
                        val stockActual = loteExistente.existencias.toDoubleOrNull() ?: 0.0
                        val nuevoStock = stockActual + cantidad
                        db.existenciasDao().actualizarExistencia(
                            item.cve ?: "",
                            lote,
                            String.format(Locale.US, "%.2f", nuevoStock)
                        )
                    } else {
                        // Crear nuevo lote
                        db.existenciasDao().insertarExistencias(listOf(
                            ExistenciaEntity(
                                clave = item.cve ?: "",
                                auxiliar = lote,
                                existencias = String.format(Locale.US, "%.2f", cantidad),
                                fecha = fecha
                            )
                        ))
                    }
                }

                db.kdm2Dao().insertaPartidas(partidas)
                if (partidasAux.isNotEmpty()) {
                    db.itemAuxDao().insertaPartidasAux(partidasAux)
                }

                Toast.makeText(this@DevolucionesActivity, "Devolución guardada localmente", Toast.LENGTH_SHORT).show()
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
}
