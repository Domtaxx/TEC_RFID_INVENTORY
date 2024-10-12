from pydantic import BaseModel
from datetime import datetime
from typing import Optional

class ItemCreate(BaseModel):
    item_name: str
    summary: Optional[str] = None
    id_department: int
    nfs: Optional[str] = None
    room_id: int
    timestamp: datetime
    token: str
    id_cycle: int

    class Config:
        arbitrary_types_allowed = True  # Allow arbitrary types like datetime


class ItemResponse(BaseModel):
    id: int