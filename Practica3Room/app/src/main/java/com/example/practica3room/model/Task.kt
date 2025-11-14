package com.example.practica3room.model


import com.google.gson.annotations.SerializedName

// ============ Modelo de Task para la API ============


data class TaskApi(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("name") val name: String = "",
        @SerializedName("status") private val _status: Int = 0,
        @SerializedName("deadline") val deadline: String = "",
        @SerializedName("created_at") val createdAt: String? = null,
        @SerializedName("updated_at") val updatedAt: String? = null
)
{
        // Propiedad computed que convierte Int a Boolean
        val status: Boolean
                get() = _status == 1
}

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


// ============ Extensión para convertir de formato de fecha ============
object DateConverter {
        // Convierte de "dd/MM/yyyy" a "yyyy-MM-dd"
        fun toApiFormat(dateString: String): String {
                val parts = dateString.split("/")
                if (parts.size != 3) return dateString
                val (day, month, year) = parts
                return "$year-${month.padStart(2, '0')}-${day.padStart(2, '0')}"
        }

        // Convierte de "yyyy-MM-dd" a "dd/MM/yyyy"
        fun toDisplayFormat(dateString: String): String {
                val parts = dateString.split("-")
                if (parts.size != 3) return dateString
                val (year, month, day) = parts
                return "${day.padStart(2, '0')}/${month.padStart(2, '0')}/$year"
        }
}