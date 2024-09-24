package com.nfs.tec_rfid
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.util.Log
import java.nio.charset.Charset

class NfcReader(private val callback: NfcReaderCallback) {

    // Interface to notify when NFC data is read
    interface NfcReaderCallback {
        fun onTagRead(content: String)
        fun onTagEmpty()
        fun onError(errorMessage: String)
    }

    // Function to handle reading the contents of an NFC tag
    fun readFromTag(tag: Tag?) {
        try {
            val ndef = Ndef.get(tag) // Get NDEF object from the tag
            if (ndef != null) {
                ndef.connect()
                val ndefMessage: NdefMessage? = ndef.ndefMessage // Get NDEF message from the tag

                if (ndefMessage != null) {
                    // Loop through each NDEF record
                    for (ndefRecord in ndefMessage.records) {
                        // Only process NDEF records of type 'T' (Text)
                        if (ndefRecord.tnf == NdefRecord.TNF_WELL_KNOWN &&
                            ndefRecord.type.contentEquals(NdefRecord.RTD_TEXT)) {
                            val tagContent = readTextFromNdefRecord(ndefRecord)
                            callback.onTagRead(tagContent)
                            return
                        }
                    }
                } else {
                    callback.onTagEmpty()
                }
                ndef.close()
            }
        } catch (e: Exception) {
            Log.e("NFC", "Error reading NFC tag", e)
            callback.onError("Error reading NFC tag: ${e.message}")
        }
    }

    // Helper function to extract text from an NDEF record
    private fun readTextFromNdefRecord(ndefRecord: NdefRecord): String {
        val payload = ndefRecord.payload
        val textEncoding = if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16" // Check the encoding
        val languageCodeLength = payload[0].toInt() and 51 // Get the language code length
        return String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, Charset.forName(textEncoding))
    }
}