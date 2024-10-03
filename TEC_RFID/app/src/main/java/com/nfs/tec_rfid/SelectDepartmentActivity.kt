package com.nfs.tec_rfid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SelectDepartmentActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var confirmButton: Button
    private lateinit var returnButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_department)

        spinner = findViewById(R.id.spinner_department)
        confirmButton = findViewById(R.id.button_confirm)
        returnButton = findViewById(R.id.btn_return_main)

        // List of departments
        val departments = listOf("HR", "IT", "Finance", "Sales", "Marketing")

        // Create an ArrayAdapter using the department list and a default spinner layout
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departments)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        spinner.adapter = adapter

        // Set up the listener for the confirm button
        confirmButton.setOnClickListener {
            val selectedDepartment = spinner.selectedItem.toString()
            Toast.makeText(this, "Selected Department: $selectedDepartment", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ActivosMenuActivity::class.java)
            startActivity(intent)
        }
        // Handle returning to MainActivity
        returnButton.setOnClickListener {
            // Create an explicit Intent to launch MainActivity
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
            finish()  // Optionally finish the current activity if you don't want it in the back stack
        }

        // Optionally handle spinner item selection changes
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Handle department selection if needed
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle when nothing is selected
            }
        }
    }
}
