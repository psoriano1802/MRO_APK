package com.example.lacteos_flores.activitys

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.*
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lacteos_flores.R
import com.example.lacteos_flores.adapters.FacturasAdapter
import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.data.CarteraEntity
import com.example.lacteos_flores.data.ClientsEntity
import com.example.lacteos_flores.data.Kdm1Entity
import com.example.lacteos_flores.data.Kdm2cxcEntity
import com.example.lacteos_flores.utils.Globales
import com.example.lacteos_flores.utils.TicketPrinter
import kotlinx.coroutines.launch
import java.util.*

class CobrosActivity : AppCompatActivity() {

    private lateinit var tvFechaCobro: TextView
    private lateinit var etBuscarCliente: EditText
    private lateinit var btnBuscarCliente: ImageButton
    private lateinit var tvClienteNombre: TextView
    
    private lateinit var spFormaPago: Spinner
    private lateinit var tvLabelBanco: TextView
    private lateinit var spBanco: Spinner
    private lateinit var etMontoCobro: EditText
    
    private lateinit var cbSeleccionarTodo: CheckBox
    private lateinit var rvFacturas: RecyclerView
    private lateinit var tvTotalSeleccionado: TextView
    private lateinit var btnAplicarCobroAuto: Button
    private lateinit var btnRegistrarCobro: Button

    private lateinit var facturasAdapter: FacturasAdapter
    private var clienteSeleccionado: ClientsEntity? = null
    private var listaFacturasOriginal: List<CarteraEntity> = emptyList()
    private val db by lazy { AppDatabase.getDatabase(this) }

    // Launcher para permisos de Bluetooth
    private val requestBluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            Toast.makeText(this, "Permisos concedidos. Intente de nuevo.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Se requieren permisos de Bluetooth para imprimir.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cobros)

        inicializarComponentes()
        setupListeners()
        setupSpinners()
        actualizarFecha()
    }

    private fun inicializarComponentes() {
        tvFechaCobro = findViewById(R.id.tv_fecha_cobro)
        etBuscarCliente = findViewById(R.id.et_buscar_cliente)
        btnBuscarCliente = findViewById(R.id.btn_buscar_cliente)
        tvClienteNombre = findViewById(R.id.tv_cliente_nombre)
        
        spFormaPago = findViewById(R.id.sp_forma_pago)
        tvLabelBanco = findViewById(R.id.tv_label_banco)
        spBanco = findViewById(R.id.sp_banco)
        etMontoCobro = findViewById(R.id.et_monto_cobro)
        
        cbSeleccionarTodo = findViewById(R.id.cb_seleccionar_todo)
        rvFacturas = findViewById(R.id.rv_facturas)
        tvTotalSeleccionado = findViewById(R.id.tv_total_seleccionado)
        btnAplicarCobroAuto = findViewById(R.id.btn_aplicar_cobro_auto)
        btnRegistrarCobro = findViewById(R.id.btn_registrar_cobro)

        rvFacturas.layoutManager = LinearLayoutManager(this)
        facturasAdapter = FacturasAdapter(emptyList()) { seleccionadas ->
            actualizarTotalSeleccionado(seleccionadas)
        }
        rvFacturas.adapter = facturasAdapter
    }

    private fun setupListeners() {
        btnBuscarCliente.setOnClickListener {
            val query = etBuscarCliente.text.toString()
            if (query.isNotEmpty()) {
                buscarClienteLocal(query)
            } else {
                Globales.showToast(this, "Ingrese un nombre o clave")
            }
        }

        spFormaPago.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val forma = parent?.getItemAtPosition(position).toString()
                if (forma == "Transferencia") {
                    tvLabelBanco.visibility = View.VISIBLE
                    spBanco.visibility = View.VISIBLE
                } else {
                    tvLabelBanco.visibility = View.GONE
                    spBanco.visibility = View.GONE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        cbSeleccionarTodo.setOnCheckedChangeListener { _, isChecked ->
            facturasAdapter.seleccionarTodo(isChecked)
        }

        btnAplicarCobroAuto.setOnClickListener {
            aplicarCobroAutomatico()
        }

        btnRegistrarCobro.setOnClickListener {
            registrarCobro()
        }
    }

    private fun setupSpinners() {
        val formasPago = arrayOf("Efectivo", "Transferencia")
        val adapterForma = ArrayAdapter(this, android.R.layout.simple_spinner_item, formasPago)
        adapterForma.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFormaPago.adapter = adapterForma

        lifecycleScope.launch {
            val bancos = db.bancoDao().obtenerBancos()
            val listaBancos = bancos.map { "${it.clave} - ${it.banco}" }
            val adapterBanco = ArrayAdapter(this@CobrosActivity, android.R.layout.simple_spinner_item, listaBancos)
            adapterBanco.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spBanco.adapter = adapterBanco
        }
    }

    private fun actualizarFecha() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        tvFechaCobro.text = sdf.format(Date())
    }

