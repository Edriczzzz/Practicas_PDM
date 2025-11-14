package com.example.practica3room.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Cambia según tu caso (emulador: 10.0.2.2)
    private const val BASE_URL = "http://10.0.2.2:3000/"

    init {
        Log.d("RetrofitClient", "🌐 BASE_URL = $BASE_URL")
    }

    // Token configurable (se puede setear desde Repository o AuthManager)
    @Volatile
    private var token: String? = null

    fun setAuthToken(newToken: String?) {
        token = newToken
        Log.d("RetrofitClient", "Token actualizado: ${if (token != null) "********" else "null"}")
    }

    // Interceptor que agrega header Authorization si token != null
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")

        token?.let {
            // suponiendo esquema "Bearer <token>"
            builder.header("Authorization", "Bearer $it")
        }

        chain.proceed(builder.build())
    }

    // Logging
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("HTTP", message)
    }.apply { level = HttpLoggingInterceptor.Level.BODY }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)          // primero añade token
        .addInterceptor(loggingInterceptor)       // luego loggea
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val taskService: TaskApiService by lazy {
        retrofit.create(TaskApiService::class.java)
    }
}
