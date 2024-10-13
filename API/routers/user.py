import hashlib
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from database.models import Employee, Department
from core.security import hash_password, verify_token
from database.session import get_db
from schemas.employee import UserUpdate
import logging
import binascii
logger = logging.getLogger("uvicorn.error")

router = APIRouter()

def hash_password_to_store(password: str) -> str:
    # Hash the password using SHA-256
    hashed_password = hashlib.sha256(password.encode()).hexdigest()
    # Convert to hex format with '0x' prefix
    return f"{hashed_password}"

@router.put("/update", response_model=None)
def update_user(user_update: UserUpdate, db: Session = Depends(get_db)):
    try:
        # Step 1: Verify token and get email
        email = verify_token(user_update.token)
        if not email:
            raise HTTPException(status_code=401, detail="Invalid token")

        # Step 2: Begin transaction to update user
        with db.begin():  # Explicit transaction management
            user = db.query(Employee).filter(Employee.email == email).first()
            
            if user is None:
                raise HTTPException(status_code=404, detail="User not found")

            # Step 3: Update password if provided
            if user_update.password:
                print(f"NEW_PASS:{binascii.hexlify(hash_password(user_update.password)).decode('utf-8')}")
                print(f"BEFORE:{binascii.hexlify(user.user_password).decode('utf-8')}")
                user.user_password = hash_password(user_update.password)
                print(f"AFTER:{binascii.hexlify(user.user_password).decode('utf-8')}")

            # Step 4: Update department if provided
            if user_update.id_department:
                department = db.query(Department).filter(Department.id == user_update.id_department).first()
                if department is None:
                    raise HTTPException(status_code=400, detail="Invalid department ID")
                user.id_department = user_update.id_department

            # Step 5: Flush and commit transaction
            db.flush()  # Ensure the changes are sent to the DB

        # Step 6: After transaction block, the commit will be handled automatically
        return {"msg": "User updated successfully"}

    except HTTPException as he:
        raise he
    except Exception as e:
        # Rollback is automatic if we encounter an error
        logger.error(f"Error updating user: {e}")
        raise HTTPException(status_code=500, detail="An unexpected error occurred")