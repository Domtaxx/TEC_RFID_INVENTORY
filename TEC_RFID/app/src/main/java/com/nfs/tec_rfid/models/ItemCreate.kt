package com.nfs.tec_rfid.models

import java.util.Date

data class ItemCreate(
    val item_name: String,
    val summary: String?,
    val id_department: Int,
    val nfs: String?,
    val room_id: Int,
    val timestamp: String,
    val id_cycle: Int,
    val token: String
)

data class ItemResponse(
    val id: Int
)