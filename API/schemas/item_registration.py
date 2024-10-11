import datetime
from typing import Optional
from pydantic import BaseModel, SkipValidation

# Item Registry Schemas
class ItemRegistryCreate(BaseModel):
    id_employee: int
    id_item: int
    registry_date: SkipValidation[datetime]
    id_cycle: int
    place: str

    class Config:
        from_attributes = True
        arbitrary_types_allowed = True

class ItemRegistryRead(BaseModel):
    id_employee: int
    id_item: int
    registry_date: SkipValidation[datetime]
    id_cycle: int
    place: str

    class Config:
        from_attributes = True
        arbitrary_types_allowed = True  # Allow arbitrary types like datetime