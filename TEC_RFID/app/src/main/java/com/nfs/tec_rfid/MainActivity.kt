package com.nfs.tec_rfid

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nfs.tec_rfid.ui.theme.TEC_RFIDTheme

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nfcTextView = findViewById(R.id.nfcTextView)

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
            // Handle the NFC tag here
            val tagId = tag.id.joinToString("") { byte -> "%02X".format(byte) }
            runOnUiThread {
                nfcTextView.text = "NFC Tag Detected: $tagId"
            }
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
}