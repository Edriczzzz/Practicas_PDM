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

    private lateinit var etEmail: EditText
    private lateinit var etAnswer: EditText
    private lateinit var btnVerify: Button
    private lateinit var tvBack: TextView
    private lateinit var tvSecretQuestion: TextView
    private var progressBar: ProgressBar? = null

    private lateinit var userViewModel: UserViewModel
    private var currentStep = 1 // 1: Buscar email, 2: Responder pregunta
    private var userEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("DEBUG: RecoveryActivity onCreate started")
        setContentView(R.layout.activity_recovery)
        println("DEBUG: RecoveryActivity setContentView completed")

        initializeViews()
        initializeViewModel()
        setupObservers()
        setupClickListeners()
        setupInitialUI()
        println("DEBUG: RecoveryActivity onCreate completed")
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etRecoveryEmail)
        etAnswer = findViewById(R.id.etSecretAnswer)
        btnVerify = findViewById(R.id.btnVerify)
        tvBack = findViewById(R.id.tvBackToLogin)
        tvSecretQuestion = findViewById(R.id.tvSecretQuestion)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initializeViewModel() {
        val userProvider = UserProvider(this)
        userViewModel = UserViewModel(userProvider)
    }

    private fun setupObservers() {
        println("DEBUG: RecoveryActivity setupObservers started")

        // Observer para el resultado de obtener la pregunta secreta (Paso 1)
        userViewModel.recoveryResult.observe(this, Observer { result ->
            println("DEBUG: RecoveryActivity recovery result: ${result.isValid} - ${result.message}")

            if (result.isValid && result.secretQuestion != null) {
                // Mostrar la pregunta secreta y pasar al paso 2
                tvSecretQuestion.text = result.secretQuestion
                tvSecretQuestion.visibility = View.VISIBLE
                etAnswer.visibility = View.VISIBLE
                etEmail.isEnabled = false // Deshabilitar email
                btnVerify.text = "Verificar Respuesta"
                currentStep = 2
                userEmail = etEmail.text.toString().trim()
            } else {
                // Error en el paso 1
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                resetToStep1()
            }
        })

        // Observer para el resultado de validar la respuesta (Paso 2)
        userViewModel.validationResult.observe(this, Observer { result ->
            println("DEBUG: RecoveryActivity validation result: ${result.isValid} - ${result.message}")

            if (result.isValid && currentStep == 2) {
                // Verificación exitosa - ir a NewPasswordActivity
                println("DEBUG: RecoveryActivity going to NewPasswordActivity")
                val intent = Intent(this, NewPasswordActivity::class.java)
                intent.putExtra("USER_EMAIL", userEmail)
                intent.putExtra("IS_RECOVERY", true)
                startActivity(intent)
                finish()
            } else if (currentStep == 2) {
                // Error en verificación de respuesta
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
            }
        })

        // Observer para el estado de loading
        userViewModel.isRecoveryLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                progressBar?.visibility = View.VISIBLE
                btnVerify.isEnabled = false
                btnVerify.text = if (currentStep == 1) "Buscando..." else "Verificando..."
            } else {
                progressBar?.visibility = View.GONE
                btnVerify.isEnabled = true
                btnVerify.text = if (currentStep == 1) "Buscar Usuario" else "Verificar Respuesta"
            }
        })
    }

    private fun setupClickListeners() {
        btnVerify.setOnClickListener {
            if (currentStep == 1) {
                performEmailSearch()
            } else {
                performAnswerVerification()
            }
        }

        tvBack.setOnClickListener {
            if (currentStep == 2) {
                // Si está en paso 2, regresar al paso 1
                resetToStep1()
            } else {
                // Si está en paso 1, regresar al login
                finish()
            }
        }
    }

    private fun setupInitialUI() {
        // Configurar UI inicial (Paso 1)
        resetToStep1()
    }

    private fun resetToStep1() {
        currentStep = 1
        etEmail.isEnabled = true
        etEmail.text?.clear()
        etAnswer.text?.clear()
        tvSecretQuestion.visibility = View.GONE
        etAnswer.visibility = View.GONE
        btnVerify.text = "Buscar Usuario"
        tvBack.text = "Volver al inicio de sesión"
        userEmail = ""
    }

    private fun performEmailSearch() {
        val email = etEmail.text.toString().trim()

        // Validaciones básicas
        if (email.isEmpty()) {
            etEmail.error = "El email es requerido"
            etEmail.requestFocus()
            return
        }

        // Validar formato de email
        if (!userViewModel.validateEmail(email)) {
            etEmail.error = "Formato de email inválido"
            etEmail.requestFocus()
            return
        }

        // Buscar usuario y obtener pregunta secreta
        userViewModel.getSecretQuestion(email)
    }

    private fun performAnswerVerification() {
        val secretAnswer = etAnswer.text.toString().trim()

        // Validaciones básicas
        if (secretAnswer.isEmpty()) {
            etAnswer.error = "La respuesta es requerida"
            etAnswer.requestFocus()
            return
        }

        // Validar respuesta secreta
        userViewModel.validateRecovery(userEmail, secretAnswer)
    }

    override fun onResume() {
        super.onResume()
        println("DEBUG: RecoveryActivity onResume called")
        userViewModel.clearStates()
    }

    override fun onDestroy() {
        super.onDestroy()
        userViewModel.clearRecoveryResults()
    }
}