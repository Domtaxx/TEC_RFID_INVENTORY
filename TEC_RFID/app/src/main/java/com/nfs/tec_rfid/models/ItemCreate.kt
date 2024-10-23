package com.nfs.tec_rfid.models

import java.util.Date

data class ItemCreate(
    val item_name: String,
    val summary: String?,
    val serial_number: String?,
    val id_employee:Int,
    val id_department: Int,
    val nfs: String?,
    val room_id: Int,
    val timestamp: String,
    val id_state: Int,
    val token: String
)

data class ItemResponse(
    val id: Int,
    val item_name: String,
    val summary: String?,
    val serial_number: String?,
    val id_department: Int,
    val id_state: Int,
    val id_room: Int,
    val id_employee:Int
)
data class ItemRegistryResponse(
    val id: Int,
    val item_name: String,
    val item_id: Int,
    val registry_date: String,
    val department_name: String,
    val department_id: Int,
    val room_name: String,
    val room_id: Int,
    val id_emp: Int
)

data class ItemRegistryUpdate(
    val id_emp: Int,
    val room_id: Int,
    val id_item: Int,
    val registry_date: String
)