import binascii
from fastapi import Depends
from sqlalchemy.orm import Session
from database.models import Employee
from schemas.employee import *
from core.security import *

def authenticate_user(db: Session, email: str, password: str):
    user = db.query(Employee).filter(
        Employee.email == email,
        Employee.user_password == hash_password(password)
    ).first()
    return user
