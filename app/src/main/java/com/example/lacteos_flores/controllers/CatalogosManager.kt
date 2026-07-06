package com.example.lacteos_flores.controllers

import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.data.BancoEntity
import com.example.lacteos_flores.data.CarteraEntity
import com.example.lacteos_flores.data.ClientsEntity
import com.example.lacteos_flores.data.DoctosEntity
import com.example.lacteos_flores.data.ExistenciaEntity
import com.example.lacteos_flores.data.GastoRegistradoEntity
import com.example.lacteos_flores.data.GastosEntity
import com.example.lacteos_flores.data.ListaPreciosEntity
import com.example.lacteos_flores.data.MonedaEntity
import com.example.lacteos_flores.data.ProductosEntity
import com.example.lacteos_flores.data.TallaAuxEntity
import com.example.lacteos_flores.data.ModeloAuxEntity
import com.example.lacteos_flores.interfaz.RetrofitClient
import com.example.lacteos_flores.models.Login
import com.example.lacteos_flores.models.LoginRequest
import com.example.lacteos_flores.models.bancos
import com.example.lacteos_flores.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import kotlin.math.log

class CatalogosManager(private val db: AppDatabase) {

    private val gson = Gson()

    suspend fun sincronizarTodos(
        login: Login,
        //agregamos una lista de parametros que pueden ser nulos segun sea el caso
        lista: List<String?>,
        onProgress: (String) -> Unit,
        onResult: (Boolean, String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            // Contadores y registros para el reporte final
            var totalErrores = 0
            val reporteErrores = mutableListOf<String>()

            // ---------------------------------------------------------
            // 1. Sincronizar Documentos
            // ---------------------------------------------------------
            try {
                withContext(Dispatchers.Main) { onProgress("Sincronizando Documentos...") }
                sincronizarDocumentos(login)
                withContext(Dispatchers.Main) { onProgress("✅ Documentos actualizados") }
            } catch (e: Exception) {
                totalErrores++
                reporteErrores.add("Documentos: ${e.message}")
                withContext(Dispatchers.Main) { onProgress("❌ Falló Documentos") }
            }

            // ---------------------------------------------------------
            // 2. Sincronizar bancos
            // ---------------------------------------------------------
            try {
                withContext(Dispatchers.Main) { onProgress("Sincronizando Bancos...") }
                sincronizarBancos(login) // Tu función real aquí
                withContext(Dispatchers.Main) { onProgress("✅ Bancos Actualizados") }
            } catch (e: Exception) {
                totalErrores++
                reporteErrores.add("bancos: ${e.message}")
                withContext(Dispatchers.Main) { onProgress("❌ Falló Bancos") }
            }

            // ---------------------------------------------------------
            // 3. Sincronizar Gastos
            // ---------------------------------------------------------
            try {
                withContext(Dispatchers.Main) { onProgress("Sincronizando Proveedores...") }
                sincronizarGastos(login) // Tu función real aquí
                withContext(Dispatchers.Main) { onProgress("✅ Proveedores actualizados") }
            } catch (e: Exception) {
                totalErrores++
                reporteErrores.add("gastos: ${e.message}")
                withContext(Dispatchers.Main) { onProgress("❌ Falló Gastos") }
            }

            // ---------------------------------------------------------
            // 4. Sincronizar Productos (validar este punto si ligamos unalista a una zona o a varias zonas)
            // ---------------------------------------------------------
            try {
                withContext(Dispatchers.Main) { onProgress("Sincronizando Productos...") }
                sincronizarProductos(login, lista[0].toString()) // Tu función real aquí
                withContext(Dispatchers.Main) { onProgress("✅ Productos actualizados") }
                
                // Sincronizar Tallas y Modelos junto con productos
                withContext(Dispatchers.Main) { onProgress("Sincronizando Tallas...") }
                sincronizarTallas(login)
                withContext(Dispatchers.Main) { onProgress("Sincronizando Modelos...") }
                sincronizarModelos(login)

            } catch (e: Exception) {
                totalErrores++
                reporteErrores.add("Productos/Tallas/Modelos: ${e.message}")
                withContext(Dispatchers.Main) { onProgress("❌ Falló Productos") }
            }
            // ---------------------------------------------------------
            // 5. Sincronizar Productos Existencias
            // ---------------------------------------------------------
            try {
                withContext(Dispatchers.Main) { onProgress("Sincronizando Existencias...") }
                sincronizarProductosExist(existenciaReques(login,"15")) // Tu función real aquí
                withContext(Dispatchers.Main) { onProgress("✅ Existencias actualizados") }
            } catch (e: Exception) {
                totalErrores++
                reporteErrores.add("Lista Existencias: ${e.message}")
                withContext(Dispatchers.Main) { onProgress("❌ Falló Existencias") }
            }
            // ---------------------------------------------------------
            // 6. Sincronizar Clientes
            // ---------------------------------------------------------
            try {
                withContext(Dispatchers.Main) { onProgress("Sincronizando Clientes...") }
                sincronizarClientes(login) // Tu función real aquí
                withContext(Dispatchers.Main) { onProgress("✅ Clientes actualizados") }
            } catch (e: Exception) {
                totalErrores++
                reporteErrores.add("Lista Clientes: ${e.message}")
                withContext(Dispatchers.Main) { onProgress("❌ Falló Clientes") }
            }
            // ---------------------------------------------------------
            // 6. Sincronizar listas
            // ---------------------------------------------------------
            try {
                withContext(Dispatchers.Main) { onProgress("Sincronizando Listas...") }
                sincronizarListaPrecios(login) // Tu función real aquí
                withContext(Dispatchers.Main) { onProgress("✅ Listas actualizados") }
            } catch (e: Exception) {
                totalErrores++
                reporteErrores.add("Lista Listas: ${e.message}")
                withContext(Dispatchers.Main) { onProgress("❌ Falló Listas") }
            }
            // ---------------------------------------------------------
            // 7. Sincronizar monedas
            // ---------------------------------------------------------
            try {
                withContext(Dispatchers.Main) { onProgress("Sincronizando Monedas...") }
                sincronizarMonedas(login) // Tu función real aquí
                withContext(Dispatchers.Main) { onProgress("✅ Monedas actualizados") }
            } catch (e: Exception) {
                totalErrores++
                reporteErrores.add("Lista Monedas: ${e.message}")
                withContext(Dispatchers.Main) { onProgress("❌ Falló Monedas") }
            }
            // ---------------------------------------------------------
            // 8. Sincronizar  cartera
            // ---------------------------------------------------------
            try {
                withContext(Dispatchers.Main) { onProgress("Sincronizando Carter...") }
                sincronizarCartera(login)
                withContext(Dispatchers.Main) { onProgress("✅ Carteras actualizadas") }
            } catch (e: Exception) {
                totalErrores++
                reporteErrores.add("Carteras: ${e.message}")
                withContext(Dispatchers.Main) { onProgress("❌ Falló Carteras") }
            }

            // ---------------------------------------------------------
            // EVALUACIÓN FINAL
            // ---------------------------------------------------------
            withContext(Dispatchers.Main) {
                if (totalErrores == 0) {
                    // Todo salió perfecto
                    onResult(true, "Todos los catálogos se sincronizaron correctamente.")
                } else {
                    // Terminó, pero hubo fallos. Unimos la lista de errores para mostrarlos.
                    val mensajeFallo = "Sincronización finalizada con $totalErrores error(es):\n" +
                            reporteErrores.joinToString("\n")

                    // Pasamos 'false' para que la Activity sepa que no fue una sincronización limpia
                    onResult(false, mensajeFallo)
                }
            }
        }
    }