    private fun buscarClienteLocal(query: String) {
        lifecycleScope.launch {
            val clientes = db.clientsDao().obtenerTodosClientes(query)
            if (clientes.isNotEmpty()) {
                if (clientes.size == 1) {
                    seleccionarCliente(clientes[0])
                } else {
                    // Mostrar diálogo de selección si hay múltiples
                    mostrarDialogoSeleccionCliente(clientes)
                }
            } else {
                Globales.showToast(this@CobrosActivity, "Cliente no encontrado")
            }
        }
    }

    private fun mostrarDialogoSeleccionCliente(clientes: List<ClientsEntity>) {
        val nombres = clientes.map { "${it.clave} - ${it.nombre}" }.toTypedArray()
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Seleccione un cliente")
        builder.setItems(nombres) { _, which ->
            seleccionarCliente(clientes[which])
        }
        builder.show()
    }

    private fun seleccionarCliente(cliente: ClientsEntity) {
        clienteSeleccionado = cliente
        tvClienteNombre.text = "${cliente.clave} - ${cliente.nombre}"
        cargarFacturasPendientes(cliente.clave)
    }


    // ... dentro de cargarFacturasPendientes
    private fun cargarFacturasPendientes(clienteCve: String) {
        lifecycleScope.launch {
            val facturas = db.carteraDao().obtenerMCarteras(clienteCve)
            // Guardamos la lista original para tener los saldos reales siempre a la mano
            listaFacturasOriginal = facturas.sortedBy { it.fecha }

            // Inicializamos con abono 0
            val facturasIniciales = listaFacturasOriginal.map { it.copy(abono = "0.00") }
            facturasAdapter.actualizarLista(facturasIniciales)
        }
    }


    private fun actualizarTotalSeleccionado(seleccionadas: List<CarteraEntity>) {
        val total = seleccionadas.sumOf { it.saldo.toDoubleOrNull() ?: 0.0 }
        tvTotalSeleccionado.text = String.format("$%.2f", total)
    }

    // ... Implementación de la nueva lógica de Aplicar Cobro
    private fun aplicarCobroAutomatico() {
        val montoStr = etMontoCobro.text.toString()
        if (montoStr.isEmpty()) {
            Globales.showToast(this, "Ingrese el monto del cobro")
            return
        }

        var montoRestante = montoStr.toDoubleOrNull() ?: 0.0
        if (montoRestante <= 0) {
            Globales.showToast(this, "El monto debe ser mayor a 0")
            return
        }

        if (listaFacturasOriginal.isEmpty()) {
            Globales.showToast(this, "No hay facturas para aplicar el cobro")
            return
        }

        val nuevasFacturas = mutableListOf<CarteraEntity>()
        val seleccionadasAuto = mutableListOf<CarteraEntity>()

        // Iteramos sobre los saldos originales para aplicar el cobro (FIFO)
        for (facturaOriginal in listaFacturasOriginal) {
            val saldoReal = facturaOriginal.saldo.toDoubleOrNull() ?: 0.0

            if (montoRestante > 0) {
                // Calculamos cuánto podemos abonar a esta factura
                val abono = if (montoRestante >= saldoReal) saldoReal else montoRestante
                val nuevoSaldo = saldoReal - abono
                montoRestante -= abono

                // Creamos una copia con el nuevo saldo y el abono aplicado
                val facturaModificada = facturaOriginal.copy(
                    saldo = String.format("%.2f", nuevoSaldo),
                    abono = String.format("%.2f", abono)
                )
                nuevasFacturas.add(facturaModificada)
                seleccionadasAuto.add(facturaModificada)
            } else {
                // Facturas a las que ya no les alcanzó el cobro
                nuevasFacturas.add(facturaOriginal.copy(abono = "0.00"))
            }
        }

        // Actualizamos la UI con los nuevos saldos calculados
        facturasAdapter.actualizarLista(nuevasFacturas)
        facturasAdapter.setSeleccionadas(seleccionadasAuto)

        // El total seleccionado ahora refleja el monto total que se distribuyó
        val totalDistribuido = montoStr.toDouble() - montoRestante
        tvTotalSeleccionado.text = String.format("$%.2f", totalDistribuido)

        Globales.showToast(this, "Cobro distribuido: $${String.format("%.2f", totalDistribuido)}")
    }

