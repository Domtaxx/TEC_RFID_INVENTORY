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

    private var itemId: String? = null // Store the item ID to be written
    private lateinit var nfcReader: NFCReader
    private lateinit var nfcAdapter: NfcAdapter
    private var nfsValue: String? = null  // This will store the tag ID
    private var textToWrite: String? = null
    private var isWaitingForScan: Boolean = false  // To track if the app is waiting for a scan

    private lateinit var itemNameEditText: EditText
    private lateinit var summaryEditText: EditText
    private lateinit var departmentSpinner: Spinner
    private lateinit var roomSpinner: Spinner
    private lateinit var cycleSpinner: Spinner
    private lateinit var writeNfcButton: Button
    private lateinit var stopButton: Button
    private var userToken: String? = null

    private var departments: List<Department> = listOf()
    private var rooms: List<Room> = listOf()
    private var cycles: List<Cycle> = listOf()
    private var selectedDepartmentId: Int? = null
    private var selectedRoomId: Int? = null
    private var selectedCycleId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_activos)

        nfcReader = NFCReader(this)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Initialize the form views
        itemNameEditText = findViewById(R.id.item_name)
        summaryEditText = findViewById(R.id.summary)
        departmentSpinner = findViewById(R.id.department_spinner)
        roomSpinner = findViewById(R.id.room_spinner)
        cycleSpinner = findViewById(R.id.cycle_spinner)
        writeNfcButton = findViewById(R.id.write_nfc_button)
        stopButton = findViewById(R.id.stop_nfc_button)

        // Fetch the user token from SharedPreferences
        userToken = getUserToken()

        // Fetch the list of departments, rooms, and cycles to populate the spinners
        fetchDepartments()
        fetchCycles()
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
        // Handle NFC tag discovery if we are waiting for a scan
        if (isWaitingForScan && (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
                    NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action ||
                    NfcAdapter.ACTION_TECH_DISCOVERED == intent.action)) {
            // Get the NFC tag from the intent
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
        if(isWaitingForScan){
            // Try to read the data stored in the tag
            val ndef = Ndef.get(tag) // Get the NDEF instance for this tag
            if (ndef != null) {
                ndef.connect() // Connect to the tag
                val ndefMessage: NdefMessage? = ndef.cachedNdefMessage // Retrieve the cached NDEF message
                if (ndefMessage != null) {
                    // Read the data from the first NDEF record
                    val record: NdefRecord = ndefMessage.records[0]
                    val payload = record.payload
                    val storedData = String(payload) // Convert the payload to a string

                    // Process the stored data as needed
                    nfsValue = storedData
                } else {
                    Toast.makeText(this, "No NDEF message found on the tag", Toast.LENGTH_SHORT).show()
                }
                ndef.close() // Close the connection to the tag
            } else {
                Toast.makeText(this, "NDEF is not supported by this tag", Toast.LENGTH_SHORT).show()
            }
            // After the tag is read, add the item to the database
            addItemToDatabase()
            // Continue with writing the item ID to the NFC tag
            itemId?.let {
                nfcReader.writeTag(tag, it)  // Write the item ID to the NFC tag
            }
        }
        else {
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

        if (itemName.isNotEmpty() && nfsValue != null && selectedDepartmentId != null && selectedRoomId != null && selectedCycleId != null && userToken != null) {
            val item = ItemCreate(
                item_name = itemName,
                summary = summary,
                id_department = selectedDepartmentId!!,
                nfs = nfcReader.stripLanguageBytes(nfsValue),
                room_id = selectedRoomId!!,
                timestamp = getFormattedTimestamp(),
                token = userToken!!,
                state = true,
                id_cycle = selectedCycleId!!
            )
            // API call to add the item
            val call = ApiClient.instance.addItem(item)
            call.enqueue(object : Callback<ItemResponse> {
                override fun onResponse(call: Call<ItemResponse>, response: Response<ItemResponse>) {
                    when {
                        response.isSuccessful && response.body() != null -> {
                            // Handle successful response
                            val itemId = response.body()!!.id
                            Toast.makeText(this@AgregarActivosActivity, "El activo fue registrado con exito", Toast.LENGTH_SHORT).show()
                            this@AgregarActivosActivity.itemId = itemId.toString()
                            playPingSound()
                            // Optionally write the item ID to the NFC tag
                            textToWrite = itemId.toString()
                        }
                        response.code() == 401 -> {
                            // Handle 401 Unauthorized error
                            Toast.makeText(this@AgregarActivosActivity, "Tag ya ha sido registrado anteriormente", Toast.LENGTH_SHORT).show()
                            playPingSound()
                            // Optionally, redirect to login activity or handle reauthentication
                        }
                        else -> {
                            // Handle other errors
                            playPingSound()
                            Toast.makeText(this@AgregarActivosActivity, "Hubo un error al registrar el activo, por favor volver a intentarlo", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onFailure(call: Call<ItemResponse>, t: Throwable) {
                    // Handle failure
                    Toast.makeText(this@AgregarActivosActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Por favor llenar todos los campos", Toast.LENGTH_SHORT).show()
        }
    }
    fun getFormattedTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC") // Set the time zone to UTC
        return dateFormat.format(Date()) // Format the current date
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

    private fun fetchCycles() {
        val call = ApiClient.instance.getCycles()
        call.enqueue(object : Callback<List<Cycle>> {
            override fun onResponse(call: Call<List<Cycle>>, response: Response<List<Cycle>>) {
                if (response.isSuccessful) {
                    cycles = response.body() ?: listOf()
                    populateCycleSpinner()
                } else {
                    Toast.makeText(this@AgregarActivosActivity, "Failed to load cycles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Cycle>>, t: Throwable) {
                Toast.makeText(this@AgregarActivosActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
                fetchRoomsByDepartment(departments[position].id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedDepartmentId = null
            }
        }
    }

    private fun populateCycleSpinner() {
        val cycleNames = cycles.map { it.cycle_name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cycleNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cycleSpinner.adapter = adapter

        cycleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCycleId = cycles[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCycleId = null
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
                    Toast.makeText(this@AgregarActivosActivity, "Failed to load rooms", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Room>>, t: Throwable) {
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
        mediaPlayer?.start() // Play the sound
        mediaPlayer?.setOnCompletionListener {
            it.release() // Release resources once playback is complete
        }
    }

}