    // Tus funciones privadas (sincronizarDocumentos, etc.) se quedan exactamente igual
    // ya que ellas se encargan de lanzar las excepciones si algo sale mal con la red o el JSON.
    // Tu lógica original, ahora convertida en una función privada e independiente
    suspend fun sincronizarDocumentos(login: Login) {
        val request = LoginRequest(login)
        val response = RetrofitClient.apiService.getDoctos(request)

        if (!response.isSuccessful) {
            throw Exception("Error servidor Documentos: ${response.code()}")
        }

        val listaRaw =
            response.body()?.ResponseDocumentos ?: throw Exception("Respuesta vacía de Documentos")

        if (listaRaw.size > 1) {
            val primerObjeto = gson.toJson(listaRaw[0])

            if (primerObjeto.contains("\"ok\":\"1\"")) {
                val listaParaGuardar = mutableListOf<DoctosEntity>()

                for (i in 1 until listaRaw.size) {
                    val jsonElement = gson.toJsonTree(listaRaw[i])
                    val doc = gson.fromJson(jsonElement, documentos::class.java)

                    listaParaGuardar.add(
                        DoctosEntity(
                            gen = doc.gen.toString(),
                            nat = doc.nat.toString(),
                            grp = doc.grp.toString(),
                            tipo = doc.tipo.toString(),
                            descripcion = doc.descrp.toString(),
                            isr = doc.isr.toString(),
                            iva = doc.iva.toString(),
                            retenido = doc.ret.toString()
                        )
                    )
                }

                db.doctosDao().eliminarTodo()
                db.doctosDao().insertarDocumentos(listaParaGuardar)
            } else {
                throw Exception("El WS de Documentos no devolvió ok:1")
            }
        }
        
        // Sincronizar también ventas pendientes
        enviarVentasPendientes(login)
    }

