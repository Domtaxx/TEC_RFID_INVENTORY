package com.nfs.tec_rfid

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ModifyUserActivity : AppCompatActivity() {

    private lateinit var passwordEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private lateinit var checkboxHR: CheckBox
    private lateinit var checkboxIT: CheckBox
    private lateinit var checkboxFinance: CheckBox
    private lateinit var checkboxSales: CheckBox
    private lateinit var checkboxMarketing: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_user)

        // Initialize UI elements
        passwordEditText = findViewById(R.id.editText_password)
        saveButton = findViewById(R.id.button_guardar)
        cancelButton = findViewById(R.id.button_cancelar)

        // Checkboxes for departments
        checkboxHR = findViewById(R.id.checkbox_hr)
        checkboxIT = findViewById(R.id.checkbox_it)
        checkboxFinance = findViewById(R.id.checkbox_finance)
        checkboxSales = findViewById(R.id.checkbox_sales)
        checkboxMarketing = findViewById(R.id.checkbox_marketing)

        // Handle Save button click
        saveButton.setOnClickListener {
            val newPassword = passwordEditText.text.toString()
            val selectedDepartments = getSelectedDepartments()

            if (newPassword.isNotEmpty()) {
                // Save the user information (password and departments)
                Toast.makeText(this, "Usuario modificado. Contraseña actualizada.", Toast.LENGTH_SHORT).show()
                // Optionally, save to database or backend
            } else {
                Toast.makeText(this, "Por favor, ingrese una nueva contraseña.", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Cancel button click
        cancelButton.setOnClickListener {
            finish()  // Close the activity without saving
        }
    }

    // Get the selected departments based on checkboxes
    private fun getSelectedDepartments(): List<String> {
        val departments = mutableListOf<String>()

        if (checkboxHR.isChecked) departments.add("Recursos Humanos")
        if (checkboxIT.isChecked) departments.add("Informática")
        if (checkboxFinance.isChecked) departments.add("Finanzas")
        if (checkboxSales.isChecked) departments.add("Ventas")
        if (checkboxMarketing.isChecked) departments.add("Marketing")

        return departments
    }
}

