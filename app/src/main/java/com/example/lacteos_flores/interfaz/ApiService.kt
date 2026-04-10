package com.example.lacteos_flores.interfaz

import com.example.lacteos_flores.models.ubicacionRequest
import com.example.lacteos_flores.models.IniciaDiaResponse
import com.example.lacteos_flores.models.AltaDoctosRequest
import com.example.lacteos_flores.models.AltaDoctosResponse
import com.example.lacteos_flores.models.BancosResponse
import com.example.lacteos_flores.models.ControlAuxResponse
import com.example.lacteos_flores.models.GastosResponse
import com.example.lacteos_flores.models.LoginRequest
import com.example.lacteos_flores.models.LoginResponse
import com.example.lacteos_flores.models.PaquetesRequest
import com.example.lacteos_flores.models.ProductosRequest
import com.example.lacteos_flores.models.ProductosResponse
import com.example.lacteos_flores.models.ResponseDocumentos
import com.example.lacteos_flores.models.ResponseExistencia
import com.example.lacteos_flores.models.ResponseParidades
import com.example.lacteos_flores.models.TerminaDiaResponse
import com.example.lacteos_flores.models.ValidaDiaResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("SOAPAction:login")
    @POST("/api_kepler_lacteos") // login lacteos
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    //ws para manejo de jornada
    //iniciar jornada
    @Headers("SOAPAction:iniciaDia")
    @POST("/api_kepler_lacteos") // iniciar dia de labores
    suspend fun sendIniDia(@Body request: ubicacionRequest): Response<IniciaDiaResponse>

    //termina dia
    @Headers("SOAPAction:terminaDia")
    @POST("/api_kepler_lacteos") // reemplaza con la URL relativa correcta
    suspend fun sendTerDia(@Body request: ubicacionRequest): Response<TerminaDiaResponse>

    //valida Dia
    @Headers("SOAPAction:ValidaDia")
    @POST("/api_kepler_lacteos") // reemplaza con la URL relativa correcta
    suspend fun validaDia(@Body request: LoginRequest): Response<ValidaDiaResponse>

    //para el manejo de las ventas
    //documentos
    @Headers("SOAPAction:Documentos")
    @POST("/api_kepler_lacteos") // reemplaza con la URL relativa correcta
    suspend fun getDoctos(@Body request: LoginRequest): Response<ResponseDocumentos>

    //bancos
    @Headers("SOAPAction:bancos")
    @POST("/api_kepler_lacteos") // reemplaza con la URL relativa correcta
    suspend fun getBancos(@Body request: LoginRequest): Response<BancosResponse>

    //paridades
    @Headers("SOAPAction:paridades")
    @POST("/api_kepler_lacteos") // reemplaza con la URL relativa correcta
    suspend fun getParidades(@Body request: LoginRequest): Response<ResponseParidades>

    //gastos
    @Headers("SOAPAction:Cat_gastos")
    @POST("/api_kepler_lacteos") // reemplaza con la URL relativa correcta
    suspend fun getgastos(@Body request: LoginRequest): Response<GastosResponse>

    //productos
    @Headers("SOAPAction:productos")
    @POST("/api_kepler_lacteos") // reemplaza con la URL relativa correcta
    suspend fun getProductos(@Body request: PaquetesRequest): Response<ProductosResponse>

    //existecias productos
    @Headers("SOAPAction:existencia")
    @POST("/api_kepler_lacteos") // reemplaza con la URL relativa correcta
    suspend fun getExistencias(@Body request: ProductosRequest): Response<ResponseExistencia>

    //controles auxiliares
    @Headers("SOAPAction:control_Aux")
    @POST("/api_kepler_lacteos") // reemplaza con la URL relativa correcta
    suspend fun getAuxiliares(@Body request: LoginRequest): Response<ControlAuxResponse>

    //tecnicos
    @Headers("SOAPAction:Cat_Tecnicos")
    @POST("/api_kepler_lacteos") // reemplaza con la URL relativa correcta
    suspend fun getTecnicos(@Body request: ubicacionRequest): Response<LoginRequest>

    //envio de documentos al servidor
    @Headers("SOAPAction:Alta_Mano_Refaccion")
    @POST("/api_kepler_lacteos") // reemplaza con la URL relativa correcta
    suspend fun sendDoctos(@Body request: AltaDoctosRequest): Response<AltaDoctosResponse>

    //documentos
    @Headers("SOAPAction:Cat_Documentos")
    @POST("/api_kepler_lacteos") // reemplaza con la URL relativa correcta
    suspend fun getDocumentos(@Body request: LoginRequest): Response<LoginResponse>

}
