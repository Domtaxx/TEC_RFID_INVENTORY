package com.nfs.tec_rfid.views

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nfs.tec_rfid.Adapters.ItemRegistryAdapter
import com.nfs.tec_rfid.R
import com.nfs.tec_rfid.models.ItemRegistryResponse
import com.nfs.tec_rfid.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ItemRegistryListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var registries: List<ItemRegistryResponse> = listOf()
    private var userToken: String? = null
    private lateinit var adapter: ItemRegistryAdapter  // Custom adapter for RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_registry_list)

        recyclerView = findViewById(R.id.item_registry_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch user token from SharedPreferences
        userToken = getUserToken()

        // Initialize the adapter
        adapter = ItemRegistryAdapter(registries) { selectedItem ->
            showModifyDialog(selectedItem)
        }

        recyclerView.adapter = adapter

        // Fetch registries
        fetchUserRegistries()
    }
    private fun getUserToken(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_token", null)
    }
    private fun fetchUserRegistries() {
        userToken?.let { token ->
            val call = ApiClient.instance.getItemsRegisteredByToken(token)
            call.enqueue(object : Callback<List<ItemRegistryResponse>> {
                override fun onResponse(call: Call<List<ItemRegistryResponse>>, response: Response<List<ItemRegistryResponse>>) {
                    if (response.isSuccessful) {
                        registries = response.body() ?: listOf()
                        adapter.updateData(registries)
                    } else {
                        Toast.makeText(this@ItemRegistryListActivity, "Failed to load registries", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<ItemRegistryResponse>>, t: Throwable) {
                    Toast.makeText(this@ItemRegistryListActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } ?: run {
            Toast.makeText(this, "User token not found. Please login again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showModifyDialog(registry: ItemRegistryResponse) {
        // Your logic for showing and modifying item registry
    }
}