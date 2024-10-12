package com.nfs.tec_rfid.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nfs.tec_rfid.R
import com.nfs.tec_rfid.models.Department
import com.nfs.tec_rfid.models.Room
import com.nfs.tec_rfid.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectDepartmentActivity : AppCompatActivity() {

    private lateinit var spinnerDepartment: Spinner
    private lateinit var spinnerRoom: Spinner
    private lateinit var confirmButton: Button
    private lateinit var returnButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_department)

        spinnerDepartment = findViewById(R.id.spinner_department)
        spinnerRoom = findViewById(R.id.spinner_room) // Make sure to add this spinner in your XML layout
        confirmButton = findViewById(R.id.button_confirm)
        returnButton = findViewById(R.id.btn_return_main)

        // Fetch departments from the server
        fetchDepartments()

        // Set up the listener for the confirm button
        confirmButton.setOnClickListener {
            val selectedDepartment = spinnerDepartment.selectedItem?.toString() ?: "No department selected"
            val selectedRoom = spinnerRoom.selectedItem?.toString() ?: "No room selected"

            // Save the data in SharedPreferences
            val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("selected_department", selectedDepartment)
            editor.putString("selected_room", selectedRoom)
            editor.apply()

            // Start the next activity
            val intent = Intent(this, ActivosMenuActivity::class.java)
            startActivity(intent)
        }

        // Handle returning to MainActivity
        returnButton.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Set listener for department selection
        spinnerDepartment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedDepartment = parent.getItemAtPosition(position) as String
                val departmentId = (parent.selectedItemPosition + 1) // Assuming the department ID corresponds to position + 1
                fetchRooms(departmentId)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle when nothing is selected
            }
        }
    }

    private fun fetchDepartments() {
        ApiClient.instance.getDepartments().enqueue(object : Callback<List<Department>> {
            override fun onResponse(call: Call<List<Department>>, response: Response<List<Department>>) {
                if (response.isSuccessful) {
                    val departments = response.body()?.map { it.department_name } ?: listOf()
                    setupSpinner(spinnerDepartment, departments)
                } else {
                    Toast.makeText(this@SelectDepartmentActivity, "Failed to load departments", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Department>>, t: Throwable) {
                Toast.makeText(this@SelectDepartmentActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchRooms(departmentId: Int) {
        ApiClient.instance.getRoomsByDepartment(departmentId).enqueue(object : Callback<List<Room>> {
            override fun onResponse(call: Call<List<Room>>, response: Response<List<Room>>) {
                if (response.isSuccessful) {
                    val rooms = response.body()?.map { it.room_name } ?: listOf()
                    if (rooms.isNotEmpty()) {
                        setupSpinner(spinnerRoom, rooms)
                        spinnerRoom.visibility = View.VISIBLE // Show the spinner when rooms are available
                        confirmButton.visibility = View.VISIBLE // Show the confirm button when rooms are available
                    } else {
                        spinnerRoom.visibility = View.GONE // Hide the spinner when no rooms are available
                        confirmButton.visibility = View.GONE // Hide the confirm button when no rooms are available
                        Toast.makeText(this@SelectDepartmentActivity, "No rooms found for this department", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    spinnerRoom.visibility = View.GONE
                    confirmButton.visibility = View.GONE
                    Toast.makeText(this@SelectDepartmentActivity, "Failed to load rooms", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Room>>, t: Throwable) {
                spinnerRoom.visibility = View.GONE
                confirmButton.visibility = View.GONE
                Toast.makeText(this@SelectDepartmentActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }
}