    suspend fun enviarCobrosPendientes(login: Login) {
        val pendientes = db.kdm1Dao().obtenerMovimientos().filter { it.staSinc == "N" && it.gen == "U" && it.nat == "A" && it.grp == "5" }
        for (cobro in pendientes) {
            enviarCobro(cobro.id, login)
        }
    }

    suspend fun enviarVentasPendientes(login: Login) {
        val pendientes = db.kdm1Dao().obtenerMovimientos().filter { it.staSinc == "N" && it.grp != "5" }
        println("pendientes${pendientes.size}")
        for (venta in pendientes) {
            enviarVenta(venta.id, login)
        }
    }

    suspend fun enviarVenta(iddoc: Long, login: Login) {
        val doc = db.kdm1Dao().obtenerDocumentoPorId(iddoc) ?: return
        val cliente = db.clientsDao().obtenerCliente(doc.cliente)
        val partidas = db.kdm2Dao().obtenerPartidas(iddoc)
        val auxiliares = db.itemAuxDao().obtenerAuxiliares(iddoc)

        val itemsDocList = partidas.map { p ->
            val auxList = auxiliares.filter { it.partida == p.partida && it.producto == p.producto && it.auxiliar != "-"}.map { a ->

                val caduc= db.existenciasDao().obtenerLoteEspecifico(p.producto,a.auxiliar)
                com.example.lacteos_flores.models.ItemsAuxiliar(
                    serie = a.auxiliar,
                    cant = a.cantidad,
                    caduca = caduc?.fecha ?: "",
                    talla = a.talla,
                    modelo = a.modelo,
                    color = a.color,
                    ubicacion = "-"
                )

            }
            println("auxiliar: $auxList")
            com.example.lacteos_flores.models.ItemsDoc(
                kparte = p.producto,
                cant = p.cantidad,
                descri = p.descrip,
                uni = p.unidad,
                precio = p.precio,
                monto = p.importe,
                coment = "",
                iva = p.iva,
                ieps = "0.0",
                desc = "0",
                itemAux = if (auxList.isNotEmpty() ) auxList else null
            )
        }

        val request = com.example.lacteos_flores.models.AltaDoctosRequest(
            login = login,
            rfcEmpresa = "PLF010228TC3",
            suc = doc.suc,
            alm = doc.alm,
            gen = doc.gen,
            nat = doc.nat,
            grp = doc.grp,
            tipo = doc.tip,
            fecha = doc.fecha,
            claveCliente = doc.cliente,
            moneda = doc.moneda,
            paridad = doc.pari,
            rfcCliente = doc.rfc,
            nombreCliente = cliente?.nombre ?: "",
            ieps = "0.0",
            iva = doc.iva,
            refer = "-",
            comenta = "",
            monto = doc.monto,
            plazo = "0",
            vence = doc.venc,
            cond = doc.condi,
            agente = doc.agent,
            lati = doc.lati,
            longi = doc.long,
            items = itemsDocList
        )

        //imprimimos el json enviado en el request
        val jsonRequest = gson.toJson(request)
        println("reques:$jsonRequest")
        val response = RetrofitClient.apiService.sendDoctos(request)
        if (response.isSuccessful) {
            val body = response.body()
            val result = body?.ResponseAlta?.firstOrNull()
            //imprimimos el json enviado
            println("result $result")
            if (result?.ok == "1") {
                db.kdm1Dao().actualizarSincronizacion(iddoc, "S", result.doc ?: result.folio)
            } else {
                throw Exception("Kepler Error: ${result?.msn}")
            }
        } else {
            throw Exception("HTTP Error: ${response.code()}")
        }
    }

    suspend fun enviarCobro(iddoc: Long, login: Login) {
        val doc = db.kdm1Dao().obtenerDocumentoPorId(iddoc) ?: return
        val partidas = db.kdm2cxcDao().obtenerPartidasPorDoc(iddoc)

        val cobrosList = partidas.map { p ->
            // Parsing folio string: UD0701-0000039
            val doctoStr = p.doctoAfectado
            val nat = if (doctoStr.isNotEmpty()) doctoStr[1].toString() else ""
            val grp = doctoStr.substring(2,4)
            val tip =doctoStr.substring(4,6)
            val folio = if (doctoStr.contains("-")) doctoStr.substringAfter("-") else doctoStr

            CobroItem(
                vence = p.fecha, // Placeholder
                refer = p.referencia.ifEmpty { "-" },
                iva = "0.0", // Placeholder
                docto = p.doctoAfectado,
                saldom = p.saldoAnt,
                descr = p.descri,
                montoOrig = p.montoDocto,
                saldo = p.saldoAnt,
                monto = p.abono,
                nat = nat,
                grp = grp,
                tipo = tip,
                folio = folio,
                fecha = p.fecha
            )
        }

        val request = AltaDoctosRequest(
            login = login,
            suc = doc.suc,
            alm = doc.alm,
            gen = doc.gen,
            nat = doc.nat,
            grp = doc.grp,
            tipo = doc.tip,
            fecha = doc.fecha,
            moneda = doc.moneda,
            paridad = doc.pari,
            comenta = "-",
            monto = doc.monto,
            agente = doc.agent,
            cobros = cobrosList,
            rfcCliente = doc.rfc,
            claveCliente = doc.cliente,
            items = null
        )

        val jsonRequest = gson.toJson(request)
        println("DEBUG ENVIAR COBRO REQUEST: $jsonRequest")
        val response = RetrofitClient.apiService.sendDoctos(request)
        if (response.isSuccessful) {
            val body = response.body()
            println("DEBUG ENVIAR COBRO RESPONSE: ${gson.toJson(body)}")
            val result = body?.ResponseAlta?.firstOrNull()
            if (result?.ok == "1") {
                db.kdm1Dao().actualizarSincronizacion(iddoc, "S", result.doc ?: result.folio)
            } else {
                throw Exception("Kepler Error: Documento no Creado!")
            }
        } else {
            throw Exception("HTTP Error: ${response.code()}")
        }
    }

