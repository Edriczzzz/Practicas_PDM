package com.example.login.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.login.R
import com.example.login.provider.UserProvider
import com.example.login.viewmodel.UserViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        initializeViewModel()
        setupObservers()
        setupClickListeners()
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initializeViewModel() {
        val userProvider = UserProvider(this)
        userViewModel = UserViewModel(userProvider)
    }

    private fun setupObservers() {
        // Observer para el estado del login
        userViewModel.loginState.observe(this, Observer { state ->
            if (state.isLoading) {
                showLoading(true)
            } else {
                showLoading(false)

                if (state.isSuccess && state.user != null) {
                    // Login exitoso - ir a WelcomeActivity
                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.putExtra("USER_NAME", state.user.name)
                    intent.putExtra("USER_EMAIL", state.user.email)
                    startActivity(intent)
                    finish()
                } else if (state.errorMessage.isNotEmpty()) {
                    // Error en login
                    Toast.makeText(this, state.errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            performLogin()
        }

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, RecoveryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validaciones básicas
        if (email.isEmpty()) {
            etEmail.error = "El email es requerido"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "La contraseña es requerida"
            etPassword.requestFocus()
            return
        }

        // Validar formato de email
        if (!userViewModel.validateEmail(email)) {
            etEmail.error = "Formato de email inválido"
            etEmail.requestFocus()
            return
        }

        // Realizar login a través del ViewModel
        userViewModel.login(email, password)
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false
            btnLogin.text = "Iniciando sesión..."
        } else {
            progressBar.visibility = View.GONE
            btnLogin.isEnabled = true
            btnLogin.text = "Iniciar Sesión"
        }
    }

    override fun onResume() {
        super.onResume()
        // Limpiar estados cuando se regresa a esta activity
        userViewModel.clearStates()
    }
}