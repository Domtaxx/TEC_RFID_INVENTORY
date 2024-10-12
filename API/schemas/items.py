import datetime
from typing import Optional
from pydantic import BaseModel, SkipValidation

class ItemCreate(BaseModel):
    item_name: str
    summary: Optional[str] = None
    id_department: int
    nfs: Optional[int] = None
    room_id: int
    timestamp: datetime
    cycle_id: int

    class Config:
        orm_mode = True
        arbitrary_types_allowed = True  # Allow arbitrary types like datetime

class ItemResponse(ItemCreate):
    id: int