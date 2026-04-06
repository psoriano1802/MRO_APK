package com.example.lacteos_flores.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ReportePDFGenerator(private val context: Context) {

    data class DatosReporte(
        val folio: String,
        val fecha: String,
        val activo: String,
        val usuario: String = "Lupita Centeno (Kepler)",
        val status: String = "Atendido",
        val falla: String = "FALLA PRUEBA",
        val tipoMantenimiento: String = "Correctivo",
        val tipoTrabajo: String = "Eléctrico",
        val detalleFalla: String = "prueba de falla",
        val observaciones: String = "prueba de falla2",
        val titulo: String = "REPORTE DE FALLA"
    )
    //reporte de refacciones
    data class DatosReporte2(
        val sucursal: String,
        val almacen: String,
        val folio: String,
        val fecha: String,
        val referencia: String,
        val activo: String,//nombre del activo
        val usuario: String = "Lupita Centeno (Kepler)",
        val partidas: List<Partida>
    )
    data class Partida(
        val clave: String,
        val cantidad: String,
        val unidad: String,
        val importe: String,
        val descripcion: String
    )


    fun generarArchivo(datos: DatosReporte): File? {
        return try {
            val pdfDocument = PdfDocument()
            val paint = Paint()

            // Configuración de página A4
            val paginaInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val pagina = pdfDocument.startPage(paginaInfo)
            val canvas = pagina.canvas

            // Configuración de colores
            val colorPrimario = Color.parseColor("#2E7D32")
            val colorSecundario = Color.parseColor("#757575")
            val colorFondo = Color.parseColor("#F5F5F5")

            // Fondo de la página
            canvas.drawColor(colorFondo)

            // Título principal
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 24f
            paint.color = colorPrimario
            canvas.drawText(datos.titulo, 150f, 80f, paint)

            // Línea decorativa bajo el título
            paint.color = colorPrimario
            paint.strokeWidth = 2f
            canvas.drawLine(150f, 90f, 445f, 90f, paint)

            var posicionY = 140f
            val margenIzquierdo = 50f
            val alturaLinea = 25f

            // Función auxiliar para dibujar campos
            fun dibujarCampo(etiqueta: String, valor: String, y: Float): Float {
                // Etiqueta
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 12f
                paint.color = colorSecundario
                canvas.drawText("$etiqueta:", margenIzquierdo, y, paint)

                // Valor
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 14f
                paint.color = Color.BLACK
                canvas.drawText(valor, margenIzquierdo, y + 18f, paint)

                return y + alturaLinea + 10f
            }

            // Información principal
            posicionY = dibujarCampo("Usuario", datos.usuario, posicionY)
            posicionY = dibujarCampo("Folio", datos.folio, posicionY)
            posicionY = dibujarCampo("Fecha", datos.fecha, posicionY)
            posicionY = dibujarCampo("Activo", datos.activo, posicionY)
            posicionY = dibujarCampo("Status", datos.status, posicionY)
            posicionY = dibujarCampo("Falla reportada", datos.falla, posicionY)
            posicionY = dibujarCampo("Tipo de mantenimiento", datos.tipoMantenimiento, posicionY)
            posicionY = dibujarCampo("Tipo de trabajo", datos.tipoTrabajo, posicionY)

            // Sección Detalle de la falla
            posicionY += 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 16f
            paint.color = colorPrimario
            canvas.drawText("DETALLE DE LA FALLA:", margenIzquierdo, posicionY, paint)

            posicionY += 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 12f
            paint.color = Color.BLACK

            val lineasDetalle = dividirTextoEnLineas(datos.detalleFalla, 80)
            lineasDetalle.forEach { linea ->
                canvas.drawText(linea, margenIzquierdo, posicionY, paint)
                posicionY += 18f
            }

            // Sección Observaciones
            posicionY += 15f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 16f
            paint.color = colorPrimario
            canvas.drawText("OBSERVACIONES:", margenIzquierdo, posicionY, paint)

            posicionY += 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 12f
            paint.color = Color.BLACK

            val lineasObservaciones = dividirTextoEnLineas(datos.observaciones, 80)
            lineasObservaciones.forEach { linea ->
                canvas.drawText(linea, margenIzquierdo, posicionY, paint)
                posicionY += 18f
            }

            // Pie de página
            val fechaGeneracion = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            paint.textSize = 10f
            paint.color = colorSecundario
            canvas.drawText("Documento generado el: $fechaGeneracion", margenIzquierdo, 820f, paint)

            pdfDocument.finishPage(pagina)

            // Guardar archivo con nombre único
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val nombreArchivo = "Reporte_Falla_${datos.folio.replace("/", "_")}_$timeStamp.pdf"
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nombreArchivo)

            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            mostrarToast("Archivo PDF generado exitosamente: $nombreArchivo")
            file

        } catch (e: Exception) {
            e.printStackTrace()
            mostrarToast("Error al generar el archivo: ${e.message}")
            null
        }
    }

    // Función simplificada para uso rápido
    fun generarArchivoSimple(folio: String, fecha: String, activo: String, user: String, status:String, falla:String, manto:String,tipo:String, detalle:String, observaciones: String,titulo: String): File? {
        val datos = DatosReporte(
            folio = folio,
            fecha = fecha,
            activo = activo,
            usuario = user,
            status = status,
            falla = falla,
            tipoMantenimiento = manto,
            tipoTrabajo = tipo,
            detalleFalla = detalle,
            observaciones= observaciones,
            titulo = titulo
        )
        return generarArchivo(datos)
    }

    // Función con datos de ejemplo
    fun generarArchivoEjemplo(): File? {
        val datos = DatosReporte(
            folio = "25/09/2025 - 16:18",
            fecha = "25/09/2025",
            activo = "C-010 - CUATRIMOTO ITALIKA ATV 200 - ALIMENTACION",
            usuario = "Lupita Centeno (Kepler)",
            status = "Atendido",
            falla = "FALLA PRUEBA",
            tipoMantenimiento = "Correctivo",
            tipoTrabajo = "Eléctrico",
            detalleFalla = "prueba de falla",
            observaciones = "prueba de falla2"
        )
        return generarArchivo(datos)
    }

    private fun dividirTextoEnLineas(texto: String, maxCaracteres: Int): List<String> {
        if (texto.length <= maxCaracteres) return listOf(texto)

        val palabras = texto.split(" ")
        val lineas = mutableListOf<String>()
        var lineaActual = ""

        palabras.forEach { palabra ->
            if ((lineaActual + palabra).length <= maxCaracteres) {
                lineaActual += if (lineaActual.isEmpty()) palabra else " $palabra"
            } else {
                if (lineaActual.isNotEmpty()) {
                    lineas.add(lineaActual)
                }
                lineaActual = palabra
            }
        }

        if (lineaActual.isNotEmpty()) {
            lineas.add(lineaActual)
        }

        return lineas
    }


    // Método específico para reportes con partidas
    fun generarArchivoConPartidas(
        folio: String,
        fecha: String,
        activo: String,
        partidas: List<Partida>,
        usuario: String = " ",
        status: String = " ",
        falla: String = " ",
        tipoMantenimiento: String = " ",
        tipoTrabajo: String = " ",
        detalleFalla: String = " ",
        observaciones: String = " "
    ): File? {
        val datos = DatosReporte2(
            sucursal = "Sucursal",
            almacen = "Almacen",
            folio = folio,
            fecha = fecha,
            referencia = "Referencia",
            activo = activo,
            usuario = usuario,
            partidas = partidas
        )
        return generarArchivo2(datos)
    }
    fun generarArchivo2(datos: DatosReporte2): File? {
        return try {
            Log.d("PDFGenerator", "Iniciando generación de PDF...")

            val pdfDocument = PdfDocument()
            val paint = Paint()

            // Configuración de página A4
            val paginaInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val pagina = pdfDocument.startPage(paginaInfo)
            val canvas = pagina.canvas

            // Configuración de colores
            val colorPrimario = Color.parseColor("#2E7D32")
            val colorSecundario = Color.parseColor("#757575")
            val colorFondo = Color.parseColor("#F5F5F5")

            // Fondo de la página
            canvas.drawColor(colorFondo)

            // Título principal
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 24f
            paint.color = colorPrimario
            canvas.drawText("REPORTE DE FALLA", 150f, 80f, paint)

            // Línea decorativa bajo el título
            paint.color = colorPrimario
            paint.strokeWidth = 2f
            canvas.drawLine(150f, 90f, 445f, 90f, paint)

            var posicionY = 140f
            val margenIzquierdo = 50f
            val alturaLinea = 25f

            // Función auxiliar para dibujar campos
            fun dibujarCampo(etiqueta: String, valor: String, y: Float): Float {
                // Etiqueta
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 12f
                paint.color = colorSecundario
                canvas.drawText("$etiqueta:", margenIzquierdo, y, paint)

                // Valor
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 14f
                paint.color = Color.BLACK
                canvas.drawText(valor, margenIzquierdo, y + 18f, paint)

                return y + alturaLinea + 10f
            }

            // Información principal
            posicionY = dibujarCampo("Sucursal", datos.sucursal, posicionY)
            posicionY = dibujarCampo("Almacen", datos.almacen, posicionY)
            posicionY = dibujarCampo("Referencia", datos.referencia, posicionY)
            posicionY = dibujarCampo("Usuario", datos.usuario, posicionY)
            posicionY = dibujarCampo("Folio", datos.folio, posicionY)
            posicionY = dibujarCampo("Fecha", datos.fecha, posicionY)
            posicionY = dibujarCampo("Activo", datos.activo, posicionY)




            // Dibujar partidas si existen
            if (datos.partidas.isNotEmpty()) {
                posicionY += 20f
                dibujarPartidas(canvas, datos.partidas, posicionY, paint)
            }

            // Pie de página
            val fechaGeneracion = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            paint.textSize = 10f
            paint.color = colorSecundario
            canvas.drawText("Documento generado el: $fechaGeneracion", margenIzquierdo, 820f, paint)

            pdfDocument.finishPage(pagina)

            // Guardar archivo con nombre único
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val nombreArchivo = "Reporte_Falla_${datos.folio.replace("/", "_")}_$timeStamp.pdf"

            // Verificar permisos y crear directorio
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val file = File(downloadsDir, nombreArchivo)

            Log.d("PDFGenerator", "Guardando archivo en: ${file.absolutePath}")

            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            Log.d("PDFGenerator", "PDF generado exitosamente")
            mostrarToast("Archivo PDF generado exitosamente: $nombreArchivo")
            file

        } catch (e: Exception) {
            Log.e("PDFGenerator", "Error al generar PDF: ${e.message}", e)
            mostrarToast("Error al generar el archivo: ${e.message}")
            null
        }
    }

    private fun dibujarPartidas(canvas: Canvas, partidas: List<Partida>, startY: Float, paint: Paint) {
        var posicionY = startY
        val margenIzquierdo = 50f

        // Título de partidas
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        paint.color = Color.parseColor("#2E7D32")
        canvas.drawText("PARTIDAS DE REFACCIONES/MATERIALES:", margenIzquierdo, posicionY, paint)

        posicionY += 25f

        // Encabezado de la tabla
        paint.textSize = 10f
        paint.color = Color.WHITE

        // Fondo del encabezado
        val headerPaint = Paint()
        headerPaint.color = Color.parseColor("#2E7D32")
        canvas.drawRect(margenIzquierdo, posicionY - 15f, 545f, posicionY + 5f, headerPaint)

        paint.color = Color.WHITE
        canvas.drawText("#", margenIzquierdo + 5f, posicionY, paint)
        canvas.drawText("Descripción", margenIzquierdo + 30f, posicionY, paint)
        canvas.drawText("Cant", margenIzquierdo + 250f, posicionY, paint)
        canvas.drawText("Unidad", margenIzquierdo + 290f, posicionY, paint)
        canvas.drawText("P. Unit", margenIzquierdo + 340f, posicionY, paint)
        canvas.drawText("Importe", margenIzquierdo + 400f, posicionY, paint)

        posicionY += 20f

        // Filas de partidas
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        var totalGeneral = 0.0

        partidas.forEachIndexed { index, partida ->
            // Fondo alternado para mejor lectura
            val rowPaint = Paint()
            if (index % 2 == 0) {
                rowPaint.color = Color.parseColor("#F8F9FA")
            } else {
                rowPaint.color = Color.WHITE
            }
            canvas.drawRect(margenIzquierdo, posicionY - 10f, 545f, posicionY + 20f, rowPaint)

            paint.color = Color.BLACK
            canvas.drawText(partida.clave, margenIzquierdo + 5f, posicionY + 5f, paint)
            canvas.drawText(partida.descripcion, margenIzquierdo + 30f, posicionY + 5f, paint)
            canvas.drawText(partida.cantidad, margenIzquierdo + 250f, posicionY + 5f, paint)
            canvas.drawText(partida.unidad, margenIzquierdo + 290f, posicionY + 5f, paint)
            canvas.drawText(partida.importe, margenIzquierdo + 400f, posicionY + 5f, paint)

            // Calcular total
            try {
                val importe = partida.importe.replace("$", "").replace(",", "").trim()
                if (importe.isNotEmpty()) {
                    totalGeneral += importe.toDouble()
                }
            } catch (e: Exception) {
                Log.e("PDFGenerator", "Error al convertir importe: ${partida.importe}")
            }

            posicionY += 20f

            // Verificar que no nos salgamos de la página
            if (posicionY > 800f) {
                Log.w("PDFGenerator", "Demasiadas partidas, algunas no caben en la página")
                return
            }
        }

        // Total
        if (posicionY < 800f) {
            posicionY += 10f
            paint.color = Color.parseColor("#2E7D32")
            paint.strokeWidth = 1f
            canvas.drawLine(margenIzquierdo + 350f, posicionY, 545f, posicionY, paint)

            posicionY += 15f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11f
            canvas.drawText("TOTAL: $${String.format(Locale.US, "%.2f", totalGeneral)}", margenIzquierdo + 400f, posicionY, paint)
        }
    }
    private fun mostrarToast(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
    }
}