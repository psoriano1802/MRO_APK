package com.example.lacteos_flores.activitys

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lacteos_flores.R
import com.example.lacteos_flores.adapters.FacturasAdapter
import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.data.CarteraEntity
import com.example.lacteos_flores.data.ClientsEntity
import com.example.lacteos_flores.utils.Globales
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
        val montoTotal = etMontoCobro.text.toString()
        if (clienteSeleccionado == null) {
            Globales.showToast(this, "Seleccione un cliente")
            return
        }
        if (montoTotal.isEmpty() || montoTotal.toDouble() <= 0) {
            Globales.showToast(this, "Ingrese un monto válido")
            return
        }
        
        val seleccionadas = facturasAdapter.getSeleccionadas()
        if (seleccionadas.isEmpty()) {
            Globales.showToast(this, "Seleccione al menos una factura")
            return
        }

        val formaPago = spFormaPago.selectedItem.toString()
        val banco = if (formaPago == "Transferencia") spBanco.selectedItem?.toString() ?: "" else "N/A"

        // Validaciones adicionales
        val totalSeleccionado = seleccionadas.sumOf { it.saldo.toDoubleOrNull() ?: 0.0 }
        if (montoTotal.toDouble() > totalSeleccionado + 0.01) { // Pequeño margen por decimales
             // Opcional: permitir cobros a favor o restringir
             // Globales.showToast(this, "El monto del cobro excede el saldo de las facturas seleccionadas")
        }


        lifecycleScope.launch {
            try {
                // Aquí se llamaría al WS real. 
                // Por ahora simulamos éxito y mostramos resumen
                val mensaje = "Cobro registrado por $$montoTotal vía $formaPago\nCliente: ${clienteSeleccionado?.nombre}"
                android.app.AlertDialog.Builder(this@CobrosActivity)
                    .setTitle("Cobro Exitoso")
                    .setMessage(mensaje)
                    .setPositiveButton("Aceptar") { _, _ -> finish() }
                    .show()
            } catch (e: Exception) {
                Globales.showToast(this@CobrosActivity, "Error al registrar: ${e.message}")
            }
        }
    }
}
