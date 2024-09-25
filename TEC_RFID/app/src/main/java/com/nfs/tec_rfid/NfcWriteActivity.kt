package com.nfs.tec_rfid
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NfcWriteActivity : AppCompatActivity() {

    private lateinit var nfcReader: NFCReader
    private lateinit var writeButton: Button
    private lateinit var inputText: EditText
    private lateinit var returnButton: Button
    private var textToWrite: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_write)

        nfcReader = NFCReader(this)
        writeButton = findViewById(R.id.write_button)
        returnButton = findViewById(R.id.btn_return_main)  // Find the return button
        inputText = findViewById(R.id.input_text)

        writeButton.setOnClickListener {
            if (nfcReader.isNfcEnabled()) {
                val text = inputText.text.toString()
                if (text.isNotEmpty()) {
                    textToWrite = text  // Store the text to be written
                    Toast.makeText(this, "Tap an NFC tag to write", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter some text to write", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "NFC is not enabled", Toast.LENGTH_SHORT).show()
            }
        }
        // Handle returning to MainActivity
        returnButton.setOnClickListener {
            // Create an explicit Intent to launch MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // Optionally finish the current activity if you don't want it in the back stack
        }
    }

    override fun onResume() {
        super.onResume()
        nfcReader.enableReaderMode()  // Enable NFC reader mode for writing
    }

    override fun onPause() {
        super.onPause()
        nfcReader.disableReaderMode()  // Disable NFC reader mode when not needed
    }

    // Method to be called when an NFC tag is detected
    fun onTagDiscovered(tag: android.nfc.Tag) {
        textToWrite?.let {
            nfcReader.writeTag(tag, it)  // Pass the discovered tag and the stored text to write
        }
    }
}
