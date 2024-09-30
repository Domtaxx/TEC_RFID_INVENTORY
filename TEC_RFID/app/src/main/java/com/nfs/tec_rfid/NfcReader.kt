package com.nfs.tec_rfid
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

    // Write an NDEF message to the NFC tag
    fun writeTag(tag: Tag, data: String) {
        try {
            val ndef = Ndef.get(tag)
            ndef?.let {
                Log.d(TAG, "Attempting to connect to NFC tag for writing")
                it.connect()
                if (it.isWritable) {
                    Log.d(TAG, "NFC tag is writable")
                    val message = createNdefMessage(data)
                    it.writeNdefMessage(message)
                    it.close()
                    Log.d(TAG, "NFC tag connection closed after writing")
                    Toast.makeText(activity, "Successfully wrote to NFC tag", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "NFC tag is not writable")
                    Toast.makeText(activity, "NFC tag is not writable", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing NFC tag", e)
            Toast.makeText(activity, "Failed to write NFC tag", Toast.LENGTH_SHORT).show()
        }
    }

    // Create an NDEF text record
    private fun createNdefMessage(text: String): NdefMessage {
        val textBytes = text.toByteArray(Charsets.UTF_8)  // Encode the text in UTF-8
        val payload = ByteArray(textBytes.size)  // Create a payload without the language code

        // Directly copy the text bytes into the payload
        System.arraycopy(textBytes, 0, payload, 0, textBytes.size)

        Log.d(TAG, "Created NDEF message with text (no language code): $text")

        // Create the NDEF record with TNF_WELL_KNOWN and RTD_TEXT, but without the language code
        return NdefMessage(arrayOf(NdefRecord(
            NdefRecord.TNF_WELL_KNOWN,
            NdefRecord.RTD_TEXT,
            ByteArray(0),  // No type identifier
            payload  // Use the text payload directly
        )))
    }
}


