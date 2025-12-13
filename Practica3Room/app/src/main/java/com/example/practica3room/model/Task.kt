package com.example.practica3room.model
import com.example.practica3room.local.TaskEntity

import com.google.gson.annotations.SerializedName

// ============ Modelo de Task para la API ============
data class TaskApi(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("name") val name: String = "",
        @SerializedName("status") val status: Boolean = false,
        @SerializedName("deadline") val deadline: String = "",
        @SerializedName("userId") val userId: Int = 1,  // ← AGREGADO
        @SerializedName("created_at") val createdAt: String? = null,
        @SerializedName("updated_at") val updatedAt: String? = null
)

// ============ Login ============
data class LoginRequest(
        @SerializedName("username") val username: String,
        @SerializedName("password") val password: String
)

data class LoginResponse(
        @SerializedName("token") val token: String,
        @SerializedName("user") val user: UserInfo? = null
)

data class UserInfo(
        @SerializedName("id") val id: Int,
        @SerializedName("username") val username: String,
        @SerializedName("email") val email: String? = null
)

// ============ TaskRequest ============
data class TaskRequest(
        @SerializedName("name") val name: String,
        @SerializedName("status") val status: Boolean,
        @SerializedName("deadline") val deadline: String,
        @SerializedName("userId") val userId: Int  // ← AGREGADO
)

// ============ MessageResponse ============
data class MessageResponse(
        @SerializedName("message") val message: String = ""
)

// ============ DateConverter ============
object DateConverter {
        // Convierte de "dd/MM/yyyy" a "yyyy-MM-dd"
        fun toApiFormat(dateString: String): String {
                // Si ya viene en formato ISO (con T), extraer solo la fecha
                if (dateString.contains("T")) {
                        return dateString.split("T")[0]
                }

                val parts = dateString.split("/")
                if (parts.size != 3) return dateString
                val (day, month, year) = parts
                return "$year-${month.padStart(2, '0')}-${day.padStart(2, '0')}"
        }

        // Convierte de "yyyy-MM-dd" a "dd/MM/yyyy"
        fun toDisplayFormat(dateString: String): String {
                // Si viene con timestamp ISO, extraer solo la fecha
                val cleanDate = if (dateString.contains("T")) {
                        dateString.split("T")[0]
                } else {
                        dateString
                }

                val parts = cleanDate.split("-")
                if (parts.size != 3) return dateString
                val (year, month, day) = parts
                return "${day.padStart(2, '0')}/${month.padStart(2, '0')}/$year"
        }


        fun TaskApi.toEntity(): TaskEntity {
                return TaskEntity(
                        id = this.id ?: 0,
                        name = this.name,
                        status = this.status,
                        deadline = this.deadline,
                        userId = this.userId,  // ← AGREGADO
                        updatedAt = System.currentTimeMillis(),
                        pendingSync = false,
                        deleted = false
                )
        }

        // Extensión para convertir TaskEntity a TaskApi
        fun TaskEntity.toApi(): TaskApi {
                return TaskApi(
                        id = this.id,
                        name = this.name,
                        status = this.status,
                        deadline = this.deadline,
                        userId = this.userId  // ← AGREGADO
                )
        }

        // Extensión para convertir TaskEntity a TaskRequest
        fun TaskEntity.toRequest(): TaskRequest {
                return TaskRequest(
                        name = this.name,
                        status = this.status,
                        deadline = this.deadline,
                        userId = this.userId  // ← AGREGADO
                )
        }

}