    suspend fun enviarGasto(gasto: GastoRegistradoEntity, login: Login) {
        val request = AltaGastoRequest(
            login = login,
            rfc = "PLF010228TC3",
            vendedor = gasto.usuario,
            gasto = gasto.tipoGasto, // Debería ser la clave
            monto = gasto.monto.toString(),
            observaciones = gasto.comentario
        )

        val jsonRequest = gson.toJson(request)
        println("DEBUG ENVIAR GASTO REQUEST: $jsonRequest")
        val response = RetrofitClient.apiService.sendAltaGasto(request)
        if (response.isSuccessful) {
            val body = response.body()
            println("DEBUG ENVIAR GASTO RESPONSE: ${gson.toJson(body)}")
            val result = body?.Registra_GastosResponse?.firstOrNull()
            println("result:"+result)
            if (result?.ok == "1") {
                db.gastoRegistradoDao().marcarComoSincronizado(gasto.id)
            } else {
                throw Exception("Kepler Error")

            }
        } else {
            throw Exception("HTTP Error: ${response.code()}")
        }
    }

    suspend fun sincronizarBancos(login: Login) {
        // ... Aquí clonas la lógica adaptada para tu catálogo de bancos ...
        val request = LoginRequest(login)
        val response = RetrofitClient.apiService.getBancos(request)

        if (!response.isSuccessful) {
            throw Exception("Error servidor bancos: ${response.code()}")
        }

        val listaRaw =
            response.body()?.BancosResponse ?: throw Exception("Respuesta vacía de Bancos")

        if (listaRaw.size > 1) {
            val primerObjeto = gson.toJson(listaRaw[0])

            if (primerObjeto.contains("\"ok\":\"1\"")) {
                val listaParaGuardar = mutableListOf<BancoEntity>()

                for (i in 1 until listaRaw.size) {
                    val jsonElement = gson.toJsonTree(listaRaw[i])
                    val banco = gson.fromJson(jsonElement, bancos::class.java)

                    listaParaGuardar.add(
                        BancoEntity(
                            clave = banco.cve.toString(),
                            banco = banco.ban.toString()
                        )
                    )
                }

                db.bancoDao().eliminarTodo()
                db.bancoDao().insertarBanco(listaParaGuardar)
            } else {
                throw Exception("El WS de bancos no devolvió ok:1")
            }
        }
    }

    suspend fun sincronizarGastos(login: Login) {
        // ... Aquí clonas la lógica adaptada para tu catálogo de bancos ...
        val request = LoginRequest(login)
        val response = RetrofitClient.apiService.getGastos(request)

        if (!response.isSuccessful) {
            throw Exception("Error servidor bancos: ${response.code()}")
        }

        val listaRaw =
            response.body()?.Cat_gastosResponse ?: throw Exception("Respuesta vacía de Gastos")

        if (listaRaw.size > 1) {
            val primerObjeto = gson.toJson(listaRaw[0])

            if (primerObjeto.contains("\"ok\":\"1\"")) {
                val listaParaGuardar = mutableListOf<GastosEntity>()

                for (i in 1 until listaRaw.size) {
                    val jsonElement = gson.toJsonTree(listaRaw[i])
                    val gasto = gson.fromJson(jsonElement, gastos::class.java)

                    listaParaGuardar.add(
                        GastosEntity(
                            clave = gasto.cve.toString(),
                            descripcion = gasto.descrp.toString()
                        )
                    )
                }

                db.gastosDao().eliminarTodo()
                db.gastosDao().insertarGasto(listaParaGuardar)
            } else {
                throw Exception("El WS de bancos no devolvió ok:1")
            }
        }
        
        // Sincronizar también gastos registrados pendientes
        enviarGastosPendientes(login)
    }

