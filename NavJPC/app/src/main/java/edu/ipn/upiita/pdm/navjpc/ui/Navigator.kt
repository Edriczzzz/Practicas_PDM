package edu.ipn.upiita.pdm.navjpc.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Navigator() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "inicio") {
        composable("inicio") { LoginScreen(navController) }
        composable("segunda") { UserMenuScreen(navController) }
        composable("registro") { FormRegScreen(navController) }
    }
}
