package com.nfs.tec_rfid.models

data class Room(
    val id: Int,
    val room_name: String,
    val id_department: Int
)

data class RoomCreate(
    val room_name: String,
    val id_department: Int
)

data class RoomUpdate(
    val id: Int,
    val room_name: String,
    val id_department: Int
)

data class RoomResponse(
    val id: Int,
    val room_name: String,
    val id_department: Int
)