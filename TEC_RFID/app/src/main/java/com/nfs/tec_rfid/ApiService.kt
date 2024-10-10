package com.nfs.tec_rfid
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    @POST("validate_token")
    suspend fun validateToken(@Body request: LoginRequest): LoginResponse
}