package com.nfs.tec_rfid
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val readNfcButton: Button = findViewById(R.id.btn_read_nfc)
        val writeNfcButton: Button = findViewById(R.id.btn_write_nfc)

        readNfcButton.setOnClickListener {
            // Launch NFC Reading Activity
            val intent = Intent(this, NfcReadActivity::class.java)
            startActivity(intent)
        }

        writeNfcButton.setOnClickListener {
            // Launch NFC Writing Activity
            val intent = Intent(this, NfcWriteActivity::class.java)
            startActivity(intent)
        }
    }
}