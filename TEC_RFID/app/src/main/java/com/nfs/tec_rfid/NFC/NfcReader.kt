package com.nfs.tec_rfid.NFC
import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.tech.Ndef
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.nfs.tec_rfid.views.NfcReadActivity
import com.nfs.tec_rfid.views.NfcWriteActivity
import java.nio.charset.Charset

class NFCReader(private val activity: Activity) {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    private val TAG = "TEC_RFID_NFC_HANDLER"

    // Callback for NFC tag discovery
    private val nfcReaderCallback = object : NfcAdapter.ReaderCallback {
        override fun onTagDiscovered(tag: Tag?) {
            tag?.let {
                Log.d(TAG, "NFC tag discovered: $tag")
                Handler(Looper.getMainLooper()).post {
                    if (activity is NfcWriteActivity) {
                        Log.d(TAG, "Calling onTagDiscovered in NfcWriteActivity")
                        (activity as NfcWriteActivity).onTagDiscovered(tag)
                    } else if (activity is NfcReadActivity) {
                        Log.d(TAG, "Calling onTagDiscovered in NfcReadActivity")
                        (activity as NfcReadActivity).onTagDiscovered(tag)  // Change this to directly call onTagDiscovered
                    }
                }
            }
        }
    }

    // Enable Reader Mode for NFC tag detection
    fun enableReaderMode() {
        if (nfcAdapter == null) {
            Log.e(TAG, "NFC is not supported on this device.")
            Toast.makeText(activity, "NFC is not supported on this device.", Toast.LENGTH_SHORT).show()
            return
        }

        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V

        Log.d(TAG, "Enabling reader mode")
        nfcAdapter.enableReaderMode(activity, nfcReaderCallback, flags, null)
    }

    // Disable Reader Mode
    fun disableReaderMode() {
        Log.d(TAG, "Disabling reader mode")
        nfcAdapter?.disableReaderMode(activity)
    }

    // Check if NFC is enabled
    fun isNfcEnabled(): Boolean {
        return nfcAdapter != null && nfcAdapter.isEnabled
    }

    // Handle the NFC tag using NDEF for reading
    private fun handleTag(tag: Tag) {
        try {
            val ndef = Ndef.get(tag)
            ndef?.let {
                Log.d(TAG, "Attempting to connect to NFC tag for reading")
                it.connect()
                if (it.isConnected) {
                    Log.d(TAG, "NFC tag connected successfully")
                    val message = it.ndefMessage
                    message?.let {
                        val records = message.records
                        val payload = String(records[0].payload)
                        Log.d(TAG, "Read payload from tag: $payload")
                        Toast.makeText(activity, "Read from tag: $payload", Toast.LENGTH_SHORT).show()
                    }
                    it.close()
                    Log.d(TAG, "NFC tag connection closed after reading")
                } else {
                    Log.e(TAG, "Failed to connect to NFC tag")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading NFC tag", e)
            Toast.makeText(activity, "Failed to read NFC tag", Toast.LENGTH_SHORT).show()
        }
    }

    fun writeTag(tag: Tag, data: String) {
        try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                Log.d(TAG, "Attempting to connect to NFC tag for writing")
                ndef.connect()  // Connect to the tag

                if (ndef.isWritable) {
                    Log.d(TAG, "NFC tag is writable")

                    // Create the NDEF message with the provided data
                    val message = createNdefMessage(data)

                    // Write the NDEF message to the tag
                    ndef.writeNdefMessage(message)
                    ndef.close()  // Close the connection to the tag

                    Log.d(TAG, "NFC tag connection closed after writing")
                    Toast.makeText(activity, "Successfully wrote to NFC tag", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "NFC tag is not writable")
                    Toast.makeText(activity, "NFC tag is not writable", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e(TAG, "NDEF is not supported by this NFC tag")
                Toast.makeText(activity, "NDEF is not supported by this NFC tag", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing NFC tag", e)
            Toast.makeText(activity, "Failed to write NFC tag", Toast.LENGTH_SHORT).show()
        }
    }

    fun createNdefMessage(data: String): NdefMessage {
        // Create a simple NDEF record containing text (RTD_TEXT format)
        val language = "en"  // Language code
        val textBytes = data.toByteArray(Charset.forName("UTF-8"))
        val languageBytes = language.toByteArray(Charset.forName("UTF-8"))
        val payload = ByteArray(1 + languageBytes.size + textBytes.size)  // Create the payload

        payload[0] = languageBytes.size.toByte()  // Set the language length byte
        System.arraycopy(languageBytes, 0, payload, 1, languageBytes.size)  // Copy language code
        System.arraycopy(textBytes, 0, payload, 1 + languageBytes.size, textBytes.size)  // Copy the actual text

        // Create an NDEF record
        val ndefRecord = NdefRecord(
            NdefRecord.TNF_WELL_KNOWN,  // Record Type: Well-known record
            NdefRecord.RTD_TEXT,        // Record Type Name: Text
            ByteArray(0),               // ID: No ID
            payload                     // Payload: The text data
        )

        // Return an NDEF message that wraps the NDEF record
        return NdefMessage(arrayOf(ndefRecord))
    }
}


