package com.nfs.tec_rfid.views

import android.content.Context
import android.content.Intent
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
import com.nfs.tec_rfid.NFC.NFCReader
import com.nfs.tec_rfid.R
import com.nfs.tec_rfid.models.*
import com.nfs.tec_rfid.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AgregarActivosActivity : AppCompatActivity() {

    private var itemId: String? = null
    private lateinit var nfcReader: NFCReader
    private lateinit var nfcAdapter: NfcAdapter
    private var nfsValue: String? = null
    private var textToWrite: String? = null
    private var isWaitingForScan: Boolean = false

    private lateinit var itemNameEditText: EditText
    private lateinit var summaryEditText: EditText
    private lateinit var SerialNumberEditText: EditText
    private lateinit var departmentSpinner: Spinner
    private lateinit var roomSpinner: Spinner
    private lateinit var stateSpinner: Spinner
    private lateinit var employeeSpinner: Spinner
    private lateinit var writeNfcButton: Button
    private lateinit var stopButton: Button
    private var userToken: String? = null

    private var departments: List<Department> = listOf()
    private var rooms: List<Room> = listOf()
    private var states: List<State> = listOf()
    private var employees: List<EmployeeResponse> = listOf()
    private var selectedDepartmentId: Int? = null
    private var selectedRoomId: Int? = null
    private var selectedStateId: Int? = null
    private var selectedEmployeeId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_activos)

        nfcReader = NFCReader(this)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Initialize the form views
        itemNameEditText = findViewById(R.id.item_name)
        summaryEditText = findViewById(R.id.summary)
        SerialNumberEditText = findViewById(R.id.serial_number)
        departmentSpinner = findViewById(R.id.department_spinner)
        roomSpinner = findViewById(R.id.room_spinner)
        stateSpinner = findViewById(R.id.state_spinner)
        employeeSpinner = findViewById(R.id.employee_spinner)
        writeNfcButton = findViewById(R.id.write_nfc_button)
        stopButton = findViewById(R.id.stop_nfc_button)

        // Fetch the user token from SharedPreferences
        userToken = getUserToken()

        // Fetch the list of departments, rooms, states, and employees to populate the spinners
        fetchDepartments()
        fetchStates()
        stopButton.setOnClickListener {
            stopWaitingForScan()
        }
        // Set up the button click listener
        writeNfcButton.setOnClickListener {
            startWaitingForScan()
        }
    }

    override fun onResume() {
        super.onResume()
        nfcReader.enableReaderMode()
    }

    override fun onPause() {
        super.onPause()
        nfcReader.disableReaderMode()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (isWaitingForScan && (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
                    NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action ||
                    NfcAdapter.ACTION_TECH_DISCOVERED == intent.action)) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                onTagDiscovered(it)
            }
        }
    }

    private fun startWaitingForScan() {
        isWaitingForScan = true
        Toast.makeText(this, "Acercar Tag para empezar a registrar", Toast.LENGTH_SHORT).show()
    }

    private fun stopWaitingForScan() {
        isWaitingForScan = false
        Toast.makeText(this, "Ya no se leeran más tags", Toast.LENGTH_SHORT).show()
    }

    fun onTagDiscovered(tag: Tag) {
        if (isWaitingForScan) {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                val ndefMessage: NdefMessage? = ndef.cachedNdefMessage
                if (ndefMessage != null) {
                    val record: NdefRecord = ndefMessage.records[0]
                    val payload = record.payload
                    val storedData = String(payload)
                    nfsValue = storedData
                } else {
                    Toast.makeText(this, "No NDEF message found on the tag", Toast.LENGTH_SHORT).show()
                }
                ndef.close()
            } else {
                Toast.makeText(this, "NDEF is not supported by this tag", Toast.LENGTH_SHORT).show()
            }
            addItemToDatabase()
            itemId?.let {
                nfcReader.writeTag(tag, it)
            }
        } else {
            Toast.makeText(this, "Por favor tocar el boton de escritura para empezar a registrar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUserToken(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_token", null)
    }

    private fun addItemToDatabase() {
        val itemName = itemNameEditText.text.toString()
        val summary = summaryEditText.text.toString()
        val serial_number = SerialNumberEditText.text.toString()
        if (itemName.isNotEmpty() && nfsValue != null && selectedDepartmentId != null && selectedRoomId != null && selectedStateId != null && userToken != null && selectedEmployeeId != null) {
            val item = ItemCreate(
                item_name = itemName,
                summary = summary,
                serial_number = serial_number,
                id_employee = selectedEmployeeId!!,
                id_department = selectedDepartmentId!!,
                nfs = nfcReader.stripLanguageBytes(nfsValue),
                room_id = selectedRoomId!!,
                timestamp = getFormattedTimestamp(),
                token = userToken!!,
                id_state = selectedStateId!!
            )
            val call = ApiClient.instance.addItem(item)
            call.enqueue(object : Callback<ItemResponse> {
                override fun onResponse(call: Call<ItemResponse>, response: Response<ItemResponse>) {
                    when {
                        response.isSuccessful && response.body() != null -> {
                            val itemId = response.body()!!.id
                            Toast.makeText(this@AgregarActivosActivity, "El activo fue registrado con exito", Toast.LENGTH_SHORT).show()
                            this@AgregarActivosActivity.itemId = itemId.toString()
                            playPingSound()
                            textToWrite = itemId.toString()
                        }
                        response.code() == 401 -> {
                            Toast.makeText(this@AgregarActivosActivity, "Tag ya ha sido registrado anteriormente", Toast.LENGTH_SHORT).show()
                            playPingSound()
                        }
                        else -> {
                            playPingSound()
                            Toast.makeText(this@AgregarActivosActivity, "Hubo un error al registrar el activo, por favor volver a intentarlo", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onFailure(call: Call<ItemResponse>, t: Throwable) {
                    Toast.makeText(this@AgregarActivosActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Por favor llenar todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFormattedTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date())
    }

    private fun fetchDepartments() {
        val call = ApiClient.instance.getDepartments()
        call.enqueue(object : Callback<List<Department>> {
            override fun onResponse(call: Call<List<Department>>, response: Response<List<Department>>) {
                if (response.isSuccessful) {
                    departments = response.body() ?: listOf()
                    populateDepartmentSpinner()
                } else {
                    Toast.makeText(this@AgregarActivosActivity, "Failed to load departments", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Department>>, t: Throwable) {
                Toast.makeText(this@AgregarActivosActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchStates() {
        val call = ApiClient.instance.getStates()
        call.enqueue(object : Callback<List<State>> {
            override fun onResponse(call: Call<List<State>>, response: Response<List<State>>) {
                if (response.isSuccessful) {
                    states = response.body() ?: listOf()
                    populateStateSpinner()
                } else {
                    Toast.makeText(this@AgregarActivosActivity, "Failed to load States", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<State>>, t: Throwable) {
                Toast.makeText(this@AgregarActivosActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@AgregarActivosActivity, "No employees found in the department", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<EmployeeResponse>>, t: Throwable) {
                employees = listOf() // Empty the employee list
                populateEmployeeSpinner() // Refresh the Spinner with empty data
                Toast.makeText(this@AgregarActivosActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@AgregarActivosActivity, "No rooms found in the department", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Room>>, t: Throwable) {
                rooms = listOf() // Empty the room list
                populateRoomSpinner() // Refresh the Spinner with empty data
                Toast.makeText(this@AgregarActivosActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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

    private fun playPingSound() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.ding)
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener {
            it.release()
        }
    }
}

