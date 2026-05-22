package com.example.lacteos_flores.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.io.OutputStream
import java.util.*

class TicketPrinter(private val context: Context) {

    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    // UUID estándar para dispositivos SPP (Serial Port Profile)
    private val PRINTER_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // ESC/POS Commands
    private val RESET_PRINTER = byteArrayOf(0x1B, 0x40)
    private val ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
    private val ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
    private val ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 0x02)
    private val BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
    private val BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
    private val FONT_LARGE = byteArrayOf(0x1D, 0x21, 0x11) // Double height and width
    private val FONT_NORMAL = byteArrayOf(0x1D, 0x21, 0x00)

    @SuppressLint("MissingPermission")
    fun connectAndPrint(printerName: String, printBlock: TicketPrinter.() -> Unit) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Toast.makeText(context, "Bluetooth no habilitado", Toast.LENGTH_SHORT).show()
            return
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        Log.d("TicketPrinter", "Dispositivos vinculados: ${pairedDevices?.map { it.name }}")
        val device = pairedDevices?.find { it.name?.contains(printerName, ignoreCase = true) == true }

        if (device == null) {
            Log.e("TicketPrinter", "No se encontró la impresora con nombre que contenga: $printerName")
            (context as? android.app.Activity)?.runOnUiThread {
                Toast.makeText(context, "Impresora $printerName no encontrada en dispositivos vinculados", Toast.LENGTH_LONG).show()
            }
            return
        }

        Log.d("TicketPrinter", "Conectando a: ${device.name} (${device.address})")
        Thread {
            try {
                // Intento 1: Conexión segura
                bluetoothSocket = try {
                    device.createRfcommSocketToServiceRecord(PRINTER_UUID)
                } catch (e: Exception) {
                    null
                }
                
                try {
                    bluetoothSocket?.connect()
                } catch (e: IOException) {
                    Log.w("TicketPrinter", "Fallo conexión segura, intentando insegura...")
                    // Intento 2: Conexión insegura (común en impresoras genéricas)
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(PRINTER_UUID)
                    bluetoothSocket?.connect()
                }

                outputStream = bluetoothSocket?.outputStream

                if (outputStream != null) {
                    sendData(RESET_PRINTER)
                    printBlock()
                    // Espacio final para poder arrancar el ticket
                    printText("\n\n\n\n")
                    outputStream?.flush()
                    Thread.sleep(800) // Un poco más de tiempo para el buffer
                }

            } catch (e: Exception) {
                Log.e("TicketPrinter", "Error fatal en la impresión", e)
                val errorMsg = e.message ?: "Error desconocido"
                (context as? android.app.Activity)?.runOnUiThread {
                    Toast.makeText(context, "Error de impresión: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } finally {
                closeConnection()
            }
        }.start()
    }

    private fun closeConnection() {
        try {
            outputStream?.flush()
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e("TicketPrinter", "Error cerrando conexión", e)
        }
    }

    private fun sendData(data: ByteArray) {
        try {
            outputStream?.write(data)
        } catch (e: IOException) {
            Log.e("TicketPrinter", "Error enviando datos", e)
        }
    }

    fun printText(text: String) {
        sendData(text.toByteArray(Charsets.ISO_8859_1))
    }

    fun setAlignCenter() = sendData(ALIGN_CENTER)
    fun setAlignLeft() = sendData(ALIGN_LEFT)
    fun setAlignRight() = sendData(ALIGN_RIGHT)
    fun setBold(on: Boolean) = if (on) sendData(BOLD_ON) else sendData(BOLD_OFF)
    fun setLargeFont(on: Boolean) = if (on) sendData(FONT_LARGE) else sendData(FONT_NORMAL)
    
    fun printDivider() = printText("--------------------------------\n")

    // Formateador para 32 columnas (impresora 58mm)
    fun printRow(col1: String, col2: String, col3: String) {
        // Ejemplo para 3 columnas: 16 | 8 | 8
        val line = String.format(Locale.US, "%-15s %8s %7s\n", col1.take(15), col2.take(8), col3.take(7))
        printText(line)
    }
}
