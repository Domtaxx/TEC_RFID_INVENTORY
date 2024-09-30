package com.nfs.tec_rfid

import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import okhttp3.*
import okio.IOException

class NfcReadActivity : AppCompatActivity() {

    private lateinit var nfcReader: NFCReader
    private lateinit var textView: TextView
    private lateinit var returnButton: Button
    private lateinit var dbService: AzureDatabaseService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_read)
        val dbService = AzureDatabaseService(this)

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
        // Use coroutines to call the connectToDatabase function
        lifecycleScope.launch {
            val connection = dbService.connectWithRetry()
            if (connection != null && dbService.isConnectionValid(connection)) {
                // Use the connection to execute queries or transactions
                val test = null;
            }
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

        //dbService.queryDataByTagId(tagId)
    }
}
