package com.example.lacteos_flores.activitys

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lacteos_flores.R
import com.example.lacteos_flores.controllers.CatalogosManager
import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.models.Login
import com.example.lacteos_flores.models.existenciaReques
import com.example.lacteos_flores.utils.Prefs
import kotlinx.coroutines.launch

class SincronizarDatosActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var catalogosManager: CatalogosManager
    private var usuario: String? = null
    private var pass: String? = null

    private lateinit var tvStatus: TextView
    private lateinit var progressBarGlobal: ProgressBar

    // Groups for easier management
    private data class SyncRow(
        val button: Button,
        val progressBar: ProgressBar,
        val resultText: TextView,
        val action: suspend (Login) -> Unit
    )

    private val syncRows = mutableListOf<SyncRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sincronizar_datos)

        initData()
        setupSyncRows()
        initGlobalViews()

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Se requiere conexión a internet para sincronizar datos.", Toast.LENGTH_LONG).show()
            tvStatus.text = "⚠️ Sin conexión a internet"
            tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
        }
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

    private fun initData() {
        db = AppDatabase.getDatabase(this)
        catalogosManager = CatalogosManager(db)
        val creds = Prefs(this).obtenerUsuario()
        usuario = creds.first
        pass = creds.second
    }

    private fun initGlobalViews() {
        tvStatus = findViewById(R.id.tvStatus)
        progressBarGlobal = findViewById(R.id.progressBar)
        findViewById<Button>(R.id.btnSincronizarTodo).setOnClickListener { syncAll() }
        findViewById<Button>(R.id.btnVerHistorial).setOnClickListener {
            val intent = android.content.Intent(this, HistorialDocumentosActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSyncRows() {
        // Documentos
        syncRows.add(SyncRow(
            findViewById(R.id.btnSincronizarDocumentos),
            findViewById(R.id.pbDocumentos),
            findViewById(R.id.tvResDocumentos)
        ) { catalogosManager.sincronizarDocumentos(it) })

        // Bancos
        syncRows.add(SyncRow(
            findViewById(R.id.btnSincronizarBancos),
            findViewById(R.id.pbBancos),
            findViewById(R.id.tvResBancos)
        ) { catalogosManager.sincronizarBancos(it) })

        // Clientes
        syncRows.add(SyncRow(
            findViewById(R.id.btnSincronizarClientes),
            findViewById(R.id.pbClientes),
            findViewById(R.id.tvResClientes)
        ) { 
            val pendingCobros = db.kdm1Dao().contarCobrosPendientes()
            if (pendingCobros > 0) {
                throw Exception("Hay $pendingCobros cobros pendientes. Sincronízalos primero.")
            }
            catalogosManager.sincronizarCartera(it)
            catalogosManager.sincronizarClientes(it)
        })

        // Cartera
        findViewById<Button>(R.id.btnSincronizarCartera).visibility = View.GONE
        findViewById<ProgressBar>(R.id.pbCartera).visibility = View.GONE
        findViewById<TextView>(R.id.tvResCartera).visibility = View.GONE

        // Gastos
        syncRows.add(SyncRow(
            findViewById(R.id.btnSincronizarGastos),
            findViewById(R.id.pbGastos),
            findViewById(R.id.tvResGastos)
        ) { catalogosManager.sincronizarGastos(it) })

        // Productos
        syncRows.add(SyncRow(
            findViewById(R.id.btnSincronizarProductos),
            findViewById(R.id.pbProductos),
            findViewById(R.id.tvResProductos)
        ) { login ->
            val userEntity = db.usuarioDao().obtenerUsuario(usuario.toString())
            catalogosManager.sincronizarProductos(login, userEntity?.lista ?: "")
            catalogosManager.sincronizarListaPrecios(login)
        })

        // Listas Precio
        findViewById<Button>(R.id.btnSincronizarListasPrecio).visibility = View.GONE
        findViewById<ProgressBar>(R.id.pbListasPrecio).visibility = View.GONE
        findViewById<TextView>(R.id.tvResListasPrecio).visibility = View.GONE

        // Existencia
        syncRows.add(SyncRow(
            findViewById(R.id.btnSincronizarExistencia),
            findViewById(R.id.pbExistencia),
            findViewById(R.id.tvResExistencia)
        ) { catalogosManager.sincronizarNuevaExistencia(it) })

        // Cobros
        syncRows.add(SyncRow(
            findViewById(R.id.btnSincronizarCobros),
            findViewById(R.id.pbCobros),
            findViewById(R.id.tvResCobros)
        ) { catalogosManager.enviarCobrosPendientes(it) })

        // Movimientos
        syncRows.add(SyncRow(
            findViewById(R.id.btnSincronizarMovimientos),
            findViewById(R.id.pbMovimientos),
            findViewById(R.id.tvResMovimientos)
        ) { login ->
            catalogosManager.enviarVentasPendientes(login)
            catalogosManager.enviarGastosPendientes(login)

        })

        // Set click listeners for individual buttons
        syncRows.forEach { row ->
            row.button.setOnClickListener {
                lifecycleScope.launch {
                    executeSyncRow(row)
                }
            }
        }
    }

    private suspend fun executeSyncRow(row: SyncRow) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Sin conexión a internet", Toast.LENGTH_SHORT).show()
            return
        }
        val login = Login(usuario.toString(), pass.toString())
        try {
            setRowLoading(row, true)
            row.action(login)
            setRowResult(row, true)
        } catch (e: Exception) {
            setRowResult(row, false)
            Toast.makeText(this, "${row.button.text}: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setRowLoading(row: SyncRow, isLoading: Boolean) {
        row.button.isEnabled = !isLoading
        row.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) row.resultText.visibility = View.GONE
    }

    private fun setRowResult(row: SyncRow, isSuccess: Boolean) {
        row.progressBar.visibility = View.GONE
        row.resultText.visibility = View.VISIBLE
        row.button.isEnabled = true
        if (isSuccess) {
            row.resultText.text = "✅"
            row.resultText.setTextColor(getColor(android.R.color.holo_green_dark))
        } else {
            row.resultText.text = "❌"
            row.resultText.setTextColor(getColor(android.R.color.holo_red_dark))
        }
    }

    private fun syncAll() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Sin conexión a internet", Toast.LENGTH_SHORT).show()
            return
        }
        val login = Login(usuario.toString(), pass.toString())
        lifecycleScope.launch {
            try {
                toggleAllButtons(false)
                progressBarGlobal.visibility = View.VISIBLE
                tvStatus.text = "Sincronizando todo..."
                tvStatus.setTextColor(getColor(android.R.color.black))

                for (row in syncRows) {
                    tvStatus.text = "Sincronizando ${row.button.text}..."
                    executeSyncRow(row)
                }

                tvStatus.text = "Sincronización completa"
                Toast.makeText(this@SincronizarDatosActivity, "Sincronización completa", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                tvStatus.text = "Error en la sincronización global"
            } finally {
                toggleAllButtons(true)
                progressBarGlobal.visibility = View.GONE
            }
        }
    }

    private fun toggleAllButtons(enabled: Boolean) {
        syncRows.forEach { it.button.isEnabled = enabled }
        findViewById<Button>(R.id.btnSincronizarTodo).isEnabled = enabled
    }
}
