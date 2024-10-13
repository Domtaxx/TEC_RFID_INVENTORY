package com.nfs.tec_rfid.views

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.nfs.tec_rfid.R
import com.nfs.tec_rfid.models.Department
import com.nfs.tec_rfid.models.UserUpdate
import com.nfs.tec_rfid.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Context
import android.view.View

class ModifyUserActivity : AppCompatActivity() {

    private lateinit var passwordEditText: EditText
    private lateinit var departmentSpinner: Spinner
    private lateinit var modifyButton: Button
    private var departments: List<Department> = listOf()
    private var selectedDepartmentId: Int? = null
    private var userToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_user)

        // Initialize UI elements
        passwordEditText = findViewById(R.id.password_edit_text)
        departmentSpinner = findViewById(R.id.department_spinner)
        modifyButton = findViewById(R.id.modify_button)

        userToken = getUserToken()

        // Fetch the list of departments and populate the spinner
        fetchDepartments()

        // Set up the button click listener
        modifyButton.setOnClickListener {
            modifyUser()
        }
    }

    private fun getUserToken(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_token", null)
    }

    private fun fetchDepartments() {
        val call = ApiClient.instance.getDepartments()
        call.enqueue(object : Callback<List<Department>> {
            override fun onResponse(call: Call<List<Department>>, response: Response<List<Department>>) {
                if (response.isSuccessful) {
                    departments = response.body() ?: listOf()
                    populateDepartmentSpinner()
                } else {
                    Toast.makeText(this@ModifyUserActivity, "Failed to load departments", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Department>>, t: Throwable) {
                Toast.makeText(this@ModifyUserActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateDepartmentSpinner() {
        val departmentNames = departments.map { it.department_name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departmentNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        departmentSpinner.adapter = adapter

        departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedDepartmentId = departments[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedDepartmentId = null
            }
        }
    }

    private fun modifyUser() {
        val newPassword = passwordEditText.text.toString()

        if (newPassword.isNotEmpty() && selectedDepartmentId != null) {
            // Use the UserUpdate data class
            val userUpdate = UserUpdate(
                password = newPassword,
                id_department = selectedDepartmentId!!,
                token = getUserToken()!!
            )

            // Assuming you're using Retrofit for API calls
            val call = ApiClient.instance.updateUser(userUpdate)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ModifyUserActivity, "User updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ModifyUserActivity, "Failed to update user", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@ModifyUserActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }

}


