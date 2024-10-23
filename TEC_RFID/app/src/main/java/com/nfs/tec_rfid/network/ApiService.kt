package com.nfs.tec_rfid.network
import com.nfs.tec_rfid.models.*
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Call
import retrofit2.Callback
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
    @GET("/items/Item_States/")
    fun getStates(): Call<List<State>>
    @GET("/items/{id}")
    fun getItemById(@Path("id") id: String): Call<ItemResponse>
    @PUT("/items/{id}")
    fun updateItem(@Path("id") id: Int, @Body item: ItemCreate): Call<ItemResponse>
    @POST("/items/register/{id}")
    fun registerItem(@Path("id") id: Int, @Body item: ItemCreate): Call<ItemResponse>
    @PUT("users/update")
    fun updateUser(@Body userUpdate: UserUpdate): Call<Void>
    @GET("users/role/{token}")
    fun getUserRole(@Path("token") token: String): Call<RoleResponse>
    @PUT("users/update_role")
    fun updateUserRole(@Body roleUpdateRequest: RoleUpdateRequest): Call<Unit>
    @PUT("departments/{id}")
    fun updateDepartment(@Path("id") departmentId: Int, @Body departmentUpdate: DepartmentUpdate): Call<Unit>
    @POST("departments/")
    fun addDepartment(@Body departmentCreate: DepartmentCreate): Call<Unit>
    @POST("rooms/create/")
    fun createRoom(@Body roomCreate: RoomCreate): Call<Void>
    @PUT("rooms/update")
    fun updateRoom(@Body roomUpdate: RoomUpdate): Call<RoomResponse>
    @GET("employees/departments/{departmentId}/employees")
    fun getEmployeesByDepartment(@Path("departmentId") departmentId: Int): Call<List<EmployeeResponse>>
    @GET("employees/items/{token}")
    fun getItemsRegisteredByToken(@Path("token") token: String): Call<List<ItemRegistryResponse>>
    @POST("/items/item_registries/update")
    fun updateItemRegistry(@Body registryUpdate: ItemRegistryUpdate): Call<Void>
    @GET("/reports/items_by_department/{department_id}")
    fun getItemsByDepartmentReport(@Path("department_id") departmentId: Int): Call<ResponseBody>
    @GET("/reports/items_by_room/{room_id}")
    fun getRoomReport(@Path("room_id") roomId: Int): Call<ResponseBody>


}