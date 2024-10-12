package com.nfs.tec_rfid.views
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.nfs.tec_rfid.R

class ActivosMenuActivity : AppCompatActivity() {
    private lateinit var returnButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activos_menu_activity)

        val readNfcButton: Button = findViewById(R.id.btn_count_items)
        val writeNfcButton: Button = findViewById(R.id.btn_register_items)
        val itemFoundButton: Button = findViewById(R.id.btn_items_found)
        val itemMissingButton: Button = findViewById(R.id.btn_missing_items)
        val missplacedButton: Button = findViewById(R.id.btn_misplaced_items)

        returnButton = findViewById(R.id.btn_return_main)

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

        itemFoundButton.setOnClickListener {
        // Launch NFC Reading Activity
            /*
            val intent = Intent(this, NfcReadActivity::class.java)
            startActivity(intent)
            */
        }
        itemMissingButton.setOnClickListener {
        // Launch NFC Reading Activity
            /*
            val intent = Intent(this, NfcReadActivity::class.java)
            startActivity(intent)
            */
        }
        missplacedButton.setOnClickListener {
        // Launch NFC Reading Activity
            /*
            val intent = Intent(this, NfcReadActivity::class.java)
            startActivity(intent)
            */
        }
        // Handle returning to MainActivity
        returnButton.setOnClickListener {
        // Create an explicit Intent to launch MainActivity
            val intent = Intent(this, SelectDepartmentActivity::class.java)
            startActivity(intent)
            finish()  // Optionally finish the current activity if you don't want it in the back stack
        }
    }
}