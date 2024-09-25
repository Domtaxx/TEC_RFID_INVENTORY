package com.nfs.tec_rfid
import okhttp3.*
import java.io.IOException
class HttpClientService {
    private val client = OkHttpClient()

    // Function to make a simple GET request to a URL
    fun makeGetRequest(url: String, callback: (String?) -> Unit) {
        val request = Request.Builder()
            .url(url)
            .build()

        // Make asynchronous request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(null)  // Callback with null on failure
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()  // Get the response body as string
                    callback(responseData)  // Callback with the response data
                } else {
                    callback(null)  // Callback with null on failure
                }
            }
        })
    }
}