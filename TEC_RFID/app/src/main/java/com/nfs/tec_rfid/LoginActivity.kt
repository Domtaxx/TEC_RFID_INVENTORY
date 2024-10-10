package com.nfs.tec_rfid

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val success: Boolean, val token: String?, val error: String?)
class LoginActivity : AppCompatActivity() {
    private lateinit var errorMessageTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login_button)
        errorMessageTextView = findViewById<TextView>(R.id.error_message)
        val registerButton = findViewById<Button>(R.id.register_button)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (!isValidEmail(email) || password.length < 6) {
                errorMessageTextView.visibility = TextView.VISIBLE
                errorMessageTextView.text = "El correo o contraseña son invalido"
            } else {
                errorMessageTextView.visibility = TextView.GONE
                performLogin(email, password)
            }
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
            val response = ApiClient.instance.login(LoginRequest(email, password))
            if (response.success) {
                ApiResult.Success(response)
            }
            else {
                if (response.error == "Unauthorized" || response.error == "401") {
                    ApiResult.Error("Unauthorized access: Please check your credentials.")
                } else {
                    ApiResult.Error(response.error ?: "Unknown error")
                }
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }
}
