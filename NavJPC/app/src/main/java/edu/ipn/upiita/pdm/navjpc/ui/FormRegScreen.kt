@file:OptIn(ExperimentalMaterial3Api::class)

package edu.ipn.upiita.pdm.navjpc.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import edu.ipn.upiita.pdm.navjpc.FormRegViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FormRegScreen(
    navController: NavHostController,
    viewModel: FormRegViewModel = viewModel()
) {
    var expandedCarrera by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Campo Nombre
            InputField(
                label = "Nombre completo",
                value = viewModel.nombre,
                onValueChange = { viewModel.nombre = it },
                error = viewModel.nombreError,
                keyboardType = KeyboardType.Text
            )

            // Campo Boleta
            InputField(
                label = "Boleta (10 dígitos)",
                value = viewModel.boleta,
                onValueChange = {
                    if (it.length <= 10) viewModel.boleta = it
                },
                error = viewModel.boletaError,
                keyboardType = KeyboardType.Number
            )

            // Campo Correo
            InputField(
                label = "Correo electrónico",
                value = viewModel.correo,
                onValueChange = { viewModel.correo = it },
                error = viewModel.correoError,
                keyboardType = KeyboardType.Email
            )

            // Dropdown de Carrera
            Column(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = expandedCarrera,
                    onExpandedChange = { expandedCarrera = !expandedCarrera }
                ) {
                    OutlinedTextField(
                        value = viewModel.carrera,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Carrera") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCarrera) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = viewModel.carreraError != null,
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCarrera,
                        onDismissRequest = { expandedCarrera = false }
                    ) {
                        viewModel.carreras.forEach { carrera ->
                            DropdownMenuItem(
                                text = { Text(carrera) },
                                onClick = {
                                    viewModel.carrera = carrera
                                    expandedCarrera = false
                                }
                            )
                        }
                    }
                }

                if (viewModel.carreraError != null) {
                    Text(
                        text = viewModel.carreraError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Campo Contraseña
            InputField(
                label = "Contraseña",
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                error = viewModel.passwordError,
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            // Campo Confirmar Contraseña
            InputField(
                label = "Confirmar contraseña",
                value = viewModel.confirmPassword,
                onValueChange = { viewModel.confirmPassword = it },
                error = viewModel.confirmPasswordError,
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Registro
            Button(
                onClick = {
                    if (viewModel.validarCampos()) {
                        // Registro exitoso
                        navController.navigate("segunda") {
                            popUpTo("registro") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar")
            }

            // Mensaje de éxito (opcional)
            if (viewModel.registroExitoso) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¡Registro exitoso!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}