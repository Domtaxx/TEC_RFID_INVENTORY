package com.nfs.tec_rfid.views

import android.Manifest
import android.content.Context
import android.util.Log
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nfs.tec_rfid.R
import com.nfs.tec_rfid.models.Department
import com.nfs.tec_rfid.models.EmployeeResponse
import com.nfs.tec_rfid.models.Room
import com.nfs.tec_rfid.network.ApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportActivity : AppCompatActivity() {

    private lateinit var departmentSpinner: Spinner
    private lateinit var employeeSpinner: Spinner
    private lateinit var roomSpinner: Spinner
    private lateinit var report1Button: Button
    private lateinit var report2Button: Button
    private lateinit var report3Button: Button

    private var departments: List<Department> = listOf()
    private var employees: List<EmployeeResponse> = listOf()
    private var rooms: List<Room> = listOf()
    private var roomList: List<Room> = listOf()
    private var selectedDepartmentId: Int? = null
    private var selectedRoom: Room? = null
    private var selectedEmployeeId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // Initialize views
        departmentSpinner = findViewById(R.id.department_spinner)
        employeeSpinner = findViewById(R.id.employee_spinner)
        roomSpinner = findViewById(R.id.room_spinner)
        report1Button = findViewById(R.id.report1_button)
        report2Button = findViewById(R.id.report2_button)
        report3Button = findViewById(R.id.report3_button)

        // Fetch departments and set up the spinner
        fetchDepartments()

        // Set up button click listeners (replace with actual report generation logic)
        report1Button.setOnClickListener {
            Toast.makeText(this, "Generating Report 1", Toast.LENGTH_SHORT).show()
            generateItemsByDepartmentReport(selectedDepartmentId!!)
        }

        report2Button.setOnClickListener {
            Toast.makeText(this, "Generating Report 2", Toast.LENGTH_SHORT).show()
            fetchRoomReport(selectedRoom!!.id)
        }

        report3Button.setOnClickListener {
            Toast.makeText(this, "Generating Report 3", Toast.LENGTH_SHORT).show()
            fetchEmployeeReport(selectedEmployeeId!!)
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
                    Toast.makeText(this@ReportActivity, "Error al cargar los departamentos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Department>>, t: Throwable) {
                Toast.makeText(this@ReportActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateDepartmentSpinner() {
        val departmentNames = departments.map { it.department_name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departmentNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        departmentSpinner.adapter = adapter

        // Handle department selection
        departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedDepartmentId = departments[position].id
                // Fetch employees and rooms for the selected department
                fetchEmployeesByDepartment(selectedDepartmentId!!)

                // Pass the context and roomSpinner as arguments
                fetchRoomsByDepartment(selectedDepartmentId!!, this@ReportActivity, roomSpinner)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle no selection case
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
                    Toast.makeText(this@ReportActivity, "Error al cargar usuarios", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<EmployeeResponse>>, t: Throwable) {
                Toast.makeText(this@ReportActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun generateItemsByDepartmentReport(departmentId: Int) {
        val call = ApiClient.instance.getItemsByDepartmentReport(departmentId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // Save or display the file
                    val file = response.body()?.byteStream()?.let {
                        saveReportToDisk(it, "items_by_department.xlsx")
                    }
                    Toast.makeText(this@ReportActivity, "El reporte se generó correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ReportActivity, "Error al generar el archivo", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ReportActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun saveReportToDisk(inputStream: InputStream, fileName: String): Boolean {
        try {
            // Check if external storage is available
            val externalStorageState = Environment.getExternalStorageState()
            if (externalStorageState != Environment.MEDIA_MOUNTED) {
                Toast.makeText(this, "No hay suficiente espacio para descargar el archivo", Toast.LENGTH_SHORT).show()
                return false
            }

            // Get the Downloads directory
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }

            // Create a unique file name with a timestamp
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val reportFile = File(downloadDir, "${fileName}_$timeStamp.xlsx")

            // Write the input stream data to the file
            val outputStream: OutputStream = FileOutputStream(reportFile)
            val buffer = ByteArray(4096)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            Toast.makeText(this, "Reporte se guardo en descargas", Toast.LENGTH_SHORT).show()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error guardando reporte: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    fun fetchEmployeeReport(empployeeID: Int){
        val call = ApiClient.instance.getEmployeeReport(empployeeID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // Save the file to the device
                    val isSaved = response.body()?.byteStream()?.let {
                        saveReportToDisk(it, "Activos_por_empleado.xlsx")
                    }
                    if (isSaved == true) {
                        Log.d("Reporte por empleado", "El reporte se guardo de forma exitosa.")
                    } else {
                        Log.e("Reporte por empleado", "Error al guardar reporte")
                    }
                } else {
                    Log.e("Reporte por empleado", "Error al descargar reporte")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Reporte por empleado", "Error: ${t.message}")
            }
        })
    }
    fun fetchRoomReport(roomId: Int) {
        // Make the API call to fetch the room report
        val call = ApiClient.instance.getRoomReport(roomId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // Save the file to the device
                    val isSaved = response.body()?.byteStream()?.let {
                        saveReportToDisk(it, "Activos_por_habitacion.xlsx")
                    }
                    if (isSaved == true) {
                        Log.d("RoomReport", "Report saved successfully.")
                    } else {
                        Log.e("RoomReport", "Failed to save the report.")
                    }
                } else {
                    Log.e("RoomReport", "Failed to download report.")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("RoomReport", "Error: ${t.message}")
            }
        })
    }


    private fun fetchRoomsByDepartment(departmentId: Int, context: Context, roomSpinner: Spinner) {
        val call = ApiClient.instance.getRoomsByDepartment(departmentId)
        call.enqueue(object : Callback<List<Room>> {
            override fun onResponse(call: Call<List<Room>>, response: Response<List<Room>>) {
                if (response.isSuccessful) {
                    rooms = response.body() ?: listOf()  // Store the RoomResponse objects
                    populateRoomSpinner(rooms)
                } else {
                    Toast.makeText(context, "Error al cargar habitaciones", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Room>>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()

                // Explicitly set the type to String for the empty list adapter
                val emptyAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listOf())
                emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                roomSpinner.adapter = emptyAdapter  // Set the empty adapter to the spinner
            }
        })
    }

    private fun populateRoomSpinner(rooms: List<Room>) {
        val roomNames = rooms.map { it.room_name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roomNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roomSpinner.adapter = adapter

        // Handle room selection
        roomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedRoom = rooms[position]  // Update the selectedRoom with the RoomResponse object
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle no selection case
                selectedRoom = null  // Clear the selection
            }
        }
    }

    private fun populateEmployeeSpinner() {
        val employeeNames = employees.map { "${it.first_name} ${it.surname}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, employeeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        employeeSpinner.adapter = adapter
        // Handle room selection
        employeeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedEmployeeId = employees[position].id  // Update the selectedRoom with the RoomResponse object
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle no selection case
                selectedEmployeeId = null  // Clear the selection
            }
        }
    }

    private val STORAGE_PERMISSION_CODE = 100

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        } else {
            // Permission already granted, proceed with the report saving
            //Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                //Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                Toast.makeText(this, "La app no tiene permiso de guardar archivos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
