import datetime
from typing import Optional
from pydantic import BaseModel, SkipValidation
# Pydantic model for Department (for validation and serialization)
class DepartmentCreate(BaseModel):
    department_name: str

class DepartmentRead(BaseModel):
    id: int
    department_name: str

class DepartmentUpdate(BaseModel):
    department_name: str

    class Config:
        orm_mode = True  # To work seamlessly with SQLAlchemy models