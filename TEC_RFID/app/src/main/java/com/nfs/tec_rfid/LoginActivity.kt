package com.nfs.tec_rfid

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

data class LoginRequest(val email: String, val password: String, val token: String)
data class LoginResponse(val success: Boolean, val token: String?, val error: String?)

class LoginActivity : AppCompatActivity() {
    private lateinit var errorMessageTextView: TextView
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val manualLoginButton = findViewById<Button>(R.id.manual_login_button)
        val biometricLoginButton = findViewById<Button>(R.id.biometric_login_button)
        errorMessageTextView = findViewById(R.id.error_message)
        val registerButton = findViewById<Button>(R.id.register_button)

        manualLoginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (!isValidEmail(email) || password.length < 6) {
                errorMessageTextView.visibility = TextView.VISIBLE
                errorMessageTextView.text = "Invalid email or password"
            } else {
                errorMessageTextView.visibility = TextView.GONE
                performLogin(email, password)
            }
        }

        biometricLoginButton.setOnClickListener {
            setupBiometricAuthentication()
            biometricPrompt.authenticate(promptInfo)
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun performLogin(email: String, password: String) {
        lifecycleScope.launch {
            when (val result = withContext(Dispatchers.IO) { login(email, password) }) {
                is ApiResult.Success -> {
                    // Save the JWT token or any other necessary data securely
                    saveTokenToSecureStorage(this@LoginActivity, result.data.token ?: "")
                    val intent = Intent(this@LoginActivity, MainMenuActivity::class.java)
                    startActivity(intent)
                }
                is ApiResult.Error -> {
                    errorMessageTextView.visibility = TextView.VISIBLE
                    errorMessageTextView.text = result.message
                }
            }
        }
    }

    private suspend fun login(email: String, password: String): ApiResult<LoginResponse> {
        return try {
            val response = ApiClient.instance.login(LoginRequest(email, password, ""))
            if (response.success) {
                ApiResult.Success(response)
            } else {
                ApiResult.Error(response.error ?: "Unknown error")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    private fun setupBiometricAuthentication() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Biometric authentication succeeded
                    loginWithBiometric()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Biometric authentication failed", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Biometric authentication error: $errString", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for TEC RFID")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()
    }

    private fun loginWithBiometric() {
        lifecycleScope.launch {
            val token = getTokenFromSecureStorage(this@LoginActivity)
            if (token.isNotEmpty()) {
                val result = withContext(Dispatchers.IO) { validateTokenWithServer(token) }
                when (result) {
                    is ApiResult.Success -> {
                        val intent = Intent(this@LoginActivity, MainMenuActivity::class.java)
                        startActivity(intent)
                    }
                    is ApiResult.Error -> {
                        errorMessageTextView.visibility = TextView.VISIBLE
                        errorMessageTextView.text = result.message
                    }
                }
            } else {
                errorMessageTextView.visibility = TextView.VISIBLE
                errorMessageTextView.text = "No saved credentials found. Please log in manually."
            }
        }
    }

    private suspend fun validateTokenWithServer(token: String): ApiResult<LoginResponse> {
        return try {
            // Send the token to the server for validation
            val response = ApiClient.instance.validateToken(LoginRequest("","",token))
            if (response.success) {
                ApiResult.Success(response)
            } else {
                ApiResult.Error(response.error ?: "Token validation failed")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }
    private fun getTokenFromSecureStorage(context: Context): String {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "secure_app_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPreferences.getString("auth_token", "") ?: ""
    }

    private fun saveTokenToSecureStorage(context: Context, token: String) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "secure_app_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        with(sharedPreferences.edit()) {
            putString("auth_token", token)
            apply()
        }
    }
}


