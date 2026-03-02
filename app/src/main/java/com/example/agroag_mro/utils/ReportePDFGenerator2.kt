package com.example.agroag_mro.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ReportePDFGenerator2(private val context: Context) {

    data class DatosReporte(
        val sucursal: String,
        val almacen: String,
        val folio: String,
        val fecha: String,
        val activo: String,
        val nameActivo: String,
        val usuario: String = "",
        val status: String = "",
        val falla: String = "",
        val tipoMantenimiento: String = "",
        val tipoTrabajo: String = "",
        val detalleFalla: String = "",
        val observaciones: String = "",
        val partidas: List<Partida> = emptyList()
    )

    data class Partida(
        val numero: String,
        val descripcion: String,
        val cantidad: String,
        val unidad: String,
        val precioUnitario: String,
        val importe: String
    )

    fun generarArchivo(datos: DatosReporte,name: String): File? {
        return try {
            Log.d("PDFGenerator", "Iniciando generación de PDF...")

            val pdfDocument = PdfDocument()

            // Configuración de página A4
            val paginaInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val pagina = pdfDocument.startPage(paginaInfo)
            val canvas = pagina.canvas

            dibujarPaginaCompleta(canvas, datos,name)
            pdfDocument.finishPage(pagina)

            // Guardar archivo en ubicación accesible
            val file = crearArchivoEnUbicacionAccesible(datos.folio,name)

            if (file == null) {
                mostrarToast("Error: No se pudo crear el archivo")
                pdfDocument.close()
                return null
            }

            Log.d("PDFGenerator", "Guardando archivo en: ${file.absolutePath}")

            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            Log.d("PDFGenerator", "PDF generado exitosamente")
            mostrarToast("Archivo PDF guardado en: ${file.parentFile?.name ?: "Descargas"}")
            file

        } catch (e: Exception) {
            Log.e("PDFGenerator", "Error al generar PDF: ${e.message}", e)
            mostrarToast("Error al generar el archivo: ${e.message}")
            null
        }
    }
    private fun crearArchivoEnUbicacionAccesible(folio: String, name: String): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val nombreArchivo = "${name}_${folio.replace("/", "_").replace(" ", "_").replace(":", "_")}_$timeStamp.pdf"

            // OPCIÓN 1: DIRECTORIO DE DOCUMENTOS PÚBLICOS (Más accesible)
            val directorio = obtenerDirectorioMasAccesible()

            if (directorio == null) {
                Log.e("PDFGenerator", "No se pudo acceder a ningún directorio público")
                return crearArchivoEnDirectorioInterno(nombreArchivo)
            }

            // Verificar permisos de escritura
            if (!directorio.canWrite()) {
                Log.e("PDFGenerator", "Sin permisos de escritura en: ${directorio.absolutePath}")
                return crearArchivoEnDirectorioInterno(nombreArchivo)
            }

            // Crear subcarpeta específica de la app si es necesario
            val carpetaApp = File(directorio, "Reportes_MRO")
            if (!carpetaApp.exists()) {
                carpetaApp.mkdirs()
            }

            val file = File(carpetaApp, nombreArchivo)

            // Verificar si podemos crear el archivo
            if (file.exists() && !file.delete()) {
                Log.w("PDFGenerator", "No se pudo eliminar archivo existente, creando nuevo")
                return File(carpetaApp, "Reporte_${timeStamp}.pdf")
            }

            file

        } catch (e: Exception) {
            Log.e("PDFGenerator", "Error creando archivo accesible: ${e.message}")
            crearArchivoEnDirectorioInterno("Reporte_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf")
        }
    }

    private fun obtenerDirectorioMasAccesible(): File? {
        return try {
            // Orden de preferencia para directorios accesibles:

            // 1. DESCARGAS (Más accesible para usuarios)
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (downloads.exists() && downloads.canWrite()) {
                    Log.d("PDFGenerator", "Usando directorio: Downloads")
                    return downloads
                }
            }

            // 2. DOCUMENTOS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val documents = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    if (documents.exists() && documents.canWrite()) {
                        Log.d("PDFGenerator", "Usando directorio: Documents")
                        return documents
                    }
                } catch (e: Exception) {
                    Log.w("PDFGenerator", "No se pudo acceder a Documents: ${e.message}")
                }
            }

            // 3. DIRECTORIO EXTERNO DE LA APP (aún visible para usuarios)
            val externalFilesDir = context.getExternalFilesDir(null)
            if (externalFilesDir != null && externalFilesDir.canWrite()) {
                Log.d("PDFGenerator", "Usando directorio externo de la app")
                return externalFilesDir
            }

            // 4. Fallback: directorio interno
            Log.w("PDFGenerator", "Usando directorio interno como fallback")
            null

        } catch (e: Exception) {
            Log.e("PDFGenerator", "Error obteniendo directorio accesible: ${e.message}")
            null
        }
    }

    private fun crearArchivoEnDirectorioInterno(nombreArchivo: String): File {
        val directorioInterno = File(context.filesDir, "Reportes_PDF")
        if (!directorioInterno.exists()) {
            directorioInterno.mkdirs()
        }
        return File(directorioInterno, nombreArchivo)
    }

    // Método para obtener la ruta amigable para mostrar al usuario
    fun obtenerRutaAmigable(file: File): String {
        return when {
            file.absolutePath.contains(Environment.DIRECTORY_DOWNLOADS) -> "Carpeta de Descargas/Reportes_Fallas"
            file.absolutePath.contains(Environment.DIRECTORY_DOCUMENTS) -> "Carpeta de Documentos/Reportes_Fallas"
            file.absolutePath.contains("filesDir") -> "Almacenamiento interno de la app"
            else -> file.parent ?: "Dispositivo"
        }
    }
    // Método para abrir el archivo directamente
    fun abrirArchivo(file: File): Boolean {
        return try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            } else {
                android.net.Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("PDFGenerator", "Error abriendo archivo: ${e.message}")
            // Si no hay app para PDFs, sugerir instalar una
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse("https://play.google.com/store/search?q=pdf%20reader")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Log.e("PDFGenerator", "Error abriendo Play Store: ${e2.message}")
            }
            false
        }
    }

    // Método para listar archivos PDF generados
    fun listarArchivosGenerados(): List<File> {
        return try {
            val directorios = mutableListOf<File>()

            // Buscar en todas las ubicaciones posibles
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val carpetaDownloads = File(downloads, "Reportes_Fallas")
            if (carpetaDownloads.exists()) {
                directorios.add(carpetaDownloads)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val documents = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val carpetaDocuments = File(documents, "Reportes_Fallas")
                if (carpetaDocuments.exists()) {
                    directorios.add(carpetaDocuments)
                }
            }

            val externalFilesDir = context.getExternalFilesDir(null)
            if (externalFilesDir != null) {
                val carpetaExterna = File(externalFilesDir, "Reportes_Fallas")
                if (carpetaExterna.exists()) {
                    directorios.add(carpetaExterna)
                }
            }

            // Combinar todos los archivos PDF encontrados
            directorios.flatMap { directorio ->
                directorio.listFiles { file ->
                    file.isFile && file.name.endsWith(".pdf")
                }?.toList() ?: emptyList()
            }.sortedByDescending { it.lastModified() }

        } catch (e: Exception) {
            Log.e("PDFGenerator", "Error listando archivos: ${e.message}")
            emptyList()
        }
    }
    private fun dibujarPaginaCompleta(canvas: Canvas, datos: DatosReporte,name: String) {
        val paint = Paint()

        // Configuración de colores
        val colorPrimario = Color.parseColor("#2E7D32")
        val colorSecundario = Color.parseColor("#757575")
        val colorFondo = Color.parseColor("#F5F5F5")
        val colorBorde = Color.parseColor("#E0E0E0")

        // Fondo de la página
        canvas.drawColor(colorFondo)

        // ===== ENCABEZADO =====
        dibujarEncabezado(canvas, paint, colorPrimario,name)

        // ===== INFORMACIÓN PRINCIPAL EN CUADRÍCULA =====
        dibujarInformacionPrincipal(canvas, paint, datos, colorPrimario, colorSecundario, colorBorde)

        // ===== DETALLE DE FALLA =====
        var posicionY = dibujarSeccionDetalle(canvas, paint, datos, colorPrimario, 320f)

        // ===== OBSERVACIONES =====
        posicionY = dibujarSeccionObservaciones(canvas, paint, datos, colorPrimario, posicionY + 20f)

        // ===== PARTIDAS (si existen) =====
        if (datos.partidas.isNotEmpty()) {
            dibujarPartidas(canvas, paint, datos.partidas, posicionY + 40f)
        }

        // ===== PIE DE PÁGINA =====
        dibujarPiePagina(canvas, paint, colorSecundario)
    }

    private fun dibujarEncabezado(canvas: Canvas, paint: Paint, colorPrimario: Int,name: String) {
        // Título principal
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        paint.color = colorPrimario
        canvas.drawText(name, 150f, 60f, paint)

        // Línea decorativa
        paint.color = colorPrimario
        paint.strokeWidth = 3f
        canvas.drawLine(50f, 75f, 545f, 75f, paint)
    }

    private fun dibujarInformacionPrincipal(
        canvas: Canvas,
        paint: Paint,
        datos: DatosReporte,
        colorPrimario: Int,
        colorSecundario: Int,
        colorBorde: Int
    ) {
        val margen = 50f
        val anchoColumna = 245f
        var posicionY = 100f

        // Fondo de la sección
        val fondoPaint = Paint()
        fondoPaint.color = Color.WHITE
        canvas.drawRect(margen, posicionY, 545f, posicionY + 200f, fondoPaint)

        // Borde
        val bordePaint = Paint()
        bordePaint.color = colorBorde
        bordePaint.style = Paint.Style.STROKE
        bordePaint.strokeWidth = 1f
        canvas.drawRect(margen, posicionY, 545f, posicionY + 200f, bordePaint)

        // Título de la sección
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        paint.color = colorPrimario
        canvas.drawText("INFORMACIÓN GENERAL", margen + 10f, posicionY + 20f, paint)

        // Línea divisoria vertical
        bordePaint.color = colorBorde
        canvas.drawLine(margen + anchoColumna, posicionY + 30f, margen + anchoColumna, posicionY + 190f, bordePaint)

        // Línea divisoria horizontal
        canvas.drawLine(margen, posicionY + 100f, 545f, posicionY + 100f, bordePaint)

        // ===== COLUMNA IZQUIERDA =====
        var yActual = posicionY + 45f

        // Fila 1: Usuario y Folio
        dibujarCampoGrid(canvas, paint, "Usuario:", datos.usuario, margen + 15f, yActual, anchoColumna - 20f, colorSecundario)
        dibujarCampoGrid(canvas, paint, "Folio:", datos.folio, margen + anchoColumna + 15f, yActual, anchoColumna - 20f, colorSecundario)
        yActual += 35f

        // Fila 2: Fecha y Activo
        dibujarCampoGrid(canvas, paint, "Fecha:", datos.fecha, margen + 15f, yActual, anchoColumna - 20f, colorSecundario)
        dibujarCampoGrid(canvas, paint, "Activo:", datos.activo+"/"+datos.nameActivo, margen + anchoColumna + 15f, yActual, anchoColumna - 20f, colorSecundario)
        yActual += 35f

        // ===== SEGUNDA FILA (debajo de la línea horizontal) =====
        yActual = posicionY + 120f
        var lbl1 = "Status:"
        var dat1 = datos.status
        var lbl2 = "Falla:"
        var dat2 = datos.falla
        if(datos.sucursal != ""){
            lbl1 = "Sucursal:"
            dat1 = datos.sucursal
            lbl2 = "Almacen:"
            dat2 = datos.almacen
        }
        // Fila 3: Status y Falla
        dibujarCampoGrid(canvas, paint, lbl1, dat1, margen + 15f, yActual, anchoColumna - 20f, colorSecundario)
        dibujarCampoGrid(canvas, paint, lbl2, dat2, margen + anchoColumna + 15f, yActual, anchoColumna - 20f, colorSecundario)
        yActual += 35f

        // Fila 4: Mantenimiento y Trabajo
        dibujarCampoGrid(canvas, paint, "Mantenimiento:", datos.tipoMantenimiento, margen + 15f, yActual, anchoColumna - 20f, colorSecundario)
        dibujarCampoGrid(canvas, paint, "Tipo Trabajo:", datos.tipoTrabajo, margen + anchoColumna + 15f, yActual, anchoColumna - 20f, colorSecundario)
    }

    private fun dibujarCampoGrid(
        canvas: Canvas,
        paint: Paint,
        etiqueta: String,
        valor: String,
        x: Float,
        y: Float,
        ancho: Float,
        colorSecundario: Int
    ) {
        // Etiqueta
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        paint.color = colorSecundario
        canvas.drawText(etiqueta, x, y, paint)

        // Valor
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.BLACK

        // Dividir valor si es muy largo
        val lineasValor = dividirTextoEnLineas(valor, 30)
        if (lineasValor.size > 1) {
            canvas.drawText(lineasValor[0], x, y + 15f, paint)
            canvas.drawText(lineasValor[1], x, y + 30f, paint)
        } else {
            canvas.drawText(valor, x, y + 15f, paint)
        }
    }

    private fun dibujarSeccionDetalle(
        canvas: Canvas,
        paint: Paint,
        datos: DatosReporte,
        colorPrimario: Int,
        startY: Float
    ): Float {
        val margen = 50f
        var posicionY = startY

        // Fondo de la sección
        val fondoPaint = Paint()
        fondoPaint.color = Color.WHITE
        canvas.drawRect(margen, posicionY, 545f, posicionY + 120f, fondoPaint)

        // Borde
        val bordePaint = Paint()
        bordePaint.color = Color.parseColor("#E0E0E0")
        bordePaint.style = Paint.Style.STROKE
        bordePaint.strokeWidth = 1f
        canvas.drawRect(margen, posicionY, 545f, posicionY + 120f, bordePaint)

        // Título
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        paint.color = colorPrimario
        canvas.drawText("DETALLE DE LA FALLA", margen + 10f, posicionY + 20f, paint)

        // Contenido
        posicionY += 35f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.BLACK

        val lineasDetalle = dividirTextoEnLineas(datos.detalleFalla, 80)
        lineasDetalle.forEachIndexed { index, linea ->
            if (index < 5) { // Máximo 5 líneas
                canvas.drawText("• $linea", margen + 15f, posicionY, paint)
                posicionY += 15f
            }
        }

        return startY + 120f
    }

    private fun dibujarSeccionObservaciones(
        canvas: Canvas,
        paint: Paint,
        datos: DatosReporte,
        colorPrimario: Int,
        startY: Float
    ): Float {
        val margen = 50f
        var posicionY = startY

        // Fondo de la sección
        val fondoPaint = Paint()
        fondoPaint.color = Color.WHITE
        canvas.drawRect(margen, posicionY, 545f, posicionY + 100f, fondoPaint)

        // Borde
        val bordePaint = Paint()
        bordePaint.color = Color.parseColor("#E0E0E0")
        bordePaint.style = Paint.Style.STROKE
        bordePaint.strokeWidth = 1f
        canvas.drawRect(margen, posicionY, 545f, posicionY + 100f, bordePaint)

        // Título
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        paint.color = colorPrimario
        canvas.drawText("OBSERVACIONES", margen + 10f, posicionY + 20f, paint)

        // Contenido
        posicionY += 35f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.BLACK

        val lineasObservaciones = dividirTextoEnLineas(datos.observaciones, 80)
        lineasObservaciones.forEachIndexed { index, linea ->
            if (index < 4) { // Máximo 4 líneas
                canvas.drawText("• $linea", margen + 15f, posicionY, paint)
                posicionY += 15f
            }
        }

        return startY + 100f
    }

    private fun dibujarPartidas(canvas: Canvas, paint: Paint, partidas: List<Partida>, startY: Float) {
        val margen = 50f
        var posicionY = startY

        // Título de partidas
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        paint.color = Color.parseColor("#2E7D32")
        canvas.drawText("PARTIDAS DE REFACCIONES/MATERIALES", margen, posicionY, paint)

        posicionY += 25f

        // Encabezado de la tabla
        paint.textSize = 10f
        paint.color = Color.WHITE

        // Fondo del encabezado
        val headerPaint = Paint()
        headerPaint.color = Color.parseColor("#2E7D32")
        canvas.drawRect(margen, posicionY - 15f, 545f, posicionY + 5f, headerPaint)

        paint.color = Color.WHITE
        canvas.drawText("#", margen + 5f, posicionY, paint)
        canvas.drawText("Descripción", margen + 30f, posicionY, paint)
        canvas.drawText("Cant", margen + 250f, posicionY, paint)
        canvas.drawText("Unidad", margen + 290f, posicionY, paint)
        canvas.drawText("P. Unit", margen + 340f, posicionY, paint)
        canvas.drawText("Importe", margen + 400f, posicionY, paint)

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
            canvas.drawRect(margen, posicionY - 10f, 545f, posicionY + 20f, rowPaint)

            paint.color = Color.BLACK
            canvas.drawText(partida.numero, margen + 5f, posicionY + 5f, paint)
            canvas.drawText(partida.descripcion, margen + 30f, posicionY + 5f, paint)
            canvas.drawText(partida.cantidad, margen + 250f, posicionY + 5f, paint)
            canvas.drawText(partida.unidad, margen + 290f, posicionY + 5f, paint)
            canvas.drawText(partida.precioUnitario, margen + 340f, posicionY + 5f, paint)
            canvas.drawText(partida.importe, margen + 400f, posicionY + 5f, paint)

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
        }

        // Total
        if (posicionY < 800f) {
            posicionY += 10f
            paint.color = Color.parseColor("#2E7D32")
            paint.strokeWidth = 1f
            canvas.drawLine(margen + 350f, posicionY, 545f, posicionY, paint)

            posicionY += 15f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11f
            canvas.drawText("TOTAL: $${String.format(Locale.US, "%.2f", totalGeneral)}", margen + 400f, posicionY, paint)
        }
    }

    private fun dibujarPiePagina(canvas: Canvas, paint: Paint, colorSecundario: Int) {
        val fechaGeneracion = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        paint.textSize = 10f
        paint.color = colorSecundario
        canvas.drawText("Documento generado el: $fechaGeneracion", 50f, 820f, paint)
    }

    // Método específico para reportes con partidas utilizado s para  solicitud de refacciones y carga de mano de obra
    fun generarArchivoConPartidas(
        sucursal: String,
        almacen: String,
        usuario: String,
        folio: String,
        fecha: String,
        activo: String,
        nameActivo: String,
        partidas: List<Partida>,
        comentarios: String,
        name: String//nombre encabezado  y reporte
    ): File? {
        Log.d("PDFGenerator", "Generando archivo con partidas: $folio")

        val datos = DatosReporte(
            sucursal = sucursal,
            almacen = almacen,
            usuario = usuario,
            folio = folio,
            fecha = fecha,
            activo = activo,
            nameActivo = nameActivo,
            partidas = partidas,
            observaciones = comentarios
        )
        return generarArchivo(datos,name)
    }

    // Función simplificada para uso rápido utilizados para reporte de falla
    fun generarArchivoSimple(folio: String, fecha: String, activo: String, nameActivo: String, user: String, status:String, falla:String, manto:String,tipo:String, detalle:String, observaciones: String,name: String): File? {
        val datos = DatosReporte(
            sucursal = "",
            almacen = "",
            folio = folio,
            fecha = fecha,
            activo = activo,
            nameActivo = nameActivo,
            usuario = user,
            status = status,
            falla = falla,
            tipoMantenimiento = manto,
            tipoTrabajo = tipo,
            detalleFalla = detalle,
            observaciones = observaciones

        )
        return generarArchivo(datos,name)
    }

    private fun dividirTextoEnLineas(texto: String, maxCaracteres: Int): List<String> {
        if (texto.isEmpty()) return listOf("")
        if (texto.length <= maxCaracteres) return listOf(texto)

        val palabras = texto.split(" ")
        val lineas = mutableListOf<String>()
        var lineaActual = ""

        for (palabra in palabras) {
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

        return if (lineas.isEmpty()) listOf(texto) else lineas
    }

    private fun mostrarToast(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
    }
}