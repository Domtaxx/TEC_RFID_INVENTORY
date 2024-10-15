package com.nfs.tec_rfid.models

data class EmployeeRequest(
    val email: String,
    val password: String,
    val ssn: String,
    val first_name: String,
    val surname: String,
    val id_department: Int,
    val id_role: Int = 2 // Assuming a default role, you can change this based on your requirements
)

data class EmployeeResponse(
    val id: Int,
    val email: String,
    val first_name: String,
    val surname: String,
    val id_department: Int,
    val id_role: Int
)