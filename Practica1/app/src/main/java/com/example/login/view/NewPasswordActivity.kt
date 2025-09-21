package com.example.login.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.login.R
import com.example.login.provider.UserProvider
import com.example.login.viewmodel.UserViewModel

class NewPasswordActivity : AppCompatActivity() {

    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var userViewModel: UserViewModel
    private var userEmail: String = ""
    private var isRecovery: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newpass)

        getIntentData()
        initializeViews()
        initializeViewModel()
        setupObservers()
        setupClickListeners()
        setupPasswordValidation()
    }

    private fun getIntentData() {
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        isRecovery = intent.getBooleanExtra("IS_RECOVERY", false)
    }

    private fun initializeViews() {
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnCancel = findViewById(R.id.btnCancel)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initializeViewModel() {
        val userProvider = UserProvider(this)
        userViewModel = UserViewModel(userProvider)
    }

    private fun setupObservers() {
        userViewModel.validationResult.observe(this, Observer { result ->
            progressBar.visibility = View.GONE
            btnChangePassword.isEnabled = true
            btnChangePassword.text = "Cambiar Contraseña"

            if (result.isValid) {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()

                // Regresar a MainActivity después del cambio exitoso
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupClickListeners() {
        btnChangePassword.setOnClickListener {
            changePassword()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupPasswordValidation() {
        etNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.isNotEmpty()) {
                    val validation = userViewModel.validatePasswordFormat(password)
                    if (!validation.isValid) {
                        etNewPassword.error = validation.message
                    } else {
                        etNewPassword.error = null
                    }
                }
            }
        })
    }

    private fun changePassword() {
        val newPassword = etNewPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        // Validaciones básicas
        if (newPassword.isEmpty()) {
            etNewPassword.error = "La nueva contraseña es requerida"
            etNewPassword.requestFocus()
            return
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "La confirmación es requerida"
            etConfirmPassword.requestFocus()
            return
        }

        if (newPassword != confirmPassword) {
            etConfirmPassword.error = "Las contraseñas no coinciden"
            etConfirmPassword.requestFocus()
            return
        }

        // Validar formato de contraseña
        val passwordValidation = userViewModel.validatePasswordFormat(newPassword)
        if (!passwordValidation.isValid) {
            etNewPassword.error = passwordValidation.message
            etNewPassword.requestFocus()
            return
        }

        // Mostrar loading
        progressBar.visibility = View.VISIBLE
        btnChangePassword.isEnabled = false
        btnChangePassword.text = "Cambiando..."

        // Realizar cambio de contraseña
        userViewModel.resetPassword(userEmail, newPassword, confirmPassword)
    }

    override fun onResume() {
        super.onResume()
        userViewModel.clearStates()
    }
}