    suspend fun enviarGastosPendientes(login: Login) {
        val pendientes = db.gastoRegistradoDao().obtenerGastosPendientes()
        for (gasto in pendientes) {
            enviarGasto(gasto, login)
        }
    }

    //sincronizarProductos
    suspend fun sincronizarProductos(login: Login, lista: String) {
        // ... Aquí clonas la lógica adaptada para tu catálogo de bancos ...
        val request = ProductosRequest(login, lista)
        val response = RetrofitClient.apiService.getProductos(request)

        println("DEBUG PRODUCTOS1 EXIST REQUEST: $request")
        if (!response.isSuccessful) {
            throw Exception("Error servidor productos: ${response.code()}")
        }


        val listaRaw =
            response.body()?.ResponseProductos ?: throw Exception("Respuesta vacía de Productos")
        println("DEBUG PRODUCTOS1 EXIST RESPONSE: ${gson.toJson(listaRaw)}")
        if (listaRaw.size > 1) {
            val primerObjeto = gson.toJson(listaRaw[0])

            if (primerObjeto.contains("\"ok\":\"1\"")) {
                val listaParaGuardar = mutableListOf<ProductosEntity>()

                for (i in 1 until listaRaw.size) {
                    val jsonElement = gson.toJsonTree(listaRaw[i])
                    val prod = gson.fromJson(jsonElement, ItemProductos::class.java)

                    listaParaGuardar.add(
                        ProductosEntity(
                            clave = prod.cve.toString(),
                            descripcion = prod.name.toString(),
                            cb = prod.cb.toString(),
                            unidad = prod.uni.toString(),
                            unidadalt = prod.uni.toString(),
                            precio1 = prod.precio.toString(),
                            precio2 = prod.precio.toString(),
                            precio3 = prod.precio.toString(),
                            precio4 = prod.precio.toString(),
                            iva = prod.iva.toString(),
                            ieps = prod.iva.toString(),
                            ubicaalm = prod.ubicalm.toString(),
                            serie = prod.serie.toString(),
                            lotesf = prod.lote.toString(),
                            tmc = prod.tmc.toString(),
                            ubicacionn = prod.ubicacion.toString(),
                            pedimento = prod.pedimento.toString(),
                            existencia = 0.0
                        )
                    )
                }

                db.productosDao().eliminarTodo()
                db.productosDao().insertarProductos(listaParaGuardar)
                
                // También sincronizamos Tallas y Modelos aquí para las llamadas individuales desde el botón Productos
                sincronizarTallas(login)
                sincronizarModelos(login)


            } else {
                throw Exception("El WS de bancos no devolvió ok:1")
            }
        }
    }

    //existencias sin control auxiliar
    suspend fun sincronizarProductosExist(login: existenciaReques) {
        // ... Aquí clonas la lógica adaptada para tu catálogo de bancos ...
        val request = login
        val jsonEnviado = gson.toJson(request)
        println("DEBUG PRODUCTOS EXIST REQUEST: $jsonEnviado")
        val response = RetrofitClient.apiService.getExistencias(request)
        if (!response.isSuccessful) {
            throw Exception("Error servidor existencias: ${response.code()}")
        }

        val body = response.body()
        println("DEBUG PRODUCTOS EXIST RESPONSE: ${gson.toJson(body)}")
        val listaRaw = body?.ResponseExistencia ?: throw Exception("Respuesta vacía de Existencias")

        if (listaRaw.size > 1) {
            val primerObjeto = gson.toJson(listaRaw[0])
            println("DEBUG primerObjeto existencias: $primerObjeto")
            if (primerObjeto.contains("\"ok\":\"1\"")) {
                val listaParaGuardar = mutableListOf<ExistenciaEntity>()

                for (i in 1 until listaRaw.size) {
                    val jsonElement = gson.toJsonTree(listaRaw[i])
                    val prod = gson.fromJson(jsonElement, existencia::class.java)
    
                    
                    if (login.tip == "16"){
                        // Actualiza o inserta nueva existencia (Upsert)
                        println("tipo de documento:${login.tip}")
                        db.existenciasDao().sumarOInsertar(
                            ExistenciaEntity(
                                clave = prod.cve.toString(),
                                auxiliar = prod.aux.toString(),
                                existencias = prod.exist.toString(),
                                fecha = prod.fec.toString(),
                                talla = prod.talla ?: "-",
                                modelo = prod.modelo ?: "-",
                                color = prod.color ?: "-"
                            )
                        )
                       // db.existenciasDao().actualizarExistAux(prod.cve.toString(),prod.aux.toString(),prod.exist.toString(),prod.talla.toString(),prod.modelo.toString(),prod.color.toString())
                    }else {
                        listaParaGuardar.add(
                            ExistenciaEntity(
                                clave = prod.cve.toString(),
                                auxiliar = prod.aux.toString(),
                                existencias = prod.exist.toString(),
                                fecha = prod.fec.toString(),
                                talla = prod.talla ?: "-",
                                modelo = prod.modelo ?: "-",
                                color = prod.color ?: "-"
                            )
                        )
                    }

                }
               
                if(login.tip == "15"){
                    println("tipo de documento:${login.tip}")
                    db.existenciasDao().eliminarTodo()
                    db.existenciasDao().insertarExistencias(listaParaGuardar)
                }


            } else {
                throw Exception("El WS de existencias no devolvió ok:1")
            }
        }
    }

