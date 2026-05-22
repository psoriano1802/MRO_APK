package com.example.lacteos_flores.activitys

import android.Manifest
import android.util.Log
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
    private fun dialogInfoCliente(){

    }
    //funcion para validaciones de los clientes limite de credito y dias de credito
    private fun validarCliente(){

    }

    //funcion para reallizar la busqueda de productos
    private fun buscarProductos() {
        val bottomSheet = BusquedaRMBottomSheet("1") { resultadoSeleccionado ->
            val cant = resultadoSeleccionado.cant ?: 0.0
            val impo = (resultadoSeleccionado.costuni ?: 0.0) * cant
            val refaccion = ProductoUI(resultadoSeleccionado.cve, cant, resultadoSeleccionado.uni, resultadoSeleccionado.costuni, impo, resultadoSeleccionado.descripcion)

            hproductsAdapter.agregarItem(refaccion)
            calcularTotales()
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
                val docConfig = filteredDoctos[selectedDocPos]
                val fecha = etFecha.text.toString()
                val almacen = etAlmacen.text.toString()

                // Header (Kdm1)
                val kdm1 = Kdm1Entity(
                    suc = "1",
                    alm = almacen,
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
                    //tomamos el tipo de documento seleccionado
                    condi = spTipoDoc.selectedItem.toString(),
                    agent = usuario ?: "",
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
                        cantidad = cantidadRestante.toString(),
                        descrip = item.descripcion ?: "",
                        unidad = item.uni ?: "",
                        precio = item.costuni.toString(),
                        importe = ((item.cant ?: 0.0) * (item.costuni ?: 0.0)).toString(),
                        iva = ((item.cant ?: 0.0) * (item.costuni ?: 0.0) * 0.16).toString()
                    ))

                    // 2. Lógica FIFO para descontar de múltiples lotes si es necesario
                    val lotesDisponibles = db.existenciasDao().obtenerLotesDisponibles(item.cve ?: "")
                    
                    for (loteEntity in lotesDisponibles) {
                        if (cantidadRestante <= 0) break

                        val stockEnLote = loteEntity.existencias.toDoubleOrNull() ?: 0.0
                        if (stockEnLote <= 0) continue

                        val cantATomar = if (cantidadRestante <= stockEnLote) cantidadRestante else stockEnLote
                        
                        // Registro en ItemAux para este lote
                        partidasAux.add(ItemAuxEntity(
                            iddoc = idDoc,
                            suc = "1",
                            alm = almacen,
                            gen = docConfig.gen,
                            nat = docConfig.nat,
                            grp = docConfig.grp,
                            tip = docConfig.tipo,
                            auxiliar = loteEntity.auxiliar,
                            partida = partidaNum,
                            producto = item.cve ?: "",
                            cantidad = cantATomar.toString()
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

                Toast.makeText(this@VentasActivity, "Documento guardado localmente", Toast.LENGTH_SHORT).show()
                
                // Imprimir ticket después de guardar
                imprimirTicketVenta(kdm1, listaPartidas)

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
                println("item${item.descripcion}")
                val line = String.format(Locale.US, "%-8s %-25s  %-5.1f %-9.2f %-10.2f\n",
                    item.cve?.take(8) ?: "",
                    item.descripcion ?: "",
                    item.cant ?: 0.0,
                    item.costuni ?: 0.0,
                    (item.cant ?: 0.0) * (item.costuni ?: 0.0)
                )
                printText(line)
                // Descripción en la siguiente línea si existe
                /*item.descripcion?.let {
                    if (it.isNotEmpty()) printText("${it.take(32)}\n")
                }*/
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
