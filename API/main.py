from typing import List
from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel
from database import SessionLocal, engine
import models
import hashlib
from HTTP_Schemas import *
from datetime import datetime, timedelta
from jose import JWTError, jwt
from sqlalchemy.orm import Session
import hashlib
import secrets
from fastapi.security import OAuth2PasswordBearer
from fastapi import status

# Secret key and algorithm for encoding the JWT
SECRET_KEY = "8b2250bb1b29478b8bcf2effd8d08da891d8eb1e787892c743d93778b8bf1a05"  # Replace with a strong secret key
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30  # Token expiration time
# Create the database tables
models.Base.metadata.create_all(bind=engine)
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")
app = FastAPI()

# Dependency to get the database session
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.post("/validate_token", response_model=LoginResponse)
def validate_token(request: LoginRequest,db: Session = Depends(get_db)):
    try:
        # Decode and validate the JWT token
        payload = jwt.decode(request.token, SECRET_KEY, algorithms=[ALGORITHM])
        email = payload.get("sub")
        if not email:
            raise HTTPException(status_code=401, detail="Invalid token")

        # Check if the user exists in the database
        user = db.query(models.Employee).filter(models.Employee.email == email).first()
        if not user:
            raise HTTPException(status_code=401, detail="User not found")

        return LoginResponse(success=True, token=request.token, error=None)
    except JWTError:
        raise HTTPException(status_code=401, detail="Token validation failed")


def hash_password(password: str) -> bytes:
    """Hashes a password using SHA-256."""
    return hashlib.sha256(password.encode()).digest()

def authenticate_user(db: Session, email: str, password: str):
    user = db.query(models.Employee).filter(
        models.Employee.email == email,
        models.Employee.user_password == hash_password(password)
    ).first()
    return user

def create_access_token(data: dict, expires_delta: timedelta = None):
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=15)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

@app.post("/login", response_model=LoginResponse)
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

# CRUD Operations for departments
@app.post("/departments/", response_model=DepartmentRead)
def create_department(department: DepartmentCreate, db: Session = Depends(get_db)):
    db_department = models.Department(department_name=department.department_name)
    db.add(db_department)
    db.commit()
    db.refresh(db_department)
    return db_department


@app.get("/departments/all", response_model=List[DepartmentRead])
def get_departments(db: Session = Depends(get_db)):
    # Query all departments
    db_departments = db.query(models.Department).all()
    
    # If no departments are found, raise an HTTP 404 error
    if not db_departments:
        raise HTTPException(status_code=404, detail="No departments found")
    return db_departments

@app.get("/departments/{department_id}", response_model=DepartmentRead)
def read_department(department_id: int, db: Session = Depends(get_db)):
    db_department = db.query(models.Department).filter(models.Department.id == department_id).first()
    if db_department is None:
        raise HTTPException(status_code=404, detail="Department not found")
    return db_department

@app.get("/departments/{department_id}/rooms", response_model=List[RoomRead])
def get_rooms_by_department(department_id: int, db: Session = Depends(get_db)):
    # Query rooms based on the department ID
    db_rooms = db.query(models.Room).filter(models.Room.id_department == department_id).all()
    
    # If no rooms are found, raise an HTTP 404 error
    if not db_rooms:
        raise HTTPException(status_code=404, detail="No rooms found for this department")
    return db_rooms

@app.delete("/departments/{department_id}", response_model=DepartmentRead)
def delete_department(department_id: int, db: Session = Depends(get_db)):
    db_department = db.query(models.Department).filter(models.Department.id == department_id).first()
    if db_department is None:
        raise HTTPException(status_code=404, detail="Department not found")
    db.delete(db_department)
    db.commit()
    return db_department

# Create a new employee (User)
@app.post("/employees/", response_model=EmployeeRead)
def create_employee(employee:EmployeeCreate, db: Session = Depends(get_db)):
    # Hash the password
    hashed_password = hash_password(employee.password)
    
    # Create new Employee instance
    db_employee = models.Employee(
        user_password=hashed_password,
        email=employee.email,
        ssn=employee.ssn,
        first_name=employee.first_name,
        surname=employee.surname,
        id_department=employee.id_department,
        id_role=employee.id_role
    )
    
    # Add to session and commit
    db.add(db_employee)
    db.commit()
    db.refresh(db_employee)
    
    return db_employee

