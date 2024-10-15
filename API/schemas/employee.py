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


class UserUpdate(BaseModel):
    password: str
    id_department: int
    token: str

# Schema for updating an employee
class EmployeeUpdate(BaseModel):
    email: Optional[str] = None
    ssn: Optional[str] = None
    first_name: Optional[str] = None
    surname: Optional[str] = None
    password: Optional[str] = None
    id_department: Optional[int] = None
    id_role: Optional[int] = None


class RoleResponse(BaseModel):
    role: str

class RoleUpdateRequest(BaseModel):
    email: str
    role: str