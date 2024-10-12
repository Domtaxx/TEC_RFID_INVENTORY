package com.nfs.tec_rfid.models

data class LoginRequest(val email: String, val password: String, val token: String)
data class LoginResponse(val success: Boolean, val token: String?, val error: String?)
