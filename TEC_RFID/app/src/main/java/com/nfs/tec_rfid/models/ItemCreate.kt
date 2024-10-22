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
    val id_department: Int,
    val id_state: Int,
    val id_room: Int,
    val state: Boolean
)
