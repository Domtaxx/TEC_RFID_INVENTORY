import datetime
from typing import Optional
from pydantic import BaseModel, SkipValidation

# Item Schemas
class ItemCreate(BaseModel):
    item_name: str
    summary: Optional[str] = None
    id_department: int
    nfs: Optional[int] = None

class ItemRead(BaseModel):
    id: int
    item_name: str
    summary: Optional[str] = None
    id_department: int
    nfs: Optional[int] = None

    class Config:
        from_attributes = True