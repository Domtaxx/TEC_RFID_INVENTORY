package com.nfs.tec_rfid.views
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.nfs.tec_rfid.R

class MainMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu_activity)

        val ReportesButton: Button = findViewById(R.id.btn_reportes)
        val UserButton: Button = findViewById(R.id.btn_usuario)
        val AdminButton: Button = findViewById(R.id.btn_admin)
        val ModifyButton: Button = findViewById(R.id.btn_nfc_modify)
        val AgregarActivosButton: Button = findViewById(R.id.btn_nfc_write)

        AdminButton.setOnClickListener {
            // Launch NFC Reading Activity
            //val intent = Intent(this, InventarioActivosActivity::class.java)
            //startActivity(intent)
        }
        ModifyButton.setOnClickListener {
            // Launch NFC Reading Activity
            val intent = Intent(this, ModifyItemActivity::class.java)
            startActivity(intent)
        }
        AgregarActivosButton.setOnClickListener {
            // Launch NFC Reading Activity
            val intent = Intent(this, AgregarActivosActivity::class.java)
            startActivity(intent)
        }
        ReportesButton.setOnClickListener {
            // Launch NFC Reading Activity
            //val intent = Intent(this, SelectDepartmentActivity::class.java)
            //startActivity(intent)
        }
        UserButton.setOnClickListener {
            // Launch NFC Writing Activity
            val intent = Intent(this, ModifyUserActivity::class.java)
            startActivity(intent)
        }
    }
}