    private fun registrarCobro() {
        // Verificar permisos antes de registrar para poder imprimir el ticket al finalizar
        if (!tienePermisosBluetooth()) {
            solicitarPermisosBluetooth()
            return
        }

        val montoTotalStr = etMontoCobro.text.toString()
        if (clienteSeleccionado == null) {
            Globales.showToast(this, "Seleccione un cliente")
            return
        }
        if (montoTotalStr.isEmpty() || montoTotalStr.toDouble() <= 0) {
            Globales.showToast(this, "Ingrese un monto válido")
            return
        }
        
        val seleccionadas = facturasAdapter.getSeleccionadas()
        if (seleccionadas.isEmpty()) {
            Globales.showToast(this, "Seleccione al menos una factura")
            return
        }

        val formaPago = spFormaPago.selectedItem.toString()
        val bancoSeleccionado = if (formaPago == "Transferencia") spBanco.selectedItem?.toString()?.split(" - ")?.first() ?: "" else "N/A"

        lifecycleScope.launch {
            try {
                // 1. Obtener datos del usuario
                val userKey = Globales.usuario ?: ""
                val usuario = db.usuarioDao().obtenerUsuario(userKey)
                if (usuario == null) {
                    Globales.showToast(this@CobrosActivity, "Error: Usuario no encontrado")
                    return@launch
                }

                // 2. Obtener documento (UA51 o UA52)
                val docGen = if (formaPago == "Transferencia") "UA51" else "UA52"
                val doctoConfig = db.doctosDao().obtenerDocumentoPorGen(docGen)
                if (doctoConfig == null) {
                    Globales.showToast(this@CobrosActivity, "Configuración $docGen no encontrada")
                    return@launch
                }

                // 3. Preparar Encabezado (Kdm1)
                val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val kdm1 = Kdm1Entity(
                    suc = "1",
                    alm = usuario.cve_alma,
                    gen = doctoConfig.gen,
                    nat = doctoConfig.nat,
                    grp = doctoConfig.grp,
                    tip = doctoConfig.tipo,
                    fecha = fechaActual,
                    cliente = clienteSeleccionado!!.clave,
                    moneda = "PESOS",
                    pari = "1",
                    rfc = clienteSeleccionado!!.rfc,
                    venc = fechaActual,
                    condi = "",
                    agent = usuario.usuario,
                    lati = "0.0",
                    long = "0.0",
                    subtotal = montoTotalStr,
                    iva = "0.00",
                    monto = montoTotalStr,
                    porAsignar = "0.00",
                    banco = bancoSeleccionado,
                    staSinc = "N"
                )

                val idKdm1 = db.kdm1Dao().insertaDocumento(kdm1)

                // 4. Preparar Partidas (Kdm2cxc)
                val partidas = seleccionadas.filter { (it.abono.toDoubleOrNull() ?: 0.0) > 0 }.map { factura ->
                    // Calculamos el saldo anterior (restituimos el abono al saldo que muestra el adapter si este fue modificado)
                    val abonoVal = factura.abono.toDoubleOrNull() ?: 0.0
                    val saldoActualVal = factura.saldo.toDoubleOrNull() ?: 0.0
                    val saldoAnterior = String.format("%.2f", saldoActualVal + abonoVal)

                    Kdm2cxcEntity(
                        iddoc = idKdm1,
                        doctoAfectado = factura.docto,
                        saldoAnt = saldoAnterior,
                        abono = factura.abono,
                        fecha = fechaActual,
                        descri = "PAGO DE FACTURA ${factura.docto}",
                        moneda = "PESOS",
                        montoDocto = factura.monto,
                        pari = "1",
                        referencia = "" 
                    )
                }
                
                db.kdm2cxcDao().insertarPartidas(partidas)

                // 5. Actualizar saldos en la tabla Cartera local
                for (factura in seleccionadas) {
                    val saldoActualVal = factura.saldo.toDoubleOrNull() ?: 0.0
                    val nuevoSaldoStr = String.format("%.2f", saldoActualVal)
                    
                    db.carteraDao().actualizarSaldo(
                        clienteSeleccionado!!.clave,
                        factura.docto,
                        nuevoSaldoStr
                    )
                }

                // 6. Imprimir ticket de cobro
                imprimirTicketCobro(clienteSeleccionado!!, montoTotalStr, formaPago, seleccionadas)

                val mensaje = "Cobro registrado por $$montoTotalStr vía $formaPago\nCliente: ${clienteSeleccionado?.nombre}"
                android.app.AlertDialog.Builder(this@CobrosActivity)
                    .setTitle("Cobro Exitoso")
                    .setMessage(mensaje)
                    .setPositiveButton("Aceptar") { _, _ -> finish() }
                    .show()

            } catch (e: Exception) {
                Globales.showToast(this@CobrosActivity, "Error al registrar: ${e.message}")
                e.printStackTrace()
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

    private fun imprimirTicketCobro(cliente: ClientsEntity, monto: String, formaPago: String, facturas: List<CarteraEntity>) {
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
            printText("RECIBO DE PAGO\n")
            printDivider()

            setAlignLeft()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            printText("Fecha: ${sdf.format(Date())}\n")
            printText("Cliente: ${cliente.clave}\n")
            printText("Nombre: ${cliente.nombre}\n")
            printDivider()

            // Docto. | Saldo Ant. | Abono
            val rowHeader = String.format(Locale.US, "%-10s %10s %10s\n", "DOCTO", "SALDO", "ABONO")
            printText(rowHeader)
            printDivider()

            for (f in facturas) {
                // Usamos f.docto que es el numero de factura
                val line = String.format(Locale.US, "%-10s %10s %10s\n",
                    f.docto.take(10),
                    f.saldo,
                    f.abono
                )
                printText(line)
            }
            printDivider()

            setAlignRight()
            setBold(true)
            printText("TOTAL RECIBIDO: $ $monto\n")
            setBold(false)
            printText("FORMA DE PAGO: $formaPago\n")

            setAlignCenter()
            printText("\n¡Gracias por su pago!\n")
        }
    }
}
