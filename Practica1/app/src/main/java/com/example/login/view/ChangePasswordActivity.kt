package com.example.login.view

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

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmNewPassword: EditText
    private lateinit var btnSavePassword: Button
    private lateinit var btnCancelChange: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var userViewModel: UserViewModel
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pass)

        getIntentData()
        initializeViews()
        initializeViewModel()
        setupObservers()
        setupClickListeners()
        setupPasswordValidation()
    }

    private fun getIntentData() {
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
    }

    private fun initializeViews() {
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword)
        btnSavePassword = findViewById(R.id.btnSavePassword)
        btnCancelChange = findViewById(R.id.btnCancelChange)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initializeViewModel() {
        val userProvider = UserProvider(this)
        userViewModel = UserViewModel(userProvider)
    }

    private fun setupObservers() {
        userViewModel.validationResult.observe(this, Observer { result ->
            progressBar.visibility = View.GONE
            btnSavePassword.isEnabled = true
            btnSavePassword.text = "Guardar Cambios"

            if (result.isValid) {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                finish() // Regresar a WelcomeActivity
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupClickListeners() {
        btnSavePassword.setOnClickListener {
            savePasswordChange()
        }

        btnCancelChange.setOnClickListener {
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

        etConfirmNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val confirmPassword = s.toString()
                val newPassword = etNewPassword.text.toString()

                if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                    etConfirmNewPassword.error = "Las contraseñas no coinciden"
                } else {
                    etConfirmNewPassword.error = null
                }
            }
        })
    }

    private fun savePasswordChange() {
        val currentPassword = etCurrentPassword.text.toString()
        val newPassword = etNewPassword.text.toString()
        val confirmNewPassword = etConfirmNewPassword.text.toString()

        // Validaciones básicas
        if (currentPassword.isEmpty()) {
            etCurrentPassword.error = "La contraseña actual es requerida"
            etCurrentPassword.requestFocus()
            return
        }

        if (newPassword.isEmpty()) {
            etNewPassword.error = "La nueva contraseña es requerida"
            etNewPassword.requestFocus()
            return
        }

        if (confirmNewPassword.isEmpty()) {
            etConfirmNewPassword.error = "La confirmación es requerida"
            etConfirmNewPassword.requestFocus()
            return
        }

        if (newPassword != confirmNewPassword) {
            etConfirmNewPassword.error = "Las contraseñas no coinciden"
            etConfirmNewPassword.requestFocus()
            return
        }

        if (currentPassword == newPassword) {
            etNewPassword.error = "La nueva contraseña debe ser diferente a la actual"
            etNewPassword.requestFocus()
            return
        }

        // Validar formato de nueva contraseña
        val passwordValidation = userViewModel.validatePasswordFormat(newPassword)
        if (!passwordValidation.isValid) {
            etNewPassword.error = passwordValidation.message
            etNewPassword.requestFocus()
            return
        }

        // Mostrar loading
        progressBar.visibility = View.VISIBLE
        btnSavePassword.isEnabled = false
        btnSavePassword.text = "Guardando..."

        // Realizar cambio de contraseña
        userViewModel.changePassword(currentPassword, newPassword, confirmNewPassword)
    }

    override fun onResume() {
        super.onResume()
        userViewModel.clearStates()
    }
}