    suspend fun sincronizarNuevaExistencia(login: Login) {
        // 1. Valida Recarga
        /*val requestValida = LoginRequest(login)
        val jsonRequestValida = gson.toJson(requestValida)
        println("DEBUG VALIDA RECARGA REQUEST: $jsonRequestValida")
        val responseValida = RetrofitClient.apiService.validaRecarga(requestValida)
        if (!responseValida.isSuccessful) {
            throw Exception("Error servidor ValidaRecarga: ${responseValida.code()}")
        }
        val bodyValida = responseValida.body()
        println("DEBUG VALIDA RECARGA RESPONSE: ${gson.toJson(bodyValida)}")
        val resultValida = bodyValida?.ValidaRecargaResponse?.firstOrNull()
        if (resultValida?.ok != "1") {
            throw Exception("Error ValidaRecarga: ${resultValida?.msn}")
        }*/

        // 2. Enviar movimientos pendientes (ventas, cobros, gastos)
        // Usamos las funciones que ahora lanzan excepciones
        //enviarVentasPendientes(login)
        //enviarCobrosPendientes(login)
        //enviarGastosPendientes(login)

        // 3. Sincronizar Existencias (Tipo "16" como ejemplo de uso previo)
        sincronizarProductosExist(existenciaReques(login, "16"))
    }

    //existencias control auxiliar validar si usar para las recargas o no utilizarlo
    /*  private suspend fun sincronizarProductosExistAux(login: Login) {
        // ... Aquí clonas la lógica adaptada para tu catálogo de bancos ...
        val request = LoginRequest(login)
        val response = RetrofitClient.apiService.getAuxiliares(request)

        if (!response.isSuccessful) {
            throw Exception("Error servidor existencias: ${response.code()}")
        }

        val listaRaw = response.body()?.ControlAuxResponse ?: throw Exception("Respuesta vacía de Existencias")

        if (listaRaw.size > 1) {
            val primerObjeto = gson.toJson(listaRaw[0])

            if (primerObjeto.contains("\"ok\":\"1\"")) {
                val listaParaGuardar = mutableListOf<ExistenciaEntity>()

                for (i in 1 until listaRaw.size) {
                    val jsonElement = gson.toJsonTree(listaRaw[i])
                    val prod = gson.fromJson(jsonElement, existencia::class.java)

                    listaParaGuardar.add(ExistenciaEntity(
                        clave = prod.cve.toString(),
                        auxiliar = prod.aux.toString(),
                        existencias = prod.exist.toString(),
                        fecha = prod.fec.toString(),
                        talla = prod.talla ?: "-",
                        modelo = prod.modelo ?: "-",
                        color = prod.color ?: "-"
                    ))
                }

                db.existenciasDao().eliminarTodo()
                db.existenciasDao().insertarExistencias(listaParaGuardar)
            } else {
                throw Exception("El WS de existencias no devolvió ok:1")
            }
        }
    }*/

