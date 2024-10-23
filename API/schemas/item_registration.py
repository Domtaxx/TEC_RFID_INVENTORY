import datetime
from typing import Optional
from pydantic import BaseModel, SkipValidation,ConfigDict

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


class ItemRegistryResponse(BaseModel):
    id: int
    item_name: str
    item_id:int
    registry_date: datetime
    department_name: str
    department_id: int
    room_name: str
    room_id: int

    model_config = ConfigDict(
        from_attributes=True,   # This replaces orm_mode in Pydantic v2
        arbitrary_types_allowed=True  # Allow handling arbitrary types like datetime
    )

class ItemRegistryUpdate(BaseModel):
    id: int
    room_id: int

    model_config = ConfigDict(
        from_attributes=True,   # This replaces orm_mode in Pydantic v2
        arbitrary_types_allowed=True  # Allow handling arbitrary types like datetime
    )


class ItemRegistryRead(BaseModel):
    id_employee: int
    id_item: int
    registry_date: SkipValidation[datetime]
    id_cycle: int
    place: str

    class Config:
        from_attributes = True
        arbitrary_types_allowed = True  # Allow arbitrary types like datetime