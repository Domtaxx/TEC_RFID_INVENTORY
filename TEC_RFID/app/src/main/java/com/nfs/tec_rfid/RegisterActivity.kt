package com.nfs.tec_rfid

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        // Access input fields
        val nameEditText = findViewById<EditText>(R.id.user_name)
        val surnameEditText = findViewById<EditText>(R.id.user_surname)
        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val ssnEditText = findViewById<EditText>(R.id.user_ssn)
        val departmentEditText = findViewById<EditText>(R.id.user_department)
        val registerButton = findViewById<Button>(R.id.register_button)

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val surname = surnameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val ssn = ssnEditText.text.toString()
            val department = departmentEditText.text.toString()

            // Simple validation
            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() ||
                ssn.isEmpty() || department.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Proceed with registration logic (e.g., save to a database or send to a server)
                registerUser(name, surname, email, password, ssn, department)
            }
        }
    }

    // Placeholder for the registration logic (can be sending data to server, etc.)
    private fun registerUser(
        name: String,
        surname: String,
        email: String,
        password: String,
        ssn: String,
        department: String
    ) {
        // Here you would normally handle the user registration (e.g., send to a server or save locally)
        Toast.makeText(this, "User Registered Successfully", Toast.LENGTH_SHORT).show()

        // Finish or redirect to login screen after successful registration
        finish() // This closes the registration screen and returns to the previous one
    }
}

