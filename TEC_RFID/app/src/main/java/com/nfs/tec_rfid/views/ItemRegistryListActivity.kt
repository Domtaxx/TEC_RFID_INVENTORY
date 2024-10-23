package com.nfs.tec_rfid.views

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nfs.tec_rfid.Adapters.ItemRegistryAdapter
import com.nfs.tec_rfid.R
import com.nfs.tec_rfid.models.Department
import com.nfs.tec_rfid.models.ItemRegistryResponse
import com.nfs.tec_rfid.models.ItemRegistryUpdate
import com.nfs.tec_rfid.models.Room
import com.nfs.tec_rfid.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ItemRegistryListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var registries: List<ItemRegistryResponse> = listOf()
    private var userToken: String? = null
    private lateinit var adapter: ItemRegistryAdapter  // Custom adapter for RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_registry_list)

        recyclerView = findViewById(R.id.item_registry_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch user token from SharedPreferences
        userToken = getUserToken()

        // Fetch registries
        fetchUserRegistries()
    }
    private fun getUserToken(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_token", null)
    }

    private fun fetchUserRegistries() {
        userToken?.let { token ->
            val call = ApiClient.instance.getItemsRegisteredByToken(token)
            call.enqueue(object : Callback<List<ItemRegistryResponse>> {
                override fun onResponse(call: Call<List<ItemRegistryResponse>>, response: Response<List<ItemRegistryResponse>>) {
                    if (response.isSuccessful) {
                        registries = response.body() ?: listOf()
                        populateRecyclerView()
                    } else {
                        Toast.makeText(this@ItemRegistryListActivity, "Failed to load registries", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<ItemRegistryResponse>>, t: Throwable) {
                    Toast.makeText(this@ItemRegistryListActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } ?: run {
            Toast.makeText(this, "User token not found. Please login again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateRecyclerView() {
        val adapter = ItemRegistryAdapter(registries) { selectedRegistry ->
            // Handle item click here
            fetchDepartments { departments ->
                showModifyDialog(selectedRegistry, departments)
            }
        }
        recyclerView.adapter = adapter
    }

    private fun showModifyDialog(registry: ItemRegistryResponse, departments: List<Department>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_modify_registry, null)
        val departmentSpinner = dialogView.findViewById<Spinner>(R.id.department_spinner)
        val roomSpinner = dialogView.findViewById<Spinner>(R.id.room_spinner)

        var selectedRoomId: Int? = null  // Variable to store selected room ID

        // Populate department spinner
        val departmentAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departments.map { it.department_name })
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        departmentSpinner.adapter = departmentAdapter

        // Select the current department by default
        val currentDepartmentIndex = departments.indexOfFirst { it.id == registry.department_id }
        if (currentDepartmentIndex >= 0) {
            departmentSpinner.setSelection(currentDepartmentIndex)
            // Fetch and populate rooms based on the department
            fetchRoomsByDepartment(departments[currentDepartmentIndex].id) { rooms ->
                populateRoomSpinner(roomSpinner, rooms, registry.room_id) { selectedRoomId = it }  // Set selectedRoomId on room selection
            }
        }

        // When a new department is selected, fetch rooms for that department
        departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedDepartmentId = departments[position].id
                fetchRoomsByDepartment(selectedDepartmentId) { rooms ->
                    populateRoomSpinner(roomSpinner, rooms, registry.room_id) { selectedRoomId = it }  // Set selectedRoomId on room selection
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Modify Registry")
            .setPositiveButton("Save") { dialog, _ ->
                val selectedDepartment = departments[departmentSpinner.selectedItemPosition]

                // Ensure we have a valid room_id before saving
                selectedRoomId?.let {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    dateFormat.timeZone = TimeZone.getTimeZone("UTC")

                        try {
                            // Parse the registry.registry_date string into a Date object
                            val parsedDate = dateFormat.parse(registry.registry_date)

                            val updatedRegistry = ItemRegistryUpdate(
                                id_emp = registry.id_emp,  // Assuming id_emp from registry
                                id_item = registry.item_id,
                                registry_date = registry.registry_date,  // Use the parsed Date object
                                room_id = selectedRoomId!!  // Use the selected room ID
                            )
                            updateRegistry(updatedRegistry)
                        } catch (e: ParseException) {
                            e.printStackTrace()
                            Toast.makeText(this, "Error parsing date", Toast.LENGTH_SHORT).show()
                        }


                    // Call API to update the registry

                } ?: run {
                    Toast.makeText(this, "Please select a valid room", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()
    }

    private fun populateRoomSpinner(roomSpinner: Spinner, rooms: List<Room>, selectedRoomId: Int, onRoomSelected: (Int) -> Unit) {
        val roomAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rooms.map { it.room_name })
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roomSpinner.adapter = roomAdapter

        // Set the current room as selected by default
        val currentRoomIndex = rooms.indexOfFirst { it.id == selectedRoomId }
        if (currentRoomIndex >= 0) {
            roomSpinner.setSelection(currentRoomIndex)
        }

        // Set listener to capture room selection
        roomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedRoom = rooms[position]
                onRoomSelected(selectedRoom.id)  // Pass the selected room_id back
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
    // Function to update the registry
    private fun updateRegistry(updatedRegistry: ItemRegistryUpdate) {
        val call = ApiClient.instance.updateItemRegistry(updatedRegistry)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ItemRegistryListActivity, "Registry updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ItemRegistryListActivity, "Failed to update registry", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ItemRegistryListActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun fetchDepartments(onComplete: (List<Department>) -> Unit) {
        val call = ApiClient.instance.getDepartments()
        call.enqueue(object : Callback<List<Department>> {
            override fun onResponse(call: Call<List<Department>>, response: Response<List<Department>>) {
                if (response.isSuccessful) {
                    onComplete(response.body() ?: listOf())
                } else {
                    Toast.makeText(this@ItemRegistryListActivity, "Failed to load departments", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Department>>, t: Throwable) {
                Toast.makeText(this@ItemRegistryListActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchRoomsByDepartment(departmentId: Int, onComplete: (List<Room>) -> Unit) {
        val call = ApiClient.instance.getRoomsByDepartment(departmentId)
        call.enqueue(object : Callback<List<Room>> {
            override fun onResponse(call: Call<List<Room>>, response: Response<List<Room>>) {
                if (response.isSuccessful) {
                    onComplete(response.body() ?: listOf())
                } else {
                    onComplete(emptyList())  // Pass an empty list to clear the spinner
                    Toast.makeText(this@ItemRegistryListActivity, "Failed to load rooms", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Room>>, t: Throwable) {
                onComplete(emptyList())  // Pass an empty list to clear the spinner
                Toast.makeText(this@ItemRegistryListActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }




}