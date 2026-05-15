package com.example.agroag_mro.activitys

import android.R.attr.bitmap
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agroag_mro.R
import com.example.agroag_mro.data.AppDatabase
import com.example.agroag_mro.data.UsuarioDao
import com.example.agroag_mro.databinding.ActivityReporteFallaBinding
import com.example.agroag_mro.interfaz.RetrofitClient.apiService
import com.example.agroag_mro.models.Login
import com.example.agroag_mro.models.LoginRequest
import com.example.agroag_mro.models.ReporteFallaRequest
import com.example.agroag_mro.models.TipoTrabajos
import com.example.agroag_mro.utils.BusquedaBottomSheet
import com.example.agroag_mro.utils.Prefs
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import android.util.Base64
import android.widget.CheckBox
import com.example.agroag_mro.models.imagen
import com.example.agroag_mro.utils.Globales
import com.example.agroag_mro.utils.ReportePDFGenerator
import com.example.agroag_mro.utils.ReportePDFGenerator2
import java.io.File
import java.io.FileOutputStream
import android.net.Uri
import android.graphics.ImageDecoder
import android.os.Build
import kotlin.io.encoding.ExperimentalEncodingApi

class ReporteFallaActivity: AppCompatActivity() {
    private lateinit var binding: ActivityReporteFallaBinding
    private lateinit var db: AppDatabase
    private lateinit var loginUserDao: UsuarioDao
    // Referencias a los views
    private lateinit var dialogView: View
    private lateinit var tvActvio: TextView
    private lateinit var tv_activocve: TextView
    private lateinit var tvFolio: TextView
    private lateinit var etFalla: EditText
    private lateinit var tvFecha: TextView
    private lateinit var spinnerMantenimiento: Spinner
    private lateinit var spinnerTipoTrabajo: Spinner
    private lateinit var etDetalleFalla: EditText
    private lateinit var etObservaciones: EditText
    private lateinit var btnImagenes: Button
    private lateinit var btnCargarImg: Button
    private lateinit var btnGuardar: Button
    private lateinit var checkImage: CheckBox
    //vairables locales
    private var usuario: String? = null
    private var pass: String? = null
    //para lasimagenes
    private val REQUEST_IMAGE_CAPTURE = 1001
    private val REQUEST_IMAGE_PICK = 1002
    private val imagenesBase64 = mutableListOf<imagen>() // aquí se guardan temporalmente

