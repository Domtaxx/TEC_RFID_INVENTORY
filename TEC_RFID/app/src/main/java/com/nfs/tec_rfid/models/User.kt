package com.nfs.tec_rfid.models
data class UserUpdate(
    val password: String,
    val id_department: Int,
    val token: String
)