# Get a specific employee by ID
@app.get("/employees/{employee_id}", response_model=EmployeeRead)
def read_employee(employee_id: int, db: Session = Depends(get_db)):
    db_employee = db.query(models.Employee).filter(models.Employee.id == employee_id).first()
    if db_employee is None:
        raise HTTPException(status_code=404, detail="Employee not found")
    return db_employee

# Update an existing employee by ID
@app.put("/employees/{employee_id}", response_model=EmployeeRead)
def update_employee(employee_id: int, employee_update: EmployeeUpdate, db: Session = Depends(get_db)):
    db_employee = db.query(models.Employee).filter(models.Employee.id == employee_id).first()
    
    if db_employee is None:
        raise HTTPException(status_code=404, detail="Employee not found")
    
    # Update fields if provided
    if employee_update.email:
        db_employee.email = employee_update.email
    if employee_update.ssn:
        db_employee.ssn = employee_update.ssn
    if employee_update.first_name:
        db_employee.first_name = employee_update.first_name
    if employee_update.surname:
        db_employee.surname = employee_update.surname
    if employee_update.password:
        db_employee.user_password = hash_password(employee_update.password)
    if employee_update.id_department is not None:
        db_employee.id_department = employee_update.id_department
    if employee_update.id_role is not None:
        db_employee.id_role = employee_update.id_role

    # Commit changes
    db.commit()
    db.refresh(db_employee)

    return db_employee

# Delete an employee by ID
@app.delete("/employees/{employee_id}", response_model=EmployeeRead)
def delete_employee(employee_id: int, db: Session = Depends(get_db)):
    db_employee = db.query(models.Employee).filter(models.Employee.id == employee_id).first()
    
    if db_employee is None:
        raise HTTPException(status_code=404, detail="Employee not found")
    
    db.delete(db_employee)
    db.commit()

    return db_employee

# ---- CRUD for Rooms ----

@app.post("/rooms/", response_model=RoomRead)
def create_room(room: RoomCreate, db: Session = Depends(get_db)):
    db_room = models.Room(
        room_name=room.room_name,
        id_department=room.id_department,
        latitude=room.latitude,
        longitude=room.longitude
    )
    db.add(db_room)
    db.commit()
    db.refresh(db_room)
    return db_room

@app.get("/rooms/all", response_model=List[RoomRead])
def get_departments(db: Session = Depends(get_db)):
    # Query all rooms
    db_rooms = db.query(models.Room).all()
    
    # If no rooms are found, raise an HTTP 404 error
    if not db_rooms:
        raise HTTPException(status_code=404, detail="No rooms found")
    return db_rooms

@app.get("/rooms/{room_id}", response_model=RoomRead)
def read_room(room_id: int, db: Session = Depends(get_db)):
    db_room = db.query(models.Room).filter(models.Room.id == room_id).first()
    if db_room is None:
        raise HTTPException(status_code=404, detail="Room not found")
    return db_room

@app.put("/rooms/{room_id}", response_model=RoomRead)
def update_room(room_id: int, room: RoomCreate, db: Session = Depends(get_db)):
    db_room = db.query(models.Room).filter(models.Room.id == room_id).first()
    if db_room is None:
        raise HTTPException(status_code=404, detail="Room not found")

    db_room.room_name = room.room_name
    db_room.id_department = room.id_department
    db_room.latitude = room.latitude
    db_room.longitude = room.longitude

    db.commit()
    db.refresh(db_room)
    return db_room

@app.delete("/rooms/{room_id}", response_model=RoomRead)
def delete_room(room_id: int, db: Session = Depends(get_db)):
    db_room = db.query(models.Room).filter(models.Room.id == room_id).first()
    if db_room is None:
        raise HTTPException(status_code=404, detail="Room not found")
    db.delete(db_room)
    db.commit()
    return db_room

