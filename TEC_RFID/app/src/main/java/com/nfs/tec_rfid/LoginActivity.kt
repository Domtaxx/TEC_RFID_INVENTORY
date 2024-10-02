package com.nfs.tec_rfid
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login_button)
        val errorMessageTextView = findViewById<TextView>(R.id.error_message)
        val registerButton = findViewById<Button>(R.id.register_button)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (!isValidEmail(email) || password.length < 6) {
                errorMessageTextView.visibility = TextView.VISIBLE
            } else {
                errorMessageTextView.visibility = TextView.GONE
                // Proceed with login logic
                // TODO: Add Login validation
                val intent = Intent(this, MainMenuActivity::class.java)
                startActivity(intent)
            }
        }
        registerButton.setOnClickListener {
            // Navigate to RegisterActivity when the button is clicked
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
