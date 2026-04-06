package com.example.lacteos_flores.interfaz

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://keplerqro.dnsalias.com:1960/" //url pruebas PLF
    //private const val BASE_URL = "http://elmex.erpkepler.net:8097/api_dimeint_inventarios/"// url para pruebas con elmex
    private  val okHttpClient = OkHttpClient.Builder()
        .addInterceptor{chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Content-Type", "application/json")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)

        }
        .build()
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
