import datetime
from typing import Optional
from pydantic import BaseModel, SkipValidation
class LoginRequest(BaseModel):
    email: str
    password: str
    token: str

class LoginResponse(BaseModel):
    success: bool
    token: Optional[str] = None
    error: Optional[str] = None