package com.example.lacteos_flores.activitys

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lacteos_flores.R
import com.example.lacteos_flores.adapters.HistorialDocumentosAdapter
import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.data.ClientsEntity
import com.example.lacteos_flores.data.Kdm1Entity
import com.example.lacteos_flores.data.Kdm2Entity
import com.example.lacteos_flores.data.Kdm2cxcEntity
import com.example.lacteos_flores.models.DocumentoHistorial
import com.example.lacteos_flores.utils.TicketPrinter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistorialDocumentosActivity : AppCompatActivity() {

    private lateinit var rvHistorial: RecyclerView
    private lateinit var adapter: HistorialDocumentosAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_documentos)

        db = AppDatabase.getDatabase(this)
        rvHistorial = findViewById(R.id.rvHistorial)
        rvHistorial.layoutManager = LinearLayoutManager(this)
        
        adapter = HistorialDocumentosAdapter(emptyList()) { item ->
            confirmarReimpresion(item)
        }
        rvHistorial.adapter = adapter

        cargarHistorial()
    }

    private fun cargarHistorial() {
        lifecycleScope.launch {
            val historial = db.kdm1Dao().obtenerHistorialDocumentos()
            adapter.updateList(historial)
        }
    }

    private fun confirmarReimpresion(item: DocumentoHistorial) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Reimpresión")
            .setMessage("¿Desea reimprimir el ticket del documento ${item.gen}${item.nat}${item.grp}${item.tip}?")
            .setPositiveButton("Sí") { _, _ ->
                reimprimirTicket(item)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun reimprimirTicket(item: DocumentoHistorial) {
        lifecycleScope.launch {
            val header = db.kdm1Dao().obtenerDocumentoPorId(item.id)
            if (header == null) {
                Toast.makeText(this@HistorialDocumentosActivity, "No se encontró el encabezado del documento", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (header.gen == "U" && header.nat == "A" && header.grp == "5") {
                // Es un cobro
                val partidas = db.kdm2cxcDao().obtenerPartidasPorDoc(item.id)
                val cliente = db.clientsDao().obtenerCliente(header.cliente)
                if (cliente != null) {
                    imprimirTicketCobro(header, cliente, partidas)
                } else {
                    Toast.makeText(this@HistorialDocumentosActivity, "No se encontró la información del cliente", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Es una venta/devolución/descarga
                val partidas = db.kdm2Dao().obtenerPartidas(item.id)
                imprimirTicketVenta(header, partidas, item.descripcion ?: "")
            }
        }
    }

    private fun imprimirTicketVenta(header: Kdm1Entity, partidas: List<Kdm2Entity>, descripcionDoc: String) {
        val printer = TicketPrinter(this)
        printer.connectAndPrint("Printer001") {
            setAlignCenter()
            setBold(true)
            printText("PRODUCTOS LACTEOS FLORES\n")
            setBold(false)
            printText("R.F.C.: PLF010228TC3\n")
            printText("NICOLAS BRAVO, CENTRO, JIQUILPAN\n")
            printText("Tel: 3535330998\n\n")
            
            printText("REIMPRESION: $descripcionDoc\n")
            printText("Fecha: ${header.fecha}\n")
            printDivider()

            setAlignLeft()
            printText("Cliente: ${header.cliente}\n")
            printDivider()

            val headerRow = String.format(Locale.US, "%-8s %-15s %5s %8s\n", "Clave", "Cant", "Precio", "Total")
            printText(headerRow)
            printDivider()

            for (item in partidas) {
                val subtotal = (item.cantidad.toDoubleOrNull() ?: 0.0) * (item.precio.toDoubleOrNull() ?: 0.0)
                printText("${item.producto.take(32)}\n")
                val line = String.format(Locale.US, "%-8s %15.1f %8.2f %8.2f\n",
                    item.producto.take(8),
                    item.cantidad.toDoubleOrNull() ?: 0.0,
                    item.precio.toDoubleOrNull() ?: 0.0,
                    subtotal
                )
                printText(line)
            }
            printDivider()

            setAlignRight()
            printText("Subtotal: $ ${header.subtotal}\n")
            printText("Impuesto: $ ${header.iva}\n")
            setBold(true)
            printText("TOTAL: $ ${header.monto}\n")
            setBold(false)

            setAlignCenter()
            printText("\n--- REIMPRESION ---\n")
        }
    }

    private fun imprimirTicketCobro(header: Kdm1Entity, cliente: ClientsEntity, facturas: List<Kdm2cxcEntity>) {
        val printer = TicketPrinter(this)
        printer.connectAndPrint("Printer001") {
            setAlignCenter()
            setBold(true)
            printText("PRODUCTOS LACTEOS FLORES\n")
            setBold(false)
            printText("R.F.C.: PLF010228TC3\n")
            printText("NICOLAS BRAVO, CENTRO, JIQUILPAN\n\n")
            
            printText("REIMPRESION: RECIBO DE PAGO\n")
            printDivider()

            setAlignLeft()
            printText("Fecha Doc: ${header.fecha}\n")
            printText("Cliente: ${cliente.clave}\n")
            printText("${cliente.nombre}\n")
            printDivider()

            val rowHeader = String.format(Locale.US, "%-14s %10s %10s\n", "DOCTO", "SALDO", "ABONO")
            printText(rowHeader)
            printDivider()

            for (f in facturas) {
                val line = String.format(Locale.US, "%-14s %10s %10s\n",
                    f.doctoAfectado.take(14),
                    f.saldoAnt,
                    f.abono
                )
                printText(line)
            }
            printDivider()

            setAlignRight()
            setBold(true)
            printText("TOTAL RECIBIDO: $ ${header.monto}\n")
            setBold(false)
            printText("Banco: ${header.banco}\n")

            setAlignCenter()
            printText("\n--- REIMPRESION ---\n")
        }
    }
}
