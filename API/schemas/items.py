from pydantic import BaseModel
from datetime import datetime
from typing import Optional


class ItemCreate(BaseModel):
    item_name: str
    summary: Optional[str] = None
    serial_number: Optional[str] = None
    responsible_email: str
    id_department: int
    nfs: Optional[str] = None
    room_id: int
    timestamp: datetime  # Assuming the timestamp is in datetime format on the backend
    id_state: int
    token: str

    class Config:
        arbitrary_types_allowed = True  # Allow arbitrary types like datetime

class ItemRead(BaseModel):
    id: int
    serial_number: Optional[str] = None
    item_name: str
    summary: Optional[str] = None
    id_department: Optional[int] = None
    nfs: Optional[str] = None
    id_state: Optional[int] = None
    responsible_email: Optional[str] = None

    class Config:
        orm_mode = True


class ItemResponse(BaseModel):
    id: int
    state: int