    private lateinit var reporteGenerator: ReportePDFGenerator
    private lateinit var reportePDFGenerator2: ReportePDFGenerator2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_falla)

        usuario = Prefs(this).obtenerUsuario().first.toString()
        pass = Prefs(this).obtenerUsuario().second.toString()
        reporteGenerator = ReportePDFGenerator(this)
        reportePDFGenerator2 = ReportePDFGenerator2(this)
        // Inicializar views
        initViews()

        // obtenemmos fecha actual
        fecha()

        // obtenemmos folio actual
        folio()

        // Configurar spinners
        setupSpinners()

        // Configurar listeners de botones
        setupButtonListeners()
    }

    //funcion para inicializar los views
    private fun initViews() {
        tvActvio = findViewById(R.id.tv_activo)
        tv_activocve = findViewById(R.id.tv_activocve)
        tvFolio = findViewById(R.id.tv_folio)
        etFalla = findViewById(R.id.et_falla)
        tvFecha = findViewById(R.id.tv_fecha)

        spinnerMantenimiento = findViewById(R.id.spinner_mantenimiento)
        spinnerTipoTrabajo = findViewById(R.id.spinner_tipo_trabajo)
        etDetalleFalla = findViewById(R.id.et_detalle_falla)
        etObservaciones = findViewById(R.id.et_observaciones)
        btnImagenes = findViewById(R.id.btn_imagenes)
        btnCargarImg = findViewById(R.id.btn_cargarImg)
        btnGuardar = findViewById(R.id.btn_guardar)
        checkImage = findViewById(R.id.chImage)
    }

    //funcion para obtener la fecha actual
    private fun fecha(){
        val fechaActual = SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
        tvFecha.text = fechaActual
    }

    //funcion para obtener el folio actual
    private fun folio(){
        lifecycleScope.launch {
            try {
                val request = LoginRequest(Login(usuario.toString(), pass.toString()))
                System.out.println("request:"+request)
                val response = apiService.getFolio(request)
                System.out.println("response:"+response)
                if (response.isSuccessful) {
                    val folio = response.body()
                    System.out.println("folio:"+folio)
                    folio?.let { folio ->
                        val okItem = folio.ResponseFolios?.find { it.ok != null }
                        if (okItem?.ok == "1") {
                            tvFolio.text = okItem.folio
                        }
                        }
                }else{
                    showToast("Error al cargar folio")
                }

            }catch (e: Exception){
                System.out.println("error:"+e)
                showToast("Error al conectar con el servidor")
            }
        }
    }

    //funcion para configurar los spinners
    private fun setupSpinners() {
        // Configurar Spinner Mantenimiento

        val manto = listOf(
            TipoTrabajos("Correctivo", "C")
            //TipoTrabajos("Preventivo", "P")
        )
        val mantenimientoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, manto)
        mantenimientoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMantenimiento.adapter = mantenimientoAdapter
        spinnerMantenimiento.setSelection(0) // Seleccionar "Correctivo" por defecto

        // Configurar Spinner Tipo de Trabajo
        obtenerTipoTrabajo()

    }

    //funcion para obtener los tipo de trabajo desde el servidor
    private fun obtenerTipoTrabajo(){
        lifecycleScope.launch {
            try {
                val request = LoginRequest(Login(usuario.toString(), pass.toString()))
                System.out.println("tiposrequest:"+request)
                val response = apiService.getTipoTrabajo (request)
                System.out.println("tiposresponse:"+response)
                if (response.isSuccessful) {
                    val opciones = response.body()
                    System.out.println("tiposopciones:" + opciones)
                    opciones?.let { tiposTrabajo ->
                        val okItem = tiposTrabajo.ResponseTrabajos?.find { it.ok != null }
                        if (okItem?.ok == "1") {
                            //llenamos el spinner reccoriendo el arreglo de tipos de trabajo
                            val listaTipoTrabajo =
                                tiposTrabajo.ResponseTrabajos.filter { it.cve != null }
                            val tt = mutableListOf<TipoTrabajos>()
                            for (tipoTrabajo in listaTipoTrabajo) {
                                tt.add(TipoTrabajos(tipoTrabajo.name!!, tipoTrabajo.cve!!))
                            }


                            val tipoTrabajoAdapter = ArrayAdapter(this@ReporteFallaActivity, android.R.layout.simple_spinner_item, tt)
                            tipoTrabajoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            spinnerTipoTrabajo.adapter = tipoTrabajoAdapter
                            spinnerTipoTrabajo.setSelection(0) //

                        }
                    }
                }else{
                    showToast("Error al cargar opciones")
                }

            }catch (e: Exception){
                showToast("Error al conectar con el servidor")
            }
        }
    }

    //funcion para configurar los listeners de los botones
    private fun setupButtonListeners() {
        btnImagenes.setOnClickListener {
            // Funcionalidad para agregar imágenes
           abrirCamara()
        }

        btnCargarImg.setOnClickListener {
            abrirGaleria()
        }


        btnGuardar.setOnClickListener {
            guardarReporte()
        }

        tvActvio.setOnClickListener {
            // Mostrar diálogo de búsqueda de activos

            mostrarDialogoBusquedaActivos()
        }
    }

    //funcion para capturar lasfotos desde la camara
    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Selecciona Imágenes"), REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    imageBitmap?.let {
                        val base64String = convertirBase64(it)
                        imagenesBase64.add(imagen(base64String))
                    }
                }
                REQUEST_IMAGE_PICK -> {
                    if (data?.clipData != null) {
                        val count = data.clipData!!.itemCount
                        for (i in 0 until count) {
                            val imageUri = data.clipData!!.getItemAt(i).uri
                            val bitmap = uriToBitmap(imageUri)
                            bitmap?.let {
                                val resized = redimensionarBitmap(it, 800, 800)
                                val base64String = convertirBase64(resized)
                                imagenesBase64.add(imagen(base64String))
                            }
                        }
                    } else if (data?.data != null) {
                        val imageUri = data.data!!
                        val bitmap = uriToBitmap(imageUri)
                        bitmap?.let {
                            val resized = redimensionarBitmap(it, 800, 800)
                            val base64String = convertirBase64(resized)
                            imagenesBase64.add(imagen(base64String))
                        }
                    }
                }
            }
            if (imagenesBase64.size > 0) {
                checkImage.isChecked = true
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    @OptIn(ExperimentalEncodingApi::class)
    private fun convertirBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Bajamos la calidad a 70 para reducir el peso significativamente
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun redimensionarBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) return bitmap

        val ratio: Float = width.toFloat() / height.toFloat()
        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (width > height) {
            finalHeight = (maxWidth / ratio).toInt()
        } else {
            finalWidth = (maxHeight * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }
    //funcion para mostrar el dialogo de busqueda de activos
    private fun mostrarDialogoBusquedaActivos   () {
        val bottomSheet = BusquedaBottomSheet("1") { resultadoSeleccionado ->//tipo de busqueda 1 para buscar activos por tipo M,C,E,U, etc y 2 para busqueda de activos por usuario
            tvActvio.text = resultadoSeleccionado.name
            tv_activocve.text = resultadoSeleccionado.cve
        }
        bottomSheet.show(supportFragmentManager, "BusquedaBottomSheet")


    }

    //funcion para guardar el reporte
    private fun guardarReporte() {
        // Validar campos obligatorios
        if (!validarCampos()) {
            return
        }

        // Obtener datos del formulario
        val activo = tv_activocve.text.toString()//clave activo
        val nombreActivo = tvActvio.text.toString()//nombre activo
        val falla = etFalla.text.toString().trim()
        val tipomnto = spinnerMantenimiento.selectedItem as TipoTrabajos
        val claveTipomanto = tipomnto.clave
        val tipoTrabajo = spinnerTipoTrabajo.selectedItem as TipoTrabajos
        val claveTipoTrabajo = tipoTrabajo.clave
        val detalleFalla = etDetalleFalla.text.toString().trim()
        val observaciones = etObservaciones.text.toString().trim()
        val folio = tvFolio.text.toString()
        val fecha = tvFecha.text.toString()

        //enviamos los datos al servidor
        lifecycleScope.launch {
            try {

                //obtenemos dato susuario
                val dbUser = Globales.obtenerUsuarioPorId(usuario.toString())

                val request = ReporteFallaRequest(Login(usuario.toString(), pass.toString()), folio,claveTipomanto,claveTipoTrabajo,activo,falla,detalleFalla,observaciones,fecha,imagenesBase64 )
                System.out.println("request:"+request)
                val response = apiService.sendReporteFalla(request)
                System.out.println("response:"+response)
                if (response.isSuccessful) {
                    val reporte = response.body()
                    System.out.println("reporte:"+reporte)
                    reporte?.let { reporte ->
                        val okItem = reporte.ResponseReporteFalla?.find { it.ok != null }
                        if (okItem?.ok == "1") {
                            //showToast("Reporte enviado exitosamente")
                           mensajeExitoso(folio,fecha,activo,nombreActivo,dbUser?.nombre.toString(),"Reportada",falla,tipomnto.descripcion,tipoTrabajo.descripcion,detalleFalla,observaciones)
                        }
                    }
                }else{
                    showToast("Error al enviar reporte")
                }

            }catch (e: Exception){
                System.out.println("error:"+e)
                showToast("Error al conectar con el servidor")
            }

        }

    }

    //funcion para mostrar mensaje existoso
    private fun mensajeExitoso(folio: String, fecha: String, activo: String,nameActivo: String, user: String, status: String,falla:String,manto:String,tipo:String,detalle:String,obse:String) {
        val mensaje = """
                                Reporte guardado exitosamente:
                                
                                Folio: $folio
                                Fecha: $fecha
                                Activo: $activo
                            """.trimIndent()

        //llamamo a la funcion para guardar el documento
        //generdaArchivo(folio,fecha,activo)
        generaReporte(folio,fecha,activo,nameActivo,user,status,falla,manto,tipo,detalle,obse,"Reporte de falla")
        AlertDialog.Builder(this)
            .setTitle("Reporte Guardado")
            .setMessage(mensaje)
            .setPositiveButton("OK") { dialog, _ ->
                //dialog.dismiss()
                // Opcional: limpiar formulario o cerrar activity
                limpiarFormulario()
            }
            .show()
    }

    //nuevafuncion para generarel reporte simpl
    private fun generaReporte(fol: String, fecha: String, activo: String,nameActivo: String, user: String, status: String, falla: String, mnto: String, tipo: String, detalle: String,obse: String, titu: String){
        val archivo = reportePDFGenerator2.generarArchivoSimple(
            fol, fecha, activo,nameActivo, user, status, falla, mnto, tipo, detalle,obse,titu
        )
        if (archivo != null) {
            showToast("Archivo PDF guardado en :${archivo.absolutePath}")
        }
    }
    //funcion que generada el archivo pdf o de otro tipo en el dispositivo
    private fun generdaArchivo(folio: String, fecha: String, activo: String){

        val pdfDocument = PdfDocument()
        val paint = Paint()
        //configuracion de pagina A4
        val paginaInfo = PdfDocument.PageInfo.Builder(595,842, 1).create()
        val pagina = pdfDocument.startPage(paginaInfo)
        val canvas = pagina.canvas

        //encabezados
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        paint.color = Color.BLACK
        canvas.drawText("Reporte de falla", 200f, 50f, paint)

        //fecha del doctumento
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        paint.color = Color.BLACK
        canvas.drawText("Fecha: $fecha", 80f, 100f, paint)

        //folio
        canvas.drawText("Folio: $folio", 80f, 120f, paint)

        //activo
        canvas.drawText("Activo: $activo", 80f, 140f, paint)

        pdfDocument.finishPage(pagina)

        ///guardamos en la carpeta descargas del dispositivo
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "reporte_falla.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            showToast("Archivo generado exitosamente en la carpeta de descargas")
        }catch (e: Exception){
            e.printStackTrace()
            showToast("Error al generar el archivo")
        }
        finally {
            pdfDocument.close()
        }

    }

    //funcion para validar los campos obligatorios
    private fun validarCampos(): Boolean {
        // Validar activo seleccionado
        if (tvActvio.text.toString().trim().isEmpty()) {
            showToast("Por favor selecciona un activo")
            return false
        }

        // Validar campo falla
        if (etFalla.text.toString().trim().isEmpty()) {
            etFalla.error = "Este campo es obligatorio"
            etFalla.requestFocus()
            return false
        }

        // Validar detalle de falla
        if (etDetalleFalla.text.toString().trim().isEmpty()) {
            etDetalleFalla.error = "Por favor describe el detalle de la falla"
            etDetalleFalla.requestFocus()
            return false
        }

        return true
    }

    //funcion para mostrar el dialogo de cancelar el reporte
    private fun mostrarDialogoCancelar() {
        AlertDialog.Builder(this)
            .setTitle("Cancelar Reporte")
            .setMessage("¿Estás seguro de que deseas cancelar? Se perderán todos los datos ingresados.")
            .setPositiveButton("Sí, Cancelar") { dialog, _ ->
                dialog.dismiss()
                finish() // Cerrar la activity
            }
            .setNegativeButton("No, Continuar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    //funcion para limpiar el formulario
    private fun limpiarFormulario() {
        tvActvio.text = ""
        tv_activocve.text = "Activo"
        etFalla.text.clear()
        spinnerMantenimiento.setSelection(0)
        spinnerTipoTrabajo.setSelection(0)
        etDetalleFalla.text.clear()
        etObservaciones.text.clear()
        checkImage.isChecked = false
    }

    //funcion para mostrar un mensaje toast
    private fun showToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}