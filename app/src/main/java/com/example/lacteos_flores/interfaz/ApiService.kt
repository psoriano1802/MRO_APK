package com.example.lacteos_flores.interfaz

import com.example.lacteos_flores.models.ubicacionRequest
import com.example.lacteos_flores.models.IniciaDiaResponse
import com.example.lacteos_flores.models.AltaDoctosRequest
import com.example.lacteos_flores.models.AltaDoctosResponse
import com.example.lacteos_flores.models.DocumentosResponse
import com.example.lacteos_flores.models.FolioResponse
import com.example.lacteos_flores.models.LoginRequest
import com.example.lacteos_flores.models.LoginResponse
import com.example.lacteos_flores.models.OrdenesRequest
import com.example.lacteos_flores.models.OrdenesResponse
import com.example.lacteos_flores.models.PantallasResponse
import com.example.lacteos_flores.models.PaquetesRequest
import com.example.lacteos_flores.models.PaquetesResponse
import com.example.lacteos_flores.models.ProductosRequest
import com.example.lacteos_flores.models.ProductosResponse
import com.example.lacteos_flores.models.ReporteFallaRequest
import com.example.lacteos_flores.models.ReporteFallaResponse
import com.example.lacteos_flores.models.SucursalResponse
import com.example.lacteos_flores.models.TecnicosResponse
import com.example.lacteos_flores.models.TerminaDiaResponse
import com.example.lacteos_flores.models.TrabajosResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("SOAPAction:login")
    @POST("/api_kepler_lacteos") // login lacteos
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    //iniciar jornada
    @Headers("SOAPAction:iniciaDia")
    @POST("/api_kepler_lacteos") // iniciar dia de labores
    suspend fun sendIniDia(@Body request: ubicacionRequest): Response<IniciaDiaResponse>

    //tipo de activos
    @Headers("SOAPAction:terminaDia")
    @POST("/api_kepler_lacteos") // reemplaza con la URL relativa correcta
    suspend fun sendTerDia(@Body request: ubicacionRequest): Response<TerminaDiaResponse>

    //tipos de trabajos
    @Headers("SOAPAction:ValidaDia")
    @POST("/api_kepler_mro") // reemplaza con la URL relativa correcta
    suspend fun getTipoTrabajo(@Body request: LoginRequest): Response<TrabajosResponse>

    //Folio actual
    @Headers("SOAPAction:Folios")
    @POST("/api_kepler_mro") // reemplaza con la URL relativa correcta
    suspend fun getFolio(@Body request: LoginRequest): Response<FolioResponse>

    //alta de reporte de falla
    @Headers("SOAPAction:Alta_Reporte_Falla")
    @POST("/api_kepler_mro") // reemplaza con la URL relativa correcta
    suspend fun sendReporteFalla(@Body request: ReporteFallaRequest): Response<ReporteFallaResponse>

    //ordenes asignadas
    @Headers("SOAPAction:Cat_OrdenesA")
    @POST("/api_kepler_mro") // reemplaza con la URL relativa correcta
    suspend fun getOrdenes(@Body request: OrdenesRequest): Response<OrdenesResponse>

    //Sucursales
    @Headers("SOAPAction:Cat_Sucursal")
    @POST("/api_kepler_mro") // reemplaza con la URL relativa correcta
    suspend fun getSucursales(@Body request: LoginRequest): Response<SucursalResponse>

    //paquetes
    @Headers("SOAPAction:Cat_Paquetes")
    @POST("/api_kepler_mro") // reemplaza con la URL relativa correcta
    suspend fun getPaquetes(@Body request: PaquetesRequest): Response<PaquetesResponse>

    //productos pueden ser refacciones(tipo 1) o mano de obra(tipo 2)
    @Headers("SOAPAction:Cat_Productos")
    @POST("/api_kepler_mro") // reemplaza con la URL relativa correcta
    suspend fun getProductos(@Body request: ProductosRequest): Response<ProductosResponse>

    //pantallas validar si es necesario usar
    @Headers("SOAPAction:Cat_Pantallas")
    @POST("/api_kepler_mro") // reemplaza con la URL relativa correcta
    suspend fun getPantallas(@Body request: LoginRequest): Response<PantallasResponse>

    //tecnicos
    @Headers("SOAPAction:Cat_Tecnicos")
    @POST("/api_kepler_mro") // reemplaza con la URL relativa correcta
    suspend fun getTecnicos(@Body request: ubicacionRequest): Response<TecnicosResponse>

    //envio de documentos al servidor
    @Headers("SOAPAction:Alta_Mano_Refaccion")
    @POST("/api_kepler_mro") // reemplaza con la URL relativa correcta
    suspend fun sendDoctos(@Body request: AltaDoctosRequest): Response<AltaDoctosResponse>

    //documentos
    @Headers("SOAPAction:Cat_Documentos")
    @POST("/api_kepler_mro") // reemplaza con la URL relativa correcta
    suspend fun getDocumentos(@Body request: LoginRequest): Response<DocumentosResponse>

}
