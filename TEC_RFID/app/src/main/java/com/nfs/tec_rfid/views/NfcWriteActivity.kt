package com.nfs.tec_rfid.views

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
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

class NfcWriteActivity : AppCompatActivity() {

    private lateinit var nfcReader: NFCReader
    private lateinit var nfcAdapter: NfcAdapter
    private var nfsValue: Int? = null  // This will store the tag ID
    private var textToWrite: String? = null
    private var isWaitingForScan: Boolean = false  // To track if the app is waiting for a scan

    private lateinit var itemNameEditText: EditText
    private lateinit var summaryEditText: EditText
    private lateinit var departmentSpinner: Spinner
    private lateinit var roomSpinner: Spinner
    private lateinit var cycleSpinner: Spinner
    private lateinit var writeNfcButton: Button
    private var userToken: String? = null

    private var departments: List<Department> = listOf()
    private var rooms: List<Room> = listOf()
    private var cycles: List<Cycle> = listOf()
    private var selectedDepartmentId: Int? = null
    private var selectedRoomId: Int? = null
    private var selectedCycleId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_write)

        nfcReader = NFCReader(this)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Initialize the form views
        itemNameEditText = findViewById(R.id.item_name)
        summaryEditText = findViewById(R.id.summary)
        departmentSpinner = findViewById(R.id.department_spinner)
        roomSpinner = findViewById(R.id.room_spinner)
        cycleSpinner = findViewById(R.id.cycle_spinner)
        writeNfcButton = findViewById(R.id.write_nfc_button)

        // Fetch the user token from SharedPreferences
        userToken = getUserToken()

        // Fetch the list of departments, rooms, and cycles to populate the spinners
        fetchDepartments()
        fetchCycles()

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
        Toast.makeText(this, "Tap an NFC tag to write", Toast.LENGTH_SHORT).show()
    }

    fun onTagDiscovered(tag: Tag) {
        // Extract the tag ID as a hexadecimal string
        val tagId = tag.id.joinToString("") { byte -> "%02X".format(byte) }
        nfsValue = tagId.toIntOrNull(16) // Convert the tag ID to an integer using base 16

        if (nfsValue != null) {
            isWaitingForScan = false // Stop waiting for a scan
            Toast.makeText(this, "NFC Tag ID: $nfsValue", Toast.LENGTH_SHORT).show()
            // After the tag is read, add the item to the database
            addItemToDatabase()
        } else {
            Toast.makeText(this, "Failed to read NFC tag", Toast.LENGTH_SHORT).show()
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
                nfs = nfsValue,
                room_id = selectedRoomId!!,
                timestamp = Date(),
                token = userToken!!,
                id_cycle = selectedCycleId!!
            )
            // API call to add the item
            val call = ApiClient.instance.addItem(item)
            call.enqueue(object : Callback<ItemResponse> {
                override fun onResponse(call: Call<ItemResponse>, response: Response<ItemResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val itemId = response.body()!!.id
                        Toast.makeText(this@NfcWriteActivity, "Item added successfully with ID: $itemId", Toast.LENGTH_SHORT).show()
                        // Optionally write the item ID to the NFC tag
                        textToWrite = itemId.toString()
                    } else {
                        Toast.makeText(this@NfcWriteActivity, "Failed to add item", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ItemResponse>, t: Throwable) {
                    Toast.makeText(this@NfcWriteActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchDepartments() {
        val call = ApiClient.instance.getDepartments()
        call.enqueue(object : Callback<List<Department>> {
            override fun onResponse(call: Call<List<Department>>, response: Response<List<Department>>) {
                if (response.isSuccessful) {
                    departments = response.body() ?: listOf()
                    populateDepartmentSpinner()
                } else {
                    Toast.makeText(this@NfcWriteActivity, "Failed to load departments", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Department>>, t: Throwable) {
                Toast.makeText(this@NfcWriteActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@NfcWriteActivity, "Failed to load cycles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Cycle>>, t: Throwable) {
                Toast.makeText(this@NfcWriteActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@NfcWriteActivity, "Failed to load rooms", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Room>>, t: Throwable) {
                Toast.makeText(this@NfcWriteActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
}
