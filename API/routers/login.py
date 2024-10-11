from datetime import timedelta
from fastapi import APIRouter, Depends, HTTPException
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError, jwt
from core.config import *
from core.security import create_access_token
from crud.employee import get_employee_by_email_crud
from crud.login import authenticate_user
from database.models import Employee
from schemas.login import LoginRequest, LoginResponse
from sqlalchemy.orm import Session
from database.session import get_db
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

router = APIRouter()
@router.post("/validate_token", response_model=LoginResponse)
def validate_token(request: LoginRequest,db: Session = Depends(get_db)):
    try:
        # Decode and validate the JWT token
        payload = jwt.decode(request.token, SECRET_KEY, algorithms=[ALGORITHM])
        email = payload.get("sub")
        if not email:
            raise HTTPException(status_code=401, detail="Invalid token")

        # Check if the user exists in the database
        user = get_employee_by_email_crud(db, email)
        if not user:
            raise HTTPException(status_code=401, detail="User not found")
        return LoginResponse(success=True, token=request.token, error=None)
    except JWTError:
        raise HTTPException(status_code=401, detail="Token validation failed")
    

@router.post("/", response_model=LoginResponse)
def login(request: LoginRequest, db: Session = Depends(get_db)):
    employee = authenticate_user(db, request.email, request.password)
    if not employee:
        return LoginResponse(success=False, token=None, error="Invalid credentials")

    # Create a JWT token
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": employee.email}, expires_delta=access_token_expires
    )

    return LoginResponse(success=True, token=access_token, error=None)