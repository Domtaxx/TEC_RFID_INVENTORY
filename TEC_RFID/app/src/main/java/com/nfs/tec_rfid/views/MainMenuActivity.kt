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

        val ActivosButton: Button = findViewById(R.id.btn_activos)
        val UserButton: Button = findViewById(R.id.btn_usuario)

        ActivosButton.setOnClickListener {
            // Launch NFC Reading Activity
            val intent = Intent(this, SelectDepartmentActivity::class.java)
            startActivity(intent)
        }

        UserButton.setOnClickListener {
            // Launch NFC Writing Activity
            val intent = Intent(this, ModifyUserActivity::class.java)
            startActivity(intent)
        }
    }
}