    //sincronizarListaPrecios
    suspend fun sincronizarListaPrecios(login: Login) {
        // ... Aquí clonas la lógica adaptada para tu catálogo de bancos ...
        val request = LoginRequest(login)
        val response = RetrofitClient.apiService.getListaProd(request)

        if (!response.isSuccessful) {
            throw Exception("Error servidor productos: ${response.code()}")
        }

        val listaRaw =
            response.body()?.ListaPreciosResponse ?: throw Exception("Respuesta vacía de Bancos")

        if (listaRaw.size > 1) {
            val primerObjeto = gson.toJson(listaRaw[0])

            if (primerObjeto.contains("\"ok\":\"1\"")) {
                val listaParaGuardar = mutableListOf<ListaPreciosEntity>()

                for (i in 1 until listaRaw.size) {
                    val jsonElement = gson.toJsonTree(listaRaw[i])
                    val list = gson.fromJson(jsonElement, listapr::class.java)//id lista de precios
                    val idLista = list.cvelist
                    // recorremos productos
                    list.Productos?.forEach { producto ->
                        listaParaGuardar.add(
                            ListaPreciosEntity(
                                listaid = idLista.toString(),
                                clave = producto.cve.toString(),
                                precio = producto.precio.toString(),
                                unidad = producto.uni.toString(),
                                comentario = producto.come.toString(),
                                zona = producto.zona.toString()
                            )
                        )
                    }

                }

                db.listaPreciosDao().eliminarTodo()
                db.listaPreciosDao().insertarListasPrecios(listaParaGuardar)
            } else {
                throw Exception("El WS de bancos no devolvió ok:1")
            }
        }
    }
    //sincronizacion de clientes
    suspend fun sincronizarClientes(login: Login) {
        // ... Aquí clonas la lógica adaptada para tu catálogo de bancos ...
        val request = LoginRequest(login)
        val response = RetrofitClient.apiService.getClientes(request)
        val jsonEnviado = Gson().toJson(request)
        println("DEBUG JSON ENVIADO: $jsonEnviado")
        println("DEBUG RESPONSE: $response")
        if (!response.isSuccessful) {
            println("DEBUG RESPONSE THROW: ${response.code()}")
            throw Exception("Error servidor cleintes: ${response.code()}")
        }

        val listaRaw =
            response.body()?.GetClientesResponse ?: throw Exception("Respuesta vacía de clientes")
        println("DEBUG RESPONSE list: $listaRaw")
        if (listaRaw.size > 1) {
            val primerObjeto = gson.toJson(listaRaw[0])

            if (primerObjeto.contains("\"ok\":\"1\"")) {
                val listaParaGuardar = mutableListOf<ClientsEntity>()

                for (i in 1 until listaRaw.size) {
                    val jsonElement = gson.toJsonTree(listaRaw[i])
                    val cli = gson.fromJson(jsonElement, cliente::class.java)


                    listaParaGuardar.add(
                        ClientsEntity(
                            clave = cli.cve.toString(),
                            nombre = cli.name.toString(),
                            rfc = cli.rfc.toString(),
                            limcre = cli.limcre.toString(),
                            plazo = cli.plazo.toString(),
                            calle = cli.calle.toString(),
                            colo = cli.colo.toString(),
                            pobl = cli.pobl.toString(),
                            tel = cli.tel.toString(),
                            cp = cli.cp.toString(),
                            agente = cli.agent.toString(),
                            latitud = cli.latitud.toString(),
                            longitud = cli.longitud.toString(),
                            flunes = cli.flunes.toString(),
                            fmartes = cli.fmartes.toString(),
                            fmiercoles = cli.fmiercoles.toString(),
                            fjueves = cli.fjueves.toString(),
                            fviernes = cli.fviernes.toString(),
                            fsabado = cli.fsabado.toString(),
                            fdomingo = cli.fdomingo.toString(),
                            lunes = cli.lunes.toString(),
                            martes = cli.martes.toString(),
                            miercoles = cli.miercoles.toString(),
                            jueves = cli.jueves.toString(),
                            viernes = cli.viernes.toString(),
                            sabado = cli.sabado.toString(),
                            domingo = cli.domingo.toString(),
                            descuentop = cli.descuentop.toString(),
                            comentarios = cli.come.toString(),
                            listaprecio = cli.lprecio.toString()
                        )
                    )
                }

                db.clientsDao().eliminarTodo()
                db.clientsDao().insertarClientes(listaParaGuardar)
            } else {
                throw Exception("El WS de bancos no devolvió ok:1")
            }
        }
    }
    //monedas
    suspend fun sincronizarMonedas(login: Login) {
        // ... Aquí clonas la lógica adaptada para tu catálogo de bancos ...
        val request = LoginRequest(login)
        val response = RetrofitClient.apiService.getParidades(request)

        if (!response.isSuccessful) {
            throw Exception("Error servidor productos: ${response.code()}")
        }

        val listaRaw = response.body()?.ResponseParidades ?: throw Exception("Respuesta vacía de Bancos")

        if (listaRaw.size > 1) {
            val primerObjeto = gson.toJson(listaRaw[0])

            if (primerObjeto.contains("\"ok\":\"1\"")) {
                val listaParaGuardar = mutableListOf<MonedaEntity>()

                for (i in 1 until listaRaw.size) {
                    val jsonElement = gson.toJsonTree(listaRaw[i])
                    val mone = gson.fromJson(jsonElement, paridades::class.java)

                    listaParaGuardar.add(MonedaEntity(
                        moneda = mone.mon.toString(),
                        fecha = mone.fecha.toString(),
                        hora = mone.hora.toString(),
                        paridad = mone.paridad.toString()
                    ))
                }

                db.monedaDao().eliminarTodo()
                db.monedaDao().insertar(listaParaGuardar)
            } else {
                throw Exception("El WS de bancos no devolvió ok:1")
            }
        }
    }
    suspend fun sincronizarCartera(login: Login) {
        val request = LoginRequest(login)
        val response = RetrofitClient.apiService.getCartera(request)

        if (!response.isSuccessful) {
            throw Exception("Error servidor Documentos: ${response.code()}")
        }

        val listaRaw =
            response.body()?.CarteraResponse  ?: throw Exception("Respuesta vacía de Cartera")

        if (listaRaw.size > 1) {
            val primerObjeto = gson.toJson(listaRaw[0])

            if (primerObjeto.contains("\"ok\":\"1\"")) {
                val listaParaGuardar = mutableListOf<CarteraEntity>()

                for (i in 1 until listaRaw.size) {
                    val jsonElement = gson.toJsonTree(listaRaw[i])
                    val doc = gson.fromJson(jsonElement, itemCartera::class.java)

                    listaParaGuardar.add(
                        CarteraEntity(
                            cli = doc.cli.toString()
                            ,docto = doc.docto.toString()
                            ,monto = doc.monto.toString()
                            ,saldo = doc.saldo.toString()
                            ,fecha = doc.fecha.toString()
                            ,credito = doc.credito.toString()
                            ,abono = doc.abono.toString()
                            ,dias = doc.dias.toString()

                        )
                    )
                    println("factura $listaParaGuardar")
                }

                db.carteraDao().eliminarTodo()
                db.carteraDao().insertaCartera(listaParaGuardar)
            } else {
                throw Exception("El WS de Documentos no devolvió ok:1")
            }
        }
    }

