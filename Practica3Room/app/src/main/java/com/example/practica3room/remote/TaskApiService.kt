package com.example.practica3room.remote
import com.example.practica3room.model.*
import retrofit2.Response
import retrofit2.http.*

interface TaskApiService {
    @GET("api/tasks")
    suspend fun getTasks(): Response<List<TaskApi>>

    @GET("api/tasks/{id}")
    suspend fun getTask(
        @Path("id") id: Int
    ): Response<TaskApi>

    @POST("api/tasks")
    suspend fun createTask(

        @Body task: TaskRequest
    ): Response<TaskApi>

    @PUT("api/tasks/{id}")
    suspend fun updateTask(

        @Path("id") id: Int,
        @Body task: TaskRequest
    ): Response<MessageResponse>

    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(

        @Path("id") id: Int
    ): Response<MessageResponse>
}