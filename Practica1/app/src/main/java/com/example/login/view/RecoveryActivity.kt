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

class RecoveryActivity : AppCompatActivity() {

    private lateinit var etRecoveryEmail: EditText
    private lateinit var etSecretAnswer: EditText
    private lateinit var btnVerify: Button
    private lateinit var tvBackToLogin: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recovery)

        initializeViews()
        initializeViewModel()
        setupObservers()
        setupClickListeners()
    }

    private fun initializeViews() {
        etRecoveryEmail = findViewById(R.id.etRecoveryEmail)
        etSecretAnswer = findViewById(R.id.etSecretAnswer)
        btnVerify = findViewById(R.id.btnVerify)
        tvBackToLogin = findViewById(R.id.tvBackToLogin)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initializeViewModel() {
        val userProvider = UserProvider(this)
        userViewModel = UserViewModel(userProvider)
    }

    private fun setupObservers() {
        userViewModel.validationResult.observe(this, Observer { result ->
            progressBar.visibility = View.GONE
            btnVerify.isEnabled = true
            btnVerify.text = "Verificar"

            if (result.isValid) {
                // Verificación exitosa - ir a NewPasswordActivity
                val intent = Intent(this, NewPasswordActivity::class.java)
                intent.putExtra("USER_EMAIL", etRecoveryEmail.text.toString().trim())
                intent.putExtra("IS_RECOVERY", true)
                startActivity(intent)
                finish()
            } else {
                // Error en verificación
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupClickListeners() {
        btnVerify.setOnClickListener {
            performRecovery()
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun performRecovery() {
        val email = etRecoveryEmail.text.toString().trim()
        val secretAnswer = etSecretAnswer.text.toString().trim()

        // Validaciones básicas
        if (email.isEmpty()) {
            etRecoveryEmail.error = "El email es requerido"
            etRecoveryEmail.requestFocus()
            return
        }

        if (secretAnswer.isEmpty()) {
            etSecretAnswer.error = "La respuesta secreta es requerida"
            etSecretAnswer.requestFocus()
            return
        }

        // Validar formato de email
        if (!userViewModel.validateEmail(email)) {
            etRecoveryEmail.error = "Formato de email inválido"
            etRecoveryEmail.requestFocus()
            return
        }

        // Mostrar loading
        progressBar.visibility = View.VISIBLE
        btnVerify.isEnabled = false
        btnVerify.text = "Verificando..."

        // Realizar verificación a través del ViewModel
        userViewModel.validateRecovery(email, secretAnswer)
    }

    override fun onResume() {
        super.onResume()
        userViewModel.clearStates()
    }
}