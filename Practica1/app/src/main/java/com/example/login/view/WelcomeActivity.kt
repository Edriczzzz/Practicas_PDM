package com.example.login.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.login.R
import com.example.login.provider.UserProvider
import com.example.login.viewmodel.UserViewModel

class WelcomeActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var btnMenu: Button
    private lateinit var layoutMenuOptions: LinearLayout
    private lateinit var btnChangePassword: Button
    private lateinit var btnLogout: Button

    private lateinit var userViewModel: UserViewModel
    private var userName: String = ""
    private var userEmail: String = ""
    private var isMenuVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        getUserDataFromIntent()
        initializeViews()
        initializeViewModel()
        setupClickListeners()
    }

    private fun getUserDataFromIntent() {
        userName = intent.getStringExtra("USER_NAME") ?: "Usuario"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
    }

    private fun initializeViews() {
        tvUserName = findViewById(R.id.tvUserName)
        btnMenu = findViewById(R.id.btnMenu)
        layoutMenuOptions = findViewById(R.id.layoutMenuOptions)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnLogout = findViewById(R.id.btnLogout)

        // Mostrar nombre del usuario
        tvUserName.text = userName
    }

    private fun initializeViewModel() {
        val userProvider = UserProvider(this)
        userViewModel = UserViewModel(userProvider)
    }

    private fun setupClickListeners() {
        btnMenu.setOnClickListener {
            toggleMenu()
        }

        // Cambiar contraseña
        btnChangePassword.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            intent.putExtra("USER_EMAIL", userEmail)
            startActivity(intent)
        }

        // Cerrar sesión
        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun toggleMenu() {
        isMenuVisible = !isMenuVisible

        if (isMenuVisible) {
            layoutMenuOptions.visibility = View.VISIBLE
            btnMenu.text = "✕"
        } else {
            layoutMenuOptions.visibility = View.GONE
            btnMenu.text = "☰"
        }
    }

    private fun logout() {
        // Limpiar sesión
        userViewModel.logout()

        // Volver a MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (isMenuVisible) {
            toggleMenu()
        } else {
            // No permitir volver atrás sin logout
            // super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        // Ocultar menú cuando se regresa de otra activity
        if (isMenuVisible) {
            toggleMenu()
        }
    }
}