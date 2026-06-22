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
import com.example.lacteos_flores.controllers.CatalogosManager
import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.data.ClientsEntity
import com.example.lacteos_flores.data.DoctosEntity
import com.example.lacteos_flores.data.ItemAuxEntity
import com.example.lacteos_flores.data.Kdm1Entity
import com.example.lacteos_flores.data.Kdm2Entity
import com.example.lacteos_flores.data.PantallasEntity
import com.example.lacteos_flores.data.ProductosEntity
import com.example.lacteos_flores.data.UsuarioDao
import com.example.lacteos_flores.data.ExistenciaEntity
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

class DescargasActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var etAlmacen: TextView
    private lateinit var adapter: OrdenesAdapter
    private lateinit var etFecha: TextView
    private lateinit var etMoneda: EditText
    private lateinit var btnBuscarCliente: Button
    private lateinit var etCodigoCliente: EditText
    private lateinit var etNombreCliente: TextView
    private lateinit var btnBuscarProducto: Button
    private lateinit var btnCargarExistencias: Button
    private lateinit var btnGuardar: Button
    private lateinit var btnFinalizarSync: Button
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
        setContentView(R.layout.activity_descargas)

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
        btnCargarExistencias = findViewById(R.id.btnCargarExistencias)
        btnGuardar = findViewById(R.id.btnAceptar)
        btnFinalizarSync = findViewById(R.id.btnFinalizarSync)
        etProducto = findViewById(R.id.etProducto)
        spTipoDoc = findViewById(R.id.spinnerTipoDoc)
        etSubTotal = findViewById(R.id.etSubTotal)
        etIva = findViewById(R.id.etIva)
        etTotal = findViewById(R.id.etTotal)
        
        // Ocultar campos financieros de la UI principal
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
            // No necesitamos calcular totales monetarios aquí
        }, null)
        recyclerView.adapter = hproductsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        etFecha.setText(fecAct)
        // Campo fecha no editable, solo muestra la actual
        etFecha.setOnClickListener(null)
        
        cargarInfoLocal()


    }

    //funcion para confiurar los listeners de los botones
    private fun setupListeners() {
        etCodigoCliente.setOnClickListener {
            buscarCliente()
        }
        btnBuscarProducto.setOnClickListener {
            buscarProductos()
        }
        btnCargarExistencias.setOnClickListener {
            cargarTodasExistencias()
        }
        btnGuardar.setOnClickListener {
            GuardadDocumentosLocal()
        }
        btnFinalizarSync.setOnClickListener {
            mostrarDialogoFinalizar()
        }
       /* val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                
                AlertDialog.Builder(this@DescargasActivity)
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
        */

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
                    (it.gen == "N" && it.nat == "D" && it.grp=="25" && it.tipo == "18") ||
                    it.descripcion.uppercase().contains("ND2518") ||
                    it.descripcion.uppercase().contains("Descarga")
                }
                
                if (filteredDoctos.isEmpty()) {
                    // Fallback
                    filteredDoctos = doctos.filter { it.gen == "U" }
                }
                
                val descripciones = filteredDoctos.map { it.descripcion }

                val adapterDoctos = ArrayAdapter(this@DescargasActivity, android.R.layout.simple_spinner_item, descripciones)
                adapterDoctos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spTipoDoc.adapter = adapterDoctos
                
                // Bloquear el spinner para que quede fijo
                if (filteredDoctos.isNotEmpty()) {
                    spTipoDoc.setSelection(0)
                    spTipoDoc.isEnabled = false
                }

            }catch (e: Exception){
                println("error:"+e)
                Toast.makeText(this@DescargasActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //funcion para buscar cliente
    private fun buscarCliente() {
        val bottomSheetCliente = BusquedaTecBottonSheet{ cli ->
            selectedClient = cli
            etNombreCliente.setText(cli.nombre)
            etCodigoCliente.setText(cli.clave)
        }
        bottomSheetCliente.show(supportFragmentManager, "BusquedaTecBottomSheet")
    }

    //funcion para reallizar la busqueda de productos
    private fun buscarProductos() {
        val yaAgregados = hproductsAdapter.obtenerLista()
        val bottomSheet = BusquedaRMBottomSheet("1", false, yaAgregados) { resultadoSeleccionado ->
            hproductsAdapter.agregarItem(resultadoSeleccionado)
        }
        bottomSheet.show(supportFragmentManager, "BusquedaRMBottomSheet")
    }

    private fun cargarTodasExistencias() {
        lifecycleScope.launch {
            try {
                val productos = db.existenciasDao().obtenerProductosConStock()
                
                if (productos.isEmpty()) {
                    Toast.makeText(this@DescargasActivity, "No hay productos con existencias", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val productosUI = productos.map {
                    ProductoUI(
                        it.clave,
                        it.existencia,
                        it.unidad,
                        0.0,
                        0.0,
                        it.descripcion
                    )
                }

                hproductsAdapter.actualizarLista(productosUI.toMutableList())
                Toast.makeText(this@DescargasActivity, "Se cargaron ${productos.size} productos con existencia", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this@DescargasActivity, "Error al cargar existencias: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun GuardadDocumentosLocal() {
        if (!tienePermisosBluetooth()) {
            solicitarPermisosBluetooth()
            return
        }

        val cliente = etCodigoCliente.text.toString()
        val listaPartidas = hproductsAdapter.obtenerLista()

        if (listaPartidas.isEmpty()) {
            Toast.makeText(this, "Debe agregar al menos un producto", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val selectedDocPos = spTipoDoc.selectedItemPosition
                if (selectedDocPos < 0 || filteredDoctos.isEmpty()) {
                    Toast.makeText(this@DescargasActivity, "Tipo de documento no válido", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                val userKey = Globales.usuario ?: ""
                val usuarioEntity = db.usuarioDao().obtenerUsuario(userKey)
                if (usuarioEntity == null) {
                    Globales.showToast(this@DescargasActivity, "Error: Usuario no encontrado")
                    return@launch
                }
                
                val docConfig = filteredDoctos[selectedDocPos]
                val fecha = etFecha.text.toString()
                val almacen = etAlmacen.text.toString()

                val kdm1 = Kdm1Entity(
                    suc = usuarioEntity.cve_suc,
                    alm = usuarioEntity.cve_alma,
                    gen = docConfig.gen,
                    nat = docConfig.nat,
                    grp = docConfig.grp,
                    tip = docConfig.tipo,
                    fecha = fecha,
                    cliente = cliente,
                    moneda = "PESOS",
                    pari = "1.0",
                    rfc = selectedClient?.rfc ?: "",
                    venc = fecha,
                    condi = spTipoDoc.selectedItem.toString(),
                    agent = usuarioEntity.usuario ?: "",
                    lati = selectedClient?.latitud ?: "0.0",
                    long = selectedClient?.longitud ?: "0.0",
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
                    var cantidadRestante = item.cant ?: 0.0

                    // 1. Crear Partida Kdm2
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
                        cantidad = cantidadRestante.toString(),
                        descrip = item.descripcion ?: "",
                        unidad = item.uni ?: "",
                        precio = "0.00",
                        importe = "0.00",
                        iva = "0.00"
                    ))

                    // 2. Lógica FIFO para descontar de múltiples lotes
                    val lotesDisponibles = db.existenciasDao().obtenerLotesDisponibles(item.cve ?: "")
                    
                    for (loteEntity in lotesDisponibles) {
                        if (cantidadRestante <= 0) break

                        val stockEnLote = loteEntity.existencias.toDoubleOrNull() ?: 0.0
                        if (stockEnLote <= 0) continue

                        val cantATomar = if (cantidadRestante <= stockEnLote) cantidadRestante else stockEnLote
                        
                        // Registro en ItemAux para este lote
                        partidasAux.add(ItemAuxEntity(
                            iddoc = idDoc,
                            suc = usuarioEntity.cve_suc,
                            alm = usuarioEntity.cve_alma,
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
                            String.format(Locale.US, "%.2f", nuevoStock)
                        )

                        cantidadRestante -= cantATomar
                    }
                    
                    if (cantidadRestante > 0) {
                        Log.w("Descargas", "Atención: El producto ${item.cve} se descargó con saldo negativo en lotes por $cantidadRestante")
                    }
                }

                db.kdm2Dao().insertaPartidas(partidas)
                if (partidasAux.isNotEmpty()) {
                    db.itemAuxDao().insertaPartidasAux(partidasAux)
                }

                Toast.makeText(this@DescargasActivity, "Descarga guardada localmente", Toast.LENGTH_SHORT).show()
                
                // Imprimir ticket de descarga
                imprimirTicketDescarga(kdm1, listaPartidas)
                mostrarDialogoFinalizar()
                finish()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@DescargasActivity, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun imprimirTicketDescarga(header: Kdm1Entity, partidas: List<ProductoUI>) {
        val printer = TicketPrinter(this)
        printer.connectAndPrint("Printer001") {
            setAlignCenter()
            setBold(true)
            printText("PRODUCTOS LACTEOS FLORES\n")
            printText("TICKET DE DESCARGA\n")
            setBold(false)
            printText("Impresion: ${etFecha.text}\n")
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
            }
            printDivider()

            setAlignCenter()
            printText("\n¡Descarga Finalizada!\n")
        }
    }

    private fun mostrarDialogoFinalizar() {
        AlertDialog.Builder(this)
            .setTitle("Finalizar Jornada")
            .setMessage("Esto enviará todos los movimientos pendientes y borrará la información local del día. ¿Desea continuar?")
            .setPositiveButton("Sí, Finalizar") { _, _ ->
                finalizarJornada()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun finalizarJornada() {
        lifecycleScope.launch {
            try {
                val progressDialog = AlertDialog.Builder(this@DescargasActivity)
                    .setTitle("Procesando")
                    .setMessage("Sincronizando y limpiando datos...")
                    .setCancelable(false)
                    .show()

                val manager = CatalogosManager(db)
                val login = Login(usuario.toString(), pass.toString())
                
                val exito = manager.enviarTodoYLimpiar(login)

                progressDialog.dismiss()

                if (exito) {
                    Prefs(this@DescargasActivity).setJornadaActiva(false)
                    Toast.makeText(this@DescargasActivity, "Jornada Finalizada Exitosamente", Toast.LENGTH_LONG).show()
                    
                    // Redirigir al inicio o cerrar
                    val intent = Intent(this@DescargasActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@DescargasActivity, "Error al sincronizar. Verifique su conexión.", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@DescargasActivity, "Error fatal: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
