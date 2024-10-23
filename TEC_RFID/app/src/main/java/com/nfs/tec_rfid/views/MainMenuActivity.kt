package com.nfs.tec_rfid.views
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.nfs.tec_rfid.R
import com.nfs.tec_rfid.network.ApiClient
import retrofit2.Callback
import retrofit2.Response
import android.content.Context
import android.widget.Toast
import com.nfs.tec_rfid.models.RoleResponse
import retrofit2.Call

class MainMenuActivity : AppCompatActivity() {
    private lateinit var AdminButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu_activity)
        val ReportesButton: Button = findViewById(R.id.btn_reportes)
        val UserButton: Button = findViewById(R.id.btn_usuario)
        val modifyItemRegistryButton: Button = findViewById(R.id.action_modify_item_registry)
        AdminButton = findViewById(R.id.btn_admin)
        AdminButton.isEnabled = false
        val ModifyButton: Button = findViewById(R.id.btn_nfc_modify)
        val AgregarActivosButton: Button = findViewById(R.id.btn_nfc_write)

        fetchUserRole(getUserToken())

        AdminButton.setOnClickListener {
            // Launch Admin Activity
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }

        modifyItemRegistryButton.setOnClickListener {
            // Launch Admin Activity
            //val intent = Intent(this, ItemRegistryListActivity::class.java)  // Replace with your actual activity name
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
    private fun getUserToken(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_token", null)
    }
    fun fetchUserRole(token: String?) {
        val call = ApiClient.instance.getUserRole(token!!)
        call.enqueue(object : Callback<RoleResponse> {
            override fun onResponse(call: Call<RoleResponse>, response: Response<RoleResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val userRole = response.body()!!.role
                    saveUserRole(userRole)  // Save the role in shared preferences
                    checkUserRole()  // Enable or disable the button based on the role
                } else {
                    Toast.makeText(this@MainMenuActivity, "Failed to fetch user role", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RoleResponse>, t: Throwable) {
                Toast.makeText(this@MainMenuActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    fun saveUserRole(role: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_role", role)
        editor.apply()
    }

    fun checkUserRole() {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userRole = sharedPreferences.getString("user_role", "")
        if (userRole == "ADMIN") {
            AdminButton.isEnabled = true
        } else {
            AdminButton.isEnabled = false
        }
    }

}