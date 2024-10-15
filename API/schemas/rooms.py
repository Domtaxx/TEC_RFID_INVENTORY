import datetime
from typing import Optional
from pydantic import BaseModel, SkipValidation
class RoomCreate(BaseModel):
    room_name: str
    id_department: int

class RoomRead(BaseModel):
    id: int
    room_name: str
    id_department: int

    class Config:
        from_attributes = True
        arbitrary_types_allowed = True  # Allow arbitrary types like datetime

class RoomUpdate(BaseModel):
    id: int
    room_name: str
    id_department: int

class RoomCreate(BaseModel):
    room_name: str
    id_department: int

class RoomResponse(BaseModel):
    id: int
    room_name: str
    id_department: int
