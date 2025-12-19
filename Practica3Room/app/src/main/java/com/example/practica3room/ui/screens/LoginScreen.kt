package com.example.practica3room.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.practica3room.di.AppContainer
import com.example.practica3room.remote.RetrofitClient
import com.example.practica3room.ui.theme.BackgroundCream
import com.example.practica3room.ui.theme.PrimaryBlue
import com.example.practica3room.viewmodel.TaskViewModel
import com.example.practica3room.viewmodel.UiState
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavHostController, viewModel: TaskViewModel) {
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("admin123") } // ← Cambiado a admin123

    val authState by viewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()

    // 🎯 ESTO ES LO IMPORTANTE - Observar cambios en authState
    LaunchedEffect(authState) {
        if (authState is UiState.Success) {
            Log.d("LoginScreen", "✅ Login exitoso, navegando a menu...")
            navController.navigate("menu") {
                // Limpiar el stack para que no pueda volver al login con back
                popUpTo("login") { inclusive = true }
            }
            viewModel.resetAuthState() // Resetear el estado
        }
    }

    // Prueba de conexión (opcional, puedes dejarlo o quitarlo)
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.taskService.getTasks()
            Log.d("API", "Conexión exitosa a Vercel ✅")
        } catch (e: Exception) {
            Log.e("API", "Error conectando a Vercel: ${e.message}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ... resto del código igual ...

        Text(
            text = "📝",
            fontSize = 72.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Task Manager",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Inicia sesión para continuar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Campo de usuario
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Usuario") },
            placeholder = { Text("admin") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                focusedLabelColor = PrimaryBlue,
                focusedLeadingIconColor = PrimaryBlue,
                cursorColor = PrimaryBlue
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            placeholder = { Text("••••••••") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                focusedLabelColor = PrimaryBlue,
                focusedLeadingIconColor = PrimaryBlue,
                cursorColor = PrimaryBlue
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de login
        Button(
            onClick = {
                if (username.isNotBlank() && password.isNotBlank()) {

                    // 🚨 SIN INTERNET: saltar login e iniciar en modo offline
                    if (!AppContainer.isNetworkAvailable)
                    {
                        Log.w("LoginScreen", "🌙 Sin conexión → Entrando en modo offline")
                        viewModel.login(username, password)
                        viewModel.enterOfflineMode()
                        return@Button
                    }

                    // 🌐 CON INTERNET: login normal
                    Log.d("LoginScreen", "🔐 Intentando login con: $username")
                    viewModel.login(username.trim(), password.trim())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = authState !is UiState.Loading
        )
 {
            if (authState is UiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = BackgroundCream
                )
            } else {
                Text(
                    text = "Iniciar Sesión",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Mostrar error si existe
        if (authState is UiState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (authState as UiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Credenciales de prueba
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Credenciales de prueba:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Usuario: admin | Contraseña: admin123", // ← Actualizado
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}