    suspend fun enviarTodoYLimpiar(login: Login): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Enviar Ventas, Devoluciones y Descargas
                val movimientos = db.kdm1Dao().obtenerMovimientos().filter { it.staSinc == "N" }
                for (doc in movimientos) {
                    // Diferenciamos Cobros de Ventas/Dev/Desc
                    if (doc.gen == "U" && doc.nat == "A" && doc.grp == "5") {
                        enviarCobro(doc.id, login)
                    } else {
                        enviarVenta(doc.id, login)
                    }
                }

                // 2. Enviar Gastos
                val pendientesGastos = db.gastoRegistradoDao().obtenerGastosPendientes()
                for (gasto in pendientesGastos) {
                    enviarGasto(gasto, login)
                }

                // 3. Limpiar tablas locales
                limpiarTablasLocales()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private suspend fun limpiarTablasLocales() {
        db.kdm1Dao().eliminarTodoMovimiento()
        db.kdm2Dao().eliminarTodoKdm2()
        db.gastoRegistradoDao().eliminarTodoGastosRegistrados()
        db.kdm2cxcDao().eliminarTodoKdm2cxc()
        db.carteraDao().eliminarTodo()
        db.existenciasDao().eliminarTodo()
        db.itemAuxDao().eliminarTodoItemAux()
    }

    suspend fun sincronizarTallas(login: Login) {
        val request = CatTmcRequest(login, "KDIS7")
        val response = RetrofitClient.apiService.getCatTmc(request)

        println("entra a catalogo tallas")
        if (!response.isSuccessful) {
            throw Exception("Error servidor Tallas: ${response.code()}")
        }

        val listaRaw = response.body()?.responseCatTmc ?: throw Exception("Respuesta vacía de Tallas")

        if (listaRaw.size > 1) {
            val primerObjeto = listaRaw[0]
            if (primerObjeto["ok"] == "1") {
                val listaParaGuardar = mutableListOf<TallaAuxEntity>()
                for (i in 1 until listaRaw.size) {
                    val item = listaRaw[i]
                    listaParaGuardar.add(
                        TallaAuxEntity(
                            clave = item["Clave"] ?: "",
                            descripcion = item["Descripcion"] ?: ""
                        )
                    )
                }
                db.tmcDao().eliminarTallas()
                db.tmcDao().insertarTallas(listaParaGuardar)
            } else {
                throw Exception("El WS de Tallas no devolvió ok:1")
            }
        }
    }

    suspend fun sincronizarModelos(login: Login) {
        val request = CatTmcRequest(login, "KDIS8")
        val response = RetrofitClient.apiService.getCatTmc(request)

        println("entra a catalogo modelo")
        if (!response.isSuccessful) {
            throw Exception("Error servidor Modelos: ${response.code()}")
        }

        val listaRaw = response.body()?.responseCatTmc ?: throw Exception("Respuesta vacía de Modelos")

        if (listaRaw.size > 1) {
            val primerObjeto = listaRaw[0]
            if (primerObjeto["ok"] == "1") {
                val listaParaGuardar = mutableListOf<ModeloAuxEntity>()
                for (i in 1 until listaRaw.size) {
                    val item = listaRaw[i]
                    listaParaGuardar.add(
                        ModeloAuxEntity(
                            clave = item["Clave"] ?: "",
                            descripcion = item["Descripcion"] ?: ""
                        )
                    )
                }
                db.tmcDao().eliminarModelos()
                db.tmcDao().insertarModelos(listaParaGuardar)
            } else {
                throw Exception("El WS de Modelos no devolvió ok:1")
            }
        }
    }
}