# ---- CRUD for Items ----

@app.post("/items/", response_model=ItemRead)
def create_item(item: ItemCreate, db: Session = Depends(get_db)):
    db_item = models.Item(
        item_name=item.item_name,
        summary=item.summary,
        id_department=item.id_department,
        nfs=item.nfs
    )
    db.add(db_item)
    db.commit()
    db.refresh(db_item)
    return db_item

@app.get("/items/{item_id}", response_model=ItemRead)
def read_item(item_id: int, db: Session = Depends(get_db)):
    db_item = db.query(models.Item).filter(models.Item.id == item_id).first()
    if db_item is None:
        raise HTTPException(status_code=404, detail="Item not found")
    return db_item

@app.put("/items/{item_id}", response_model=ItemRead)
def update_item(item_id: int, item: ItemCreate, db: Session = Depends(get_db)):
    db_item = db.query(models.Item).filter(models.Item.id == item_id).first()
    if db_item is None:
        raise HTTPException(status_code=404, detail="Item not found")

    db_item.item_name = item.item_name
    db_item.summary = item.summary
    db_item.id_department = item.id_department
    db_item.nfs = item.nfs

    db.commit()
    db.refresh(db_item)
    return db_item

@app.delete("/items/{item_id}", response_model=ItemRead)
def delete_item(item_id: int, db: Session = Depends(get_db)):
    print(item_id)
    db_item = db.query(models.Item).filter(models.Item.id == item_id).first()
    if db_item is None:
        raise HTTPException(status_code=404, detail="Item not found")
    db.delete(db_item)
    db.commit()
    return db_item

# ---- CRUD for Item Registry ----

@app.post("/item_registry/", response_model=ItemRegistryRead)
def create_item_registry(item_registry: ItemRegistryCreate, db: Session = Depends(get_db)):
    db_item_registry = models.ItemRegistry(
        id_employee=item_registry.id_employee,
        id_item=item_registry.id_item,
        registry_date=item_registry.registry_date,
        id_cycle=item_registry.id_cycle,
        place=item_registry.place
    )
    db.add(db_item_registry)
    db.commit()
    return db_item_registry

@app.get("/item_registry/{id_employee}/{id_item}", response_model=ItemRegistryRead)
def read_item_registry(id_employee: int, id_item: int, db: Session = Depends(get_db)):
    db_item_registry = db.query(models.ItemRegistry).filter(
        models.ItemRegistry.id_employee == id_employee,
        models.ItemRegistry.id_item == id_item
    ).first()
    if db_item_registry is None:
        raise HTTPException(status_code=404, detail="Item registry not found")
    return db_item_registry

@app.put("/item_registry/{id_employee}/{id_item}", response_model=ItemRegistryRead)
def update_item_registry(id_employee: int, id_item: int, item_registry: ItemRegistryCreate, db: Session = Depends(get_db)):
    db_item_registry = db.query(models.ItemRegistry).filter(
        models.ItemRegistry.id_employee == id_employee,
        models.ItemRegistry.id_item == id_item
    ).first()
    if db_item_registry is None:
        raise HTTPException(status_code=404, detail="Item registry not found")

    db_item_registry.registry_date = item_registry.registry_date
    db_item_registry.id_cycle = item_registry.id_cycle
    db_item_registry.place = item_registry.place

    db.commit()
    return db_item_registry

@app.delete("/item_registry/{id_employee}/{id_item}", response_model=ItemRegistryRead)
def delete_item_registry(id_employee: int, id_item: int, db: Session = Depends(get_db)):
    db_item_registry = db.query(models.ItemRegistry).filter(
        models.ItemRegistry.id_employee == id_employee,
        models.ItemRegistry.id_item == id_item
    ).first()
    if db_item_registry is None:
        raise HTTPException(status_code=404, detail="Item registry not found")
    db.delete(db_item_registry)
    db.commit()
    return db_item_registry