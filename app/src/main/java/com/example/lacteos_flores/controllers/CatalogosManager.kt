package com.example.lacteos_flores.controllers

import com.example.lacteos_flores.data.AppDatabase
import com.example.lacteos_flores.data.BancoEntity
import com.example.lacteos_flores.data.ClientsEntity
import com.example.lacteos_flores.data.DoctosEntity
import com.example.lacteos_flores.data.ExistenciaEntity
import com.example.lacteos_flores.data.GastosEntity
import com.example.lacteos_flores.data.ListaPreciosEntity
import com.example.lacteos_flores.data.MonedaEntity
import com.example.lacteos_flores.data.ProductosEntity
import com.example.lacteos_flores.interfaz.RetrofitClient
import com.example.lacteos_flores.models.Login
import com.example.lacteos_flores.models.LoginRequest
import com.example.lacteos_flores.models.bancos
import com.example.lacteos_flores.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson

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
            } catch (e: Exception) {
                totalErrores++
                reporteErrores.add("Lista productos: ${e.message}")
                withContext(Dispatchers.Main) { onProgress("❌ Falló Productos") }
            }
            // ---------------------------------------------------------
            // 5. Sincronizar Productos Existencias
            // ---------------------------------------------------------
            try {
                withContext(Dispatchers.Main) { onProgress("Sincronizando Existencias...") }
                sincronizarProductosExist(login) // Tu función real aquí
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
            // 6. Sincronizar Clientes
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
    private suspend fun sincronizarDocumentos(login: Login) {
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
    }

    private suspend fun sincronizarBancos(login: Login) {
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

    private suspend fun sincronizarGastos(login: Login) {
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
    }

    //sincronizarProductos
    private suspend fun sincronizarProductos(login: Login, lista: String) {
        // ... Aquí clonas la lógica adaptada para tu catálogo de bancos ...
        val request = ProductosRequest(login, lista)
        val response = RetrofitClient.apiService.getProductos(request)

        if (!response.isSuccessful) {
            throw Exception("Error servidor productos: ${response.code()}")
        }

        val listaRaw =
            response.body()?.ResponseProductos ?: throw Exception("Respuesta vacía de Bancos")

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
            } else {
                throw Exception("El WS de bancos no devolvió ok:1")
            }
        }
    }

    //existencias sin control auxiliar
    private suspend fun sincronizarProductosExist(login: Login) {
        // ... Aquí clonas la lógica adaptada para tu catálogo de bancos ...
        val request = LoginRequest(login)
        val response = RetrofitClient.apiService.getExistencias(request)
        val jsonEnviado = com.google.gson.Gson().toJson(request)
        println("DEBUG JSON ENVIADO existencas: $jsonEnviado")
        println("DEBUG RESPONSE existencias: $response")
        if (!response.isSuccessful) {
            throw Exception("Error servidor existencias: ${response.code()}")
        }

        val listaRaw =
            response.body()?.ResponseExistencia ?: throw Exception("Respuesta vacía de Existencias")

        if (listaRaw.size > 1) {
            val primerObjeto = gson.toJson(listaRaw[0])
            println("DEBUG primerObjeto existencias: $primerObjeto")
            if (primerObjeto.contains("\"ok\":\"1\"")) {
                val listaParaGuardar = mutableListOf<ExistenciaEntity>()

                for (i in 1 until listaRaw.size) {
                    val jsonElement = gson.toJsonTree(listaRaw[i])
                    val prod = gson.fromJson(jsonElement, existencia::class.java)

                    listaParaGuardar.add(
                        ExistenciaEntity(
                            clave = prod.cve.toString(),
                            auxiliar = prod.aux.toString(),
                            existencias = prod.exist.toString()
                        )
                    )
                }

                db.existenciasDao().eliminarTodo()
                db.existenciasDao().insertarExistencias(listaParaGuardar)
            } else {
                throw Exception("El WS de existencias no devolvió ok:1")
            }
        }
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
                        existencias = prod.exist.toString()
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
    private suspend fun sincronizarListaPrecios(login: Login) {
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
    private suspend fun sincronizarClientes(login: Login) {
        // ... Aquí clonas la lógica adaptada para tu catálogo de bancos ...
        val request = LoginRequest(login)
        val response = RetrofitClient.apiService.getClientes(request)
        val jsonEnviado = com.google.gson.Gson().toJson(request)
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
    private suspend fun sincronizarMonedas(login: Login) {
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
}