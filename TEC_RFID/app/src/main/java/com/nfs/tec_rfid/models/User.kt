package com.nfs.tec_rfid.models
data class UserUpdate(
    val password: String,
    val id_department: Int,
    val token: String
)

data class RoleResponse(
    val role: String
)
data class RoleUpdateRequest(
    val email: String,
    val role: String
)