package com.nfs.tec_rfid.views

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.nfs.tec_rfid.R
import com.nfs.tec_rfid.models.Department
import com.nfs.tec_rfid.models.EmployeeRequest
import com.nfs.tec_rfid.models.EmployeeResponse
import com.nfs.tec_rfid.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var departmentSpinner: Spinner
    private var departmentList: List<Department> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        // Access input fields
        val nameEditText = findViewById<EditText>(R.id.user_name)
        val surnameEditText = findViewById<EditText>(R.id.user_surname)
        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val ssnEditText = findViewById<EditText>(R.id.user_ssn)
        departmentSpinner = findViewById(R.id.department_spinner)
        val registerButton = findViewById<Button>(R.id.register_button)

        // Fetch and populate the department list
        fetchDepartments()

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val surname = surnameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val ssn = ssnEditText.text.toString()
            val departmentPosition = departmentSpinner.selectedItemPosition

            if (departmentPosition < 0 || departmentPosition >= departmentList.size) {
                Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val department = departmentList[departmentPosition]

            // Simple validation
            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || ssn.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Proceed with registration logic
                registerUser(name, surname, email, password, ssn, department.id)
            }
        }
    }

    private fun fetchDepartments() {
        val call: Call<List<Department>> = ApiClient.instance.getDepartments()
        call.enqueue(object : Callback<List<Department>> {
            override fun onResponse(call: Call<List<Department>>, response: Response<List<Department>>) {
                if (response.isSuccessful && response.body() != null) {
                    departmentList = response.body() ?: listOf()
                    populateDepartmentSpinner()
                } else {
                    Toast.makeText(this@RegisterActivity, "Failed to load departments", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Department>>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun populateDepartmentSpinner() {
        val departmentNames = departmentList.map { it.department_name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departmentNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        departmentSpinner.adapter = adapter
    }

    private fun registerUser(
        name: String,
        surname: String,
        email: String,
        password: String,
        ssn: String,
        departmentId: Int
    ) {
        val employeeRequest = EmployeeRequest(
            email = email,
            password = password,
            ssn = ssn,
            first_name = name,
            surname = surname,
            id_department = departmentId
        )

        val call = ApiClient.instance.registerEmployee(employeeRequest)
        call.enqueue(object : Callback<EmployeeResponse> {
            override fun onResponse(call: Call<EmployeeResponse>, response: Response<EmployeeResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "User Registered Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, "Failed to register user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<EmployeeResponse>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}


