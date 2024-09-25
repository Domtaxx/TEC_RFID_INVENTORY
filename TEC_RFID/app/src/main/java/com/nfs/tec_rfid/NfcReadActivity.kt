package com.nfs.tec_rfid

import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okio.IOException

class NfcReadActivity : AppCompatActivity() {

    private lateinit var nfcReader: NFCReader
    private lateinit var textView: TextView
    private lateinit var returnButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_read)

        // Initialize UI elements
        nfcReader = NFCReader(this)
        textView = findViewById(R.id.textView_readData)
        returnButton = findViewById(R.id.btn_return_main)

        // Handle return to MainActivity
        returnButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        nfcReader.enableReaderMode()  // Enable NFC reader mode for reading
    }

    override fun onPause() {
        super.onPause()
        nfcReader.disableReaderMode()  // Disable NFC reader mode when not needed
    }

    // Method to be called when an NFC tag is detected
    fun onTagDiscovered(tag: Tag) {
        // Extract tag ID as an example (you can also extract other info from the tag)
        val tagId = tag.id.joinToString(separator = "") { String.format("%02X", it) }
        textView.text = "Tag ID: $tagId"  // Display the tag ID in the UI

        // Call the HTTPS client to send a GET request with the tag ID
        sendGetRequest(tagId)
    }

    // Function to send a GET request using OkHttp and the tag ID as a query parameter
    private fun sendGetRequest(tagId: String) {
        val client = OkHttpClient()
        val url = "https://tecrfidrestapi20240925152155.azurewebsites.net/Empleado_/Get_all_users"  // Modify with your server URL

        val request = Request.Builder()
            .url(url)
            .build()

        // Perform an asynchronous GET request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@NfcReadActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    runOnUiThread {
                        textView.text = "Server Response: $responseData"  // Update UI with server response
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@NfcReadActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
