package com.nfs.tec_rfid.views

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.nfs.tec_rfid.R
import com.nfs.tec_rfid.models.Department
import com.nfs.tec_rfid.models.DepartmentCreate
import com.nfs.tec_rfid.models.DepartmentUpdate
import com.nfs.tec_rfid.models.RoleUpdateRequest
import com.nfs.tec_rfid.models.Room
import com.nfs.tec_rfid.models.RoomCreate
import com.nfs.tec_rfid.models.RoomResponse
import com.nfs.tec_rfid.models.RoomUpdate
import com.nfs.tec_rfid.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminActivity : AppCompatActivity() {

    private lateinit var roomButton: Button
    private lateinit var departmentButton: Button
    private lateinit var editRoomButton: Button
    private lateinit var editDepartmentButton: Button
    private lateinit var roleButton: Button
    private lateinit var emailEditText: EditText
    private lateinit var roleSpinner: Spinner
    private lateinit var roomSpinner: Spinner
    private lateinit var departmentSpinner: Spinner

    private var rooms: List<Room> = listOf()
    private var departments: List<Department> = listOf()
    private var selectedRoomId: Int? = null
    private var selectedDepartmentId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity)

        // Initialize UI components
        roomButton = findViewById(R.id.add_room_button)
        departmentButton = findViewById(R.id.add_department_button)
        editRoomButton = findViewById(R.id.edit_room_button)
        editDepartmentButton = findViewById(R.id.edit_department_button)
        roleButton = findViewById(R.id.update_role_button)
        emailEditText = findViewById(R.id.user_email_editText)
        roleSpinner = findViewById(R.id.role_spinner)
        roomSpinner = findViewById(R.id.room_spinner)
        departmentSpinner = findViewById(R.id.department_spinner)


        // Populate role spinner with Admin/Regular options
        populateRoleSpinner()
        // Fetch and populate room and department spinners
        fetchDepartments()

        editRoomButton.setOnClickListener {
            if (selectedDepartmentId != null) {
                val selectedRoom = rooms[roomSpinner.selectedItemPosition]  // Get the selected room
                showEditRoomDialog(selectedRoom.id, selectedRoom.room_name) // Show the pop-up dialog when the button is clicked
            } else {
                Toast.makeText(this, "Seleccione una habitación", Toast.LENGTH_SHORT).show()
            }

        }
        // Setup button click listeners
        roomButton.setOnClickListener {
            // Code to open room management activity or fragment
            showAddRoomDialog() // Show the pop-up dialog when the button is clicked
        }

        editDepartmentButton.setOnClickListener {
            if (selectedDepartmentId != null) {
                showEditDepartmentDialog() // Show the pop-up dialog when the button is clicked
            } else {
                Toast.makeText(this, "Seleccione un departmento", Toast.LENGTH_SHORT).show()
            }
        }
        departmentButton.setOnClickListener {
            // Code to open department management activity or fragment
            showAddDepartmentDialog()
        }

        roleButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val selectedRole = roleSpinner.selectedItem.toString()

            if (email.isNotEmpty()) {
                updateUserRole(email, selectedRole)
            } else {
                Toast.makeText(this, "Por favor ingrese un correo valido", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showEditRoomDialog(roomId: Int, currentRoomName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_room, null)
        val roomNameEditText = dialogView.findViewById<EditText>(R.id.room_name_edit_text1)

        // Set current room name as a hint or pre-filled text
        roomNameEditText.setText(currentRoomName)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Edit Room")
            .setPositiveButton("Save") { _, _ ->
                val newRoomName = roomNameEditText.text.toString()
                val selectedDepartmentId = departmentSpinner.selectedItemPosition // Get the selected department
                if (newRoomName.isNotEmpty()) {
                    updateRoom(roomId, newRoomName, selectedDepartmentId)
                } else {
                    Toast.makeText(this, "Please enter a room name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun updateRoom(roomId: Int, newRoomName: String, departmentId: Int) {
        val roomUpdate = RoomUpdate(
            id = roomId,
            room_name = newRoomName,
            id_department = departmentId
        )

        val call = ApiClient.instance.updateRoom(roomUpdate)
        call.enqueue(object : Callback<RoomResponse> {
            override fun onResponse(call: Call<RoomResponse>, response: Response<RoomResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminActivity, "Room updated successfully", Toast.LENGTH_SHORT).show()
                    fetchRoomsByDepartment(departmentSpinner.selectedItemPosition+1) // Refresh the room list after update
                } else {
                    Toast.makeText(this@AdminActivity, "Failed to update room", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RoomResponse>, t: Throwable) {
                Toast.makeText(this@AdminActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddRoomDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_room, null)
        val roomNameEditText = dialogView.findViewById<EditText>(R.id.room_name_editText)
        val departmentSpinner = dialogView.findViewById<Spinner>(R.id.department_spinner)

        // Use the updated function to populate the department spinner
        populateDepartmentSpinner(departmentSpinner)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Agregar habitación")
            .setPositiveButton("Crear") { _, _ ->
                val roomName = roomNameEditText.text.toString()
                val selectedDepartmentId = departments[departmentSpinner.selectedItemPosition].id

                if (roomName.isNotEmpty()) {
                    createRoom(roomName, selectedDepartmentId)
                } else {
                    Toast.makeText(this, "Por favor ingrese el nombre de la habitación", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }

        dialogBuilder.create().show()
    }


    private fun createRoom(roomName: String, departmentId: Int) {
        val roomCreate = RoomCreate(room_name = roomName, id_department = departmentId)
        val call = ApiClient.instance.createRoom(roomCreate)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminActivity, "Habitación creada exitosamente", Toast.LENGTH_SHORT).show()
                    // Optionally refresh the room list or UI
                } else {
                    Toast.makeText(this@AdminActivity, "Error al crear habitación", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@AdminActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // Show pop-up dialog to edit department name
    private fun showEditDepartmentDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Editar departmento")

        // Set up the input field for the new department name
        val input = EditText(this)
        input.hint = "Nuevo nombre del departamento"
        input.setPadding(16, 16, 16, 16)

        dialogBuilder.setView(input)

        // Set up the buttons
        dialogBuilder.setPositiveButton("Actualizar") { dialog, which ->
            val newDepartmentName = input.text.toString()
            if (newDepartmentName.isNotEmpty()) {
                updateDepartment(selectedDepartmentId!!, newDepartmentName)
            } else {
                Toast.makeText(this, "Por favor ingrese un valor valido", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBuilder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }
    private fun populateRoleSpinner() {
        val roles = listOf("Admin", "Regular")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter
    }

    private fun fetchDepartments() {
        val call = ApiClient.instance.getDepartments()
        call.enqueue(object : Callback<List<Department>> {
            override fun onResponse(call: Call<List<Department>>, response: Response<List<Department>>) {
                if (response.isSuccessful) {
                    departments = response.body() ?: listOf()
                    populateDepartmentSpinner()
                } else {
                    Toast.makeText(this@AdminActivity, "Error al cargar departamentos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Department>>, t: Throwable) {
                Toast.makeText(this@AdminActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateRoomSpinner() {
        val roomNames = rooms.map { it.room_name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roomNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roomSpinner.adapter = adapter

        roomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedRoomId = rooms[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedRoomId = null
            }
        }
    }
    private fun populateDepartmentSpinner(departmentSpinner_popup: Spinner) {
        val departmentNames = departments.map { it.department_name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departmentNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        departmentSpinner_popup.adapter = adapter

        departmentSpinner_popup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Perform any action when a department is selected if needed
                selectedDepartmentId = departments[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle the case where nothing is selected
                selectedDepartmentId = null
            }
        }
    }

    private fun populateDepartmentSpinner() {
        val departmentNames = departments.map { it.department_name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departmentNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        departmentSpinner.adapter = adapter

        departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedDepartmentId = departments[position].id
                fetchRoomsByDepartment(departments[position].id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedDepartmentId = null
            }
        }
    }
    private fun fetchRoomsByDepartment(departmentId: Int) {
        val call = ApiClient.instance.getRoomsByDepartment(departmentId)
        call.enqueue(object : Callback<List<Room>> {
            override fun onResponse(call: Call<List<Room>>, response: Response<List<Room>>) {
                if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                    rooms = response.body() ?: listOf()
                    populateRoomSpinner()
                } else {
                    rooms = listOf() // Vacía la lista de habitaciones
                    populateRoomSpinner() // Vuelve a llamar para refrescar el Spinner vacío
                    Toast.makeText(this@AdminActivity, "No hay habitaciones en el departamentos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Room>>, t: Throwable) {
                rooms = listOf() // Vacía la lista de habitaciones
                populateRoomSpinner() // Vuelve a llamar para refrescar el Spinner vacío
                Toast.makeText(this@AdminActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserRole(email: String, role: String) {
        val roleUpdateRequest = RoleUpdateRequest(email = email, role = role)
        val call = ApiClient.instance.updateUserRole(roleUpdateRequest)
        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminActivity, "El usuario se ha actualizado con exito", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AdminActivity, "Error al actualizar el rol del usuario", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Toast.makeText(this@AdminActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun updateDepartment(departmentId: Int, newDepartmentName: String) {
        val departmentUpdate = DepartmentUpdate(department_name = newDepartmentName)
        val call = ApiClient.instance.updateDepartment(departmentId, departmentUpdate)
        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminActivity, "Department actualizado correctamente", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        fetchDepartments() // Refresh the department list
                    }, 2000)
                    fetchDepartments()  // Refresh the department spinner after update
                } else {
                    Toast.makeText(this@AdminActivity, "Error al actualizar el departamento", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Toast.makeText(this@AdminActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun showAddDepartmentDialog() {
        // Crear un AlertDialog Builder
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_department, null)
        dialogBuilder.setView(dialogView)

        val departmentNameEditText = dialogView.findViewById<EditText>(R.id.department_name_editText)

        dialogBuilder.setTitle("Agregar Departamento")
        dialogBuilder.setMessage("Ingrese el nombre del nuevo departamento")
        dialogBuilder.setPositiveButton("Agregar") { _, _ ->
            val departmentName = departmentNameEditText.text.toString()
            if (departmentName.isNotEmpty()) {
                addDepartment(departmentName)
            } else {
                Toast.makeText(this, "El nombre del departamento no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }
        dialogBuilder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    // Función para agregar un departamento
    private fun addDepartment(departmentName: String) {
        val department = DepartmentCreate(department_name = departmentName)
        val call = ApiClient.instance.addDepartment(department)
        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminActivity, "Departamento agregado con éxito", Toast.LENGTH_SHORT).show()

                    // Añadir retraso de 5 segundos antes de refrescar el listado
                    Handler(Looper.getMainLooper()).postDelayed({
                        fetchDepartments() // Refrescar la lista de departamentos
                    }, 5000) // 5000 milisegundos = 5 segundos

                } else {
                    Toast.makeText(this@AdminActivity, "Error al agregar el departamento", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Toast.makeText(this@AdminActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}


