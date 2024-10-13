package com.nfs.tec_rfid.network
import com.nfs.tec_rfid.models.*
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.PUT

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    @POST("login/validate_token")
    suspend fun validateToken(@Body request: LoginRequest): LoginResponse
    @GET("departments/all") // Replace with your actual endpoint
    fun getDepartments(): Call<List<Department>>
    @GET("departments/{department_id}/rooms")
    fun getRoomsByDepartment(@Path("department_id") departmentId: Int): Call<List<Room>>
    @POST("/employees/create")
    fun registerEmployee(@Body employee: EmployeeRequest): Call<EmployeeResponse>
    @POST("/items/")
    fun addItem(@Body itemCreate: ItemCreate): Call<ItemResponse> // Assuming the backend returns no response body
    @GET("/items/cycles/")
    fun getCycles(): Call<List<Cycle>>
    @GET("/items/{id}")
    fun getItemById(@Path("id") id: String): Call<ItemResponse>
    @PUT("/items/{id}")
    fun updateItem(@Path("id") id: Int, @Body item: ItemCreate): Call<ItemResponse>
    @POST("/items/register/{id}")
    fun registerItem(@Path("id") id: Int, @Body item: ItemCreate): Call<ItemResponse>
    @PUT("users/update")
    fun updateUser(@Body userUpdate: UserUpdate): Call<Void>
}