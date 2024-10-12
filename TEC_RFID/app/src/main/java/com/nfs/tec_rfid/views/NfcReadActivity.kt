package com.nfs.tec_rfid.views

import android.content.Intent
import android.media.MediaPlayer
import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nfs.tec_rfid.NFC.NFCReader
import com.nfs.tec_rfid.R
import java.io.IOException

class NfcReadActivity : AppCompatActivity() {

    private lateinit var nfcReader: NFCReader
    private lateinit var textView: TextView
    private lateinit var lastTagInfo: TextView
    private lateinit var returnButton: Button
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var spinner: Spinner
    private var isItemSelected: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_read)
        spinner = findViewById(R.id.spinner_selection)
        val spinnerItems = listOf("Seleccione una opción", "Periodo 1", "Periodo 2", "Periodo 3")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        // Handle spinner selection changes
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                isItemSelected = position != 0  // Ensure first item "Select an option" is not selected
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                isItemSelected = false
            }
        }
        // Initialize the MediaPlayer with the "ding" sound
        mediaPlayer = MediaPlayer.create(this, R.raw.ding)

        // Initialize UI elements
        nfcReader = NFCReader(this)
        textView = findViewById(R.id.textView_readData)
        lastTagInfo = findViewById(R.id.textView_lastTag)  // Add a new TextView for last tag info
        returnButton = findViewById(R.id.btn_return_main)

        // Handle return to MainActivity
        returnButton.setOnClickListener {
            val intent = Intent(this, ActivosMenuActivity::class.java)
            startActivity(intent)
            finish()
        }
        /*
        // Use coroutines to call the connectToDatabase function
        lifecycleScope.launch {
            val connection = dbService.connectWithRetry()
            if (connection != null && dbService.isConnectionValid(connection)) {
                textView.text = "Connected to the database"
            } else {
                textView.text = "Failed to connect to the database"
            }
        }
         */
    }

    override fun onResume() {
        super.onResume()
        nfcReader.enableReaderMode()  // Enable NFC reader mode for reading
    }

    override fun onPause() {
        super.onPause()
        nfcReader.disableReaderMode()  // Disable NFC reader mode when not needed
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        nfcReader.disableReaderMode()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }

    // Method to be called when an NFC tag is detected
    fun onTagDiscovered(tag: Tag) {
        if (!isItemSelected) {
            lastTagInfo.text = "Por favor seleccione un periodo valido."
            return  // Exit the function if no item is selected
        }
        mediaPlayer.start()  // Play the "ding" sound

        // Extract tag ID
        val tagId = tag.id.joinToString(separator = "") { String.format("%02X", it) }
        textView.text = "Current Tag ID: $tagId"

        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()

                val ndefMessage: NdefMessage? = ndef.cachedNdefMessage
                if (ndefMessage != null) {
                    val records = ndefMessage.records
                    for (record in records) {
                        if (record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_TEXT)) {
                            val payload = record.payload

                            // Ensure payload has a valid size
                            if (payload.size > 0) {
                                val textEncoding = if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16"
                                val languageCodeLength = payload[0].toInt() and 63

                                // Check if the payload is large enough to contain the language code and actual text
                                if (payload.size > languageCodeLength + 1) {
                                    val tagText = String(
                                        payload,
                                        languageCodeLength + 1,
                                        payload.size - languageCodeLength - 1,
                                        charset(textEncoding)
                                    )

                                    // Display the tag text
                                    lastTagInfo.text = "Last Tag: $tagId\nData: $tagText"
                                } else {
                                    lastTagInfo.text = "Last Tag: $tagId\nError: Payload too small."
                                }
                            } else {
                                lastTagInfo.text = "Last Tag: $tagId\nError: Empty payload."
                            }
                        }
                    }
                } else {
                    lastTagInfo.text = "Last Tag: $tagId\nNo NDEF messages found."
                }
            } catch (e: IOException) {
                Log.e("NfcReadActivity", "Error reading NFC tag", e)
                lastTagInfo.text = "Error reading NFC tag."
            } catch (e: FormatException) {
                Log.e("NfcReadActivity", "NDEF format error", e)
                lastTagInfo.text = "NDEF format error."
            } finally {
                try {
                    ndef.close()
                } catch (e: IOException) {
                    Log.e("NfcReadActivity", "Error closing NFC tag", e)
                }
            }
        } else {
            lastTagInfo.text = "Tag does not support NDEF."
        }
        /*
        lifecycleScope.launch {
            val data = dbService.queryDataByTagId(tagId)
            if (data != null) {
                // Update the lastTagInfo TextView to show data from the last tag read
                lastTagInfo.text = "Last Tag: $tagId\nData: $data"
            } else {
                lastTagInfo.text = "Last Tag: $tagId\nNo data found."
            }
        }
         */
    }
}

