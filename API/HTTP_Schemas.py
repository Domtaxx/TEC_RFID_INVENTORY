import datetime
from typing import Optional
from pydantic import BaseModel, SkipValidation
# Schema for creating an employee (User)
class EmployeeCreate(BaseModel):
    email: str
    ssn: str
    first_name: str
    surname: str
    password: str
    id_department: Optional[int] = None
    id_role: Optional[int] = None

class LoginRequest(BaseModel):
    email: str
    password: str
    token: str

class LoginResponse(BaseModel):
    success: bool
    token: Optional[str] = None
    error: Optional[str] = None

# Schema for reading an employee
class EmployeeRead(BaseModel):
    id: int
    email: str
    ssn: str
    first_name: str
    surname: str
    id_department: Optional[int] = None
    id_role: Optional[int] = None

    class Config:
        from_attributes  = True  # This allows Pydantic to work with SQLAlchemy objects directly

# Schema for updating an employee
class EmployeeUpdate(BaseModel):
    email: Optional[str] = None
    ssn: Optional[str] = None
    first_name: Optional[str] = None
    surname: Optional[str] = None
    password: Optional[str] = None
    id_department: Optional[int] = None
    id_role: Optional[int] = None

# Pydantic model for Department (for validation and serialization)
class DepartmentCreate(BaseModel):
    department_name: str

class DepartmentRead(BaseModel):
    id: int
    department_name: str
# Room Schemas
class RoomCreate(BaseModel):
    room_name: str
    id_department: int
    latitude: Optional[float] = None
    longitude: Optional[float] = None

class RoomRead(BaseModel):
    id: int
    room_name: str
    id_department: int
    latitude: Optional[float] = None
    longitude: Optional[float] = None

    class Config:
        from_attributes = True
        arbitrary_types_allowed = True  # Allow arbitrary types like datetime

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