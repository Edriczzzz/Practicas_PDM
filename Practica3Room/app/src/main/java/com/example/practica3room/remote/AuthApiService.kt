package com.example.practica3room.remote

import com.example.practica3room.model.*
import retrofit2.Response
import retrofit2.http.*

// ============ Servicio de Autenticación ============
interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}