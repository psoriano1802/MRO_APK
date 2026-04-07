package com.example.agroag_mro.interfaz

import com.example.agroag_mro.models.ActivoRequest
import com.example.agroag_mro.models.ActivosResponse
import com.example.agroag_mro.models.AltaDoctosRequest
import com.example.agroag_mro.models.AltaDoctosResponse
import com.example.agroag_mro.models.DocumentosResponse
import com.example.agroag_mro.models.FolioResponse
import com.example.agroag_mro.models.LoginRequest
import com.example.agroag_mro.models.LoginResponse
import com.example.agroag_mro.models.OrdenesRequest
import com.example.agroag_mro.models.OrdenesResponse
import com.example.agroag_mro.models.PantallasResponse
import com.example.agroag_mro.models.PaquetesRequest
import com.example.agroag_mro.models.PaquetesResponse
import com.example.agroag_mro.models.ProductosRequest
import com.example.agroag_mro.models.ProductosResponse
import com.example.agroag_mro.models.ReporteFallaRequest
import com.example.agroag_mro.models.ReporteFallaResponse
import com.example.agroag_mro.models.SucursalResponse
import com.example.agroag_mro.models.TecnicosResponse
import com.example.agroag_mro.models.TiposActivoResponse
import com.example.agroag_mro.models.TrabajosResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("SOAPAction:login")
    @POST("/api_kepler_mro") // reemplaza con la URL relativa correcta
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    //activos
    @Headers("SOAPAction:Cat_Activos")
    @POST("/api_kepler_mro") // reemplaza con la URL relativa correcta
    suspend fun getActivos(@Body request: ActivoRequest): Response<ActivosResponse>

    //tipo de activos
    @Headers("SOAPAction:Cat_Tipos_Activos")
    @POST("/api_kepler_mro") // reemplaza con la URL relativa correcta
    suspend fun getTipoActivo(@Body request: LoginRequest): Response<TiposActivoResponse>

    //tipos de trabajos
    @Headers("SOAPAction:Cat_Tipos_Trabajos")
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
    suspend fun getTecnicos(@Body request: ActivoRequest): Response<TecnicosResponse>

    //envio de documentos al servidor
    @Headers("SOAPAction:Alta_Mano_Refaccion")
    @POST("/api_kepler_mro") // reemplaza con la URL relativa correcta
    suspend fun sendDoctos(@Body request: AltaDoctosRequest): Response<AltaDoctosResponse>

    //documentos
    @Headers("SOAPAction:Cat_Documentos")
    @POST("/api_kepler_mro") // reemplaza con la URL relativa correcta
    suspend fun getDocumentos(@Body request: LoginRequest): Response<DocumentosResponse>

    //06-04-2026
    //Implementacion de la pantallavalidar ordemn, realizaran siguientes endpoint
    //buscar activos relacionados a los usuarios por validar si se agrega a mapeo de loggin o realizar  nueva busqueda
    //buscar ordenes realacioandos al activos que elusuario ha elegido, (buscaOrdenActivo)
    //validar ordenes seleccionada, enviara los documentos seleccionados para realizar la modificacion del esatus en la orden de mantenimiento

}
