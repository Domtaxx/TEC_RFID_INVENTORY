package com.nfs.tec_rfid.views

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.nfs.tec_rfid.NFC.NFCReader
import com.nfs.tec_rfid.R
import com.nfs.tec_rfid.models.*
import com.nfs.tec_rfid.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


data class Item_State(
    val state_name: String,
    val value: Boolean
)
class ModifyItemActivity : AppCompatActivity() {

    private lateinit var nfcReader: NFCReader
    private lateinit var nfcAdapter: NfcAdapter
    private var itemId: String? = null
    private var itemData: ItemResponse? = null

    // UI elements
    private lateinit var itemNameEditText: EditText
    private lateinit var summaryEditText: EditText
    private lateinit var SerialNumberEditText: EditText
    private lateinit var departmentSpinner: Spinner
    private lateinit var roomSpinner: Spinner
    private lateinit var stateSpinner: Spinner
    private lateinit var employeeSpinner: Spinner

    private lateinit var modifyButton: Button
    private lateinit var registerButton: Button
    private var departments: List<Department> = listOf()
    private var rooms: List<Room> = listOf()
    private var states: List<State> = listOf()
    private var employees: List<EmployeeResponse> = listOf()
    private var selectedDepartmentId: Int? = null
    private var selectedRoomId: Int? = null
    private var selectedStateId: Int? = null
    private var selectedEmployeeId: Int? = null
    private var userToken: String? = null
    private var nfcValue: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_item)

        // Initialize NFC Reader and Adapter
        nfcReader = NFCReader(this)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Initialize UI elements
        itemNameEditText = findViewById(R.id.item_name)
        summaryEditText = findViewById(R.id.summary)
        SerialNumberEditText = findViewById(R.id.serial_number)
        departmentSpinner = findViewById(R.id.department_spinner)
        roomSpinner = findViewById(R.id.room_spinner)
        this.stateSpinner = findViewById(R.id.state_spinner)
        employeeSpinner = findViewById(R.id.employee_spinner)
        modifyButton = findViewById(R.id.save_button)

        registerButton = findViewById(R.id.register_button)
        registerButton.visibility = View.GONE

        userToken = getUserToken()

        // Disable UI elements until NFC tag is scanned
        enableUIElements(false)

        modifyButton.setOnClickListener {
            modifyItemInDatabase()
        }

        registerButton.setOnClickListener {
            RegisterItemInDatabase()
        }

        // Notify user to scan an NFC tag
        Toast.makeText(this, "Por favor escanear un tag", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()

        // Set up NFC foreground dispatch for the activity
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
        val nfcIntentFilters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilters, null)
    }

    override fun onPause() {
        super.onPause()
        // Disable NFC foreground dispatch
        nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle NFC tag discovery
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                onTagDiscovered(it)
            }
        }
    }

    // Function called when an NFC tag is discovered
    fun onTagDiscovered(tag: Tag) {
        // Attempt to read data stored in the NFC tag
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            ndef.connect()
            val ndefMessage: NdefMessage? = ndef.cachedNdefMessage
            if (ndefMessage != null) {
                // Read data from the first NDEF record
                val record: NdefRecord = ndefMessage.records[0]
                val payload = record.payload
                nfcValue = String(payload.copyOfRange(3, payload.size)) // Skipping language bytes
                playPingSound()
                fetchItemData(nfcValue!!)

            } else {
                Toast.makeText(this, "No NDEF message found on the tag", Toast.LENGTH_SHORT).show()
            }
            ndef.close()
        } else {
            Toast.makeText(this, "NDEF is not supported by this tag", Toast.LENGTH_SHORT).show()
        }
    }

    // Enable or disable UI elements
    private fun enableUIElements(enable: Boolean) {
        itemNameEditText.isEnabled = enable
        summaryEditText.isEnabled = enable
        departmentSpinner.isEnabled = enable
        employeeSpinner.isEnabled = enable
        roomSpinner.isEnabled = enable
        this.stateSpinner.isEnabled = enable
        this.stateSpinner.isEnabled = enable
        modifyButton.isEnabled = enable
        registerButton.isEnabled = enable
    }

    // Fetch item data from the backend using the NFC tag's NFS value
    private fun fetchItemData(nfsValue: String) {
        val call = ApiClient.instance.getItemById(nfsValue)
        call.enqueue(object : Callback<ItemResponse> {
            override fun onResponse(call: Call<ItemResponse>, response: Response<ItemResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    itemData = response.body()
                    itemId = itemData?.id.toString()

                    // Populate UI fields with the retrieved item data
                    populateUIWithData(itemData)

                    // Enable UI elements for modification
                    enableUIElements(true)
                } else {
                    Toast.makeText(this@ModifyItemActivity, "Item not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ItemResponse>, t: Throwable) {
                Toast.makeText(this@ModifyItemActivity, "Error fetching item: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Populate the UI fields with the item data
    private fun populateUIWithData(item: ItemResponse?) {
        item?.let {
            // Set text fields
            itemNameEditText.setText(it.item_name)
            summaryEditText.setText(it.summary)
            SerialNumberEditText.setText(it.serial_number)

            // Fetch departments and states first
            fetchDepartments {
                // Once departments are fetched, set the department selection
                val departmentId = it.id_department
                val departmentIndex = departments.indexOfFirst { department -> department.id == departmentId }
                if (departmentIndex >= 0) {
                    departmentSpinner.setSelection(departmentIndex)
                    // Fetch employees for the selected department after setting the department
                    fetchEmployeesByDepartment(departmentId) {
                        // After employees are fetched, set the employee selection
                        val employeeId = it.id_employee
                        val employeeIndex = employees.indexOfFirst { employee -> employee.id == employeeId }
                        if (employeeIndex >= 0) {
                            employeeSpinner.setSelection(employeeIndex)
                        }
                    }
                }
            }

            fetchStates {
                // Set the state spinner after states are fetched
                val stateId = it.id_state
                val stateIndex = states.indexOfFirst { state -> state.id == stateId }
                if (stateIndex >= 0) {
                    stateSpinner.setSelection(stateIndex)
                }
            }
        }
    }
    private fun fetchDepartments(onComplete: () -> Unit) {
        val call = ApiClient.instance.getDepartments()
        call.enqueue(object : Callback<List<Department>> {
            override fun onResponse(call: Call<List<Department>>, response: Response<List<Department>>) {
                if (response.isSuccessful) {
                    departments = response.body() ?: listOf()
                    populateDepartmentSpinner()
                    onComplete() // Call the callback when departments are fetched
                } else {
                    Toast.makeText(this@ModifyItemActivity, "Failed to load departments", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Department>>, t: Throwable) {
                Toast.makeText(this@ModifyItemActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchEmployeesByDepartment(departmentId: Int, onComplete: () -> Unit) {
        val call = ApiClient.instance.getEmployeesByDepartment(departmentId)
        call.enqueue(object : Callback<List<EmployeeResponse>> {
            override fun onResponse(call: Call<List<EmployeeResponse>>, response: Response<List<EmployeeResponse>>) {
                if (response.isSuccessful) {
                    employees = response.body() ?: listOf()
                    populateEmployeeSpinner()
                    onComplete() // Call the callback when employees are fetched
                } else {
                    employees = listOf() // Empty the employee list
                    populateEmployeeSpinner() // Refresh the Spinner with empty data
                    Toast.makeText(this@ModifyItemActivity, "No employees found in the department", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<EmployeeResponse>>, t: Throwable) {
                employees = listOf() // Empty the employee list
                populateEmployeeSpinner() // Refresh the Spinner with empty data
                Toast.makeText(this@ModifyItemActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchStates(onComplete: () -> Unit) {
        val call = ApiClient.instance.getStates()
        call.enqueue(object : Callback<List<State>> {
            override fun onResponse(call: Call<List<State>>, response: Response<List<State>>) {
                if (response.isSuccessful) {
                    states = response.body() ?: listOf()
                    populateStateSpinner()
                    onComplete() // Call the callback when states are fetched
                } else {
                    Toast.makeText(this@ModifyItemActivity, "Failed to load States", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<State>>, t: Throwable) {
                Toast.makeText(this@ModifyItemActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun populateStateSpinner() {
        val stateNames = states.map { it.state_name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stateNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        stateSpinner.adapter = adapter

        stateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedStateId = states[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedStateId = null
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
                fetchEmployeesByDepartment(departments[position].id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedDepartmentId = null
            }
        }
    }

    private fun fetchEmployeesByDepartment(departmentId: Int) {
        val call = ApiClient.instance.getEmployeesByDepartment(departmentId)
        call.enqueue(object : Callback<List<EmployeeResponse>> {
            override fun onResponse(call: Call<List<EmployeeResponse>>, response: Response<List<EmployeeResponse>>) {
                if (response.isSuccessful) {
                    employees = response.body() ?: listOf()
                    populateEmployeeSpinner()
                } else {
                    employees = listOf() // Empty the employee list
                    populateEmployeeSpinner() // Refresh the Spinner with empty data
                    Toast.makeText(this@ModifyItemActivity, "No employees found in the department", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<EmployeeResponse>>, t: Throwable) {
                employees = listOf() // Empty the employee list
                populateEmployeeSpinner() // Refresh the Spinner with empty data
                Toast.makeText(this@ModifyItemActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateEmployeeSpinner() {
        val employeeNames = employees.map { "${it.first_name} ${it.surname}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, employeeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        employeeSpinner.adapter = adapter

        employeeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedEmployeeId = employees[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedEmployeeId = null
            }
        }
    }

    private fun fetchRoomsByDepartment(departmentId: Int) {
        val call = ApiClient.instance.getRoomsByDepartment(departmentId)
        call.enqueue(object : Callback<List<Room>> {
            override fun onResponse(call: Call<List<Room>>, response: Response<List<Room>>) {
                if (response.isSuccessful) {
                    rooms = response.body() ?: listOf()
                    populateRoomSpinner()
                } else {
                    rooms = listOf() // Empty the room list
                    populateRoomSpinner() // Refresh the Spinner with empty data
                    Toast.makeText(this@ModifyItemActivity, "No rooms found in the department", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Room>>, t: Throwable) {
                rooms = listOf() // Empty the room list
                populateRoomSpinner() // Refresh the Spinner with empty data
                Toast.makeText(this@ModifyItemActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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

    private fun getUserToken(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_token", null)
    }

    private fun playPingSound() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.ding)
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener {
            it.release()
        }
    }

    private fun modifyItemInDatabase() {
        // Ensure valid selections and non-empty lists
        if (departmentSpinner.selectedItemPosition < 0 || rooms.isEmpty() || employees.isEmpty() || states.isEmpty()) {
            Toast.makeText(this, "Please select valid items for all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Safely get selected IDs from spinners only if valid
        val selectedDepartmentId = departments.getOrNull(departmentSpinner.selectedItemPosition)?.id
        val selectedRoomId = rooms.getOrNull(roomSpinner.selectedItemPosition)?.id
        val selectedStateId = states.getOrNull(stateSpinner.selectedItemPosition)?.id
        val selectedEmployeeId = employees.getOrNull(employeeSpinner.selectedItemPosition)?.id

        // Verify that all selections are valid before proceeding
        if (selectedDepartmentId == null || selectedRoomId == null || selectedStateId == null || selectedEmployeeId == null) {
            Toast.makeText(this, "Por favor rellenar todos los espacios", Toast.LENGTH_SHORT).show()
            return
        }

        val modifiedItem = ItemCreate(
            item_name = itemNameEditText.text.toString(),
            summary = summaryEditText.text.toString(),
            serial_number = SerialNumberEditText.text.toString(),  // Ensure serial number is retrieved
            id_department = selectedDepartmentId,
            nfs = nfcValue ?: "",  // Handle potential nulls
            timestamp = getFormattedTimestamp(),
            room_id = selectedRoomId,
            id_state = selectedStateId,
            id_employee = selectedEmployeeId,  // Pass the selected employee ID
            token = userToken ?: ""  // Handle null safety for userToken
        )

        val call = ApiClient.instance.updateItem(itemId!!.toInt(), modifiedItem)
        call.enqueue(object : Callback<ItemResponse> {
            override fun onResponse(call: Call<ItemResponse>, response: Response<ItemResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ModifyItemActivity, "Item updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ModifyItemActivity, "Failed to update item", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ItemResponse>, t: Throwable) {
                Toast.makeText(this@ModifyItemActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun RegisterItemInDatabase() {
        // Ensure valid selections and non-empty lists
        if (departmentSpinner.selectedItemPosition < 0 || rooms.isEmpty() || employees.isEmpty() || states.isEmpty()) {
            Toast.makeText(this, "Por favor rellenar todos los espacios", Toast.LENGTH_SHORT).show()
            return
        }

        // Safely get selected IDs from spinners only if valid
        val selectedDepartmentId = departments.getOrNull(departmentSpinner.selectedItemPosition)?.id
        val selectedRoomId = rooms.getOrNull(roomSpinner.selectedItemPosition)?.id
        val selectedStateId = states.getOrNull(stateSpinner.selectedItemPosition)?.id
        val selectedEmployeeId = employees.getOrNull(employeeSpinner.selectedItemPosition)?.id

        // Verify that all selections are valid before proceeding
        if (selectedDepartmentId == null || selectedRoomId == null || selectedStateId == null) {
            Toast.makeText(this, "Invalid selection, please ensure all fields are correctly filled", Toast.LENGTH_SHORT).show()
            return
        }

        val modifiedItem = ItemCreate(
            item_name = itemNameEditText.text.toString(),
            summary = summaryEditText.text.toString(),
            serial_number = SerialNumberEditText.text.toString(),  // Ensure serial number is retrieved
            id_department = selectedDepartmentId,
            nfs = nfcValue ?: "",  // Handle potential nulls
            timestamp = getFormattedTimestamp(),
            room_id = selectedRoomId,
            id_state = selectedStateId,
            id_employee = -1,  // Pass the selected employee ID
            token = userToken ?: ""  // Handle null safety for userToken
        )

        val call = ApiClient.instance.registerItem(itemId!!.toInt(), modifiedItem)
        call.enqueue(object : Callback<ItemResponse> {
            override fun onResponse(call: Call<ItemResponse>, response: Response<ItemResponse>) {
                when {
                    response.isSuccessful && response.body() != null -> {
                        Toast.makeText(this@ModifyItemActivity, "Activo registrado exitosamente", Toast.LENGTH_SHORT).show()
                    }

                    response.code() == 401 -> {
                        Toast.makeText(this@ModifyItemActivity, "El tag esta inactivo", Toast.LENGTH_SHORT).show()
                        playPingSound()
                    }

                    else -> {
                        Toast.makeText(this@ModifyItemActivity, "Error al registrar activo", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ItemResponse>, t: Throwable) {
                Toast.makeText(this@ModifyItemActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // Get formatted timestamp for the current time
    private fun getFormattedTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date())
    }
}



