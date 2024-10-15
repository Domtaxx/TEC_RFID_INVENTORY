package com.nfs.tec_rfid.models

data class Department(
    val id: Int,
    val department_name: String
)

data class DepartmentUpdate(
    val department_name: String
)
data class DepartmentCreate(
    val department_name: String
)