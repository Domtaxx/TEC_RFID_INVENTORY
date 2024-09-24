package com.nfs.tec_rfid
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), NfcReader.NfcReaderCallback {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcTextView: TextView
    private lateinit var nfcReader: NfcReader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nfcTextView = findViewById(R.id.nfcTextView)

        // Initialize NFC reader
        nfcReader = NfcReader(this)

        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            nfcTextView.text = "NFC is not available on this device."
            return
        }
    }

    // Enable NFC Reader Mode in onResume()
    override fun onResume() {
        super.onResume()

        val nfcReaderCallback = NfcAdapter.ReaderCallback { tag ->
            nfcReader.readFromTag(tag)
        }

        val readerModeFlags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

        // Enable reader mode
        nfcAdapter?.enableReaderMode(this, nfcReaderCallback, readerModeFlags, null)
    }

    // Disable NFC Reader Mode in onPause()
    override fun onPause() {
        super.onPause()
        // Disable reader mode
        nfcAdapter?.disableReaderMode(this)
    }

    // Implement the callback functions to handle NFC tag events
    override fun onTagRead(content: String) {
        runOnUiThread {
            nfcTextView.text = "NFC Tag Content: $content"
        }
    }

    override fun onTagEmpty() {
        runOnUiThread {
            nfcTextView.text = "Empty NFC Tag"
        }
    }

    override fun onError(errorMessage: String) {
        runOnUiThread {
            nfcTextView.text = "Error: $errorMessage"
        }
    }
}