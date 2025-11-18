package com.example.practica3room.model


import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale

// ============ Modelo de Task para la API ============


data class TaskApi(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("name") val name: String = "",
        @SerializedName("status") val status: Boolean = false,
        @SerializedName("deadline") val deadline: String = "",
        @SerializedName("created_at") val createdAt: String? = null,
        @SerializedName("updated_at") val updatedAt: String? = null
)


data class LoginRequest(
        @SerializedName("username") val username: String,
        @SerializedName("password") val password: String
)

data class LoginResponse(
        @SerializedName("token") val token: String
)

data class TaskRequest(
        @SerializedName("name") val name: String,
        @SerializedName("status") val status: Boolean,
        @SerializedName("deadline") val deadline: String
)

data class MessageResponse(
        @SerializedName("message") val message: String = ""
)


object DateConverter {
        private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        private val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Convierte de "dd/MM/yyyy" a "yyyy-MM-dd"
        fun toApiFormat(dateString: String): String {
                return try {
                        // Si ya viene en formato ISO (con T), extraer solo la fecha
                        if (dateString.contains("T")) {
                                return dateString.split("T")[0]
                        }

                        // Si viene en formato display (dd/MM/yyyy)
                        if (dateString.contains("/")) {
                                val parts = dateString.split("/")
                                if (parts.size == 3) {
                                        val (day, month, year) = parts
                                        return "$year-${month.padStart(2, '0')}-${day.padStart(2, '0')}"
                                }
                        }

                        // Si ya viene en formato correcto (yyyy-MM-dd)
                        dateString
                } catch (e: Exception) {
                        dateString
                }
        }

        // Convierte de "yyyy-MM-dd" a "dd/MM/yyyy"
        fun toDisplayFormat(dateString: String): String {
                return try {
                        // Si viene con timestamp ISO, extraer solo la fecha
                        val cleanDate = if (dateString.contains("T")) {
                                dateString.split("T")[0]
                        } else {
                                dateString
                        }

                        val parts = cleanDate.split("-")
                        if (parts.size == 3) {
                                val (year, month, day) = parts
                                return "${day.padStart(2, '0')}/${month.padStart(2, '0')}/$year"
                        }
                        dateString
                } catch (e: Exception) {
                        dateString
                }
        }
}