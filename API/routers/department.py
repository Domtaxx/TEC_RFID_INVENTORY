from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from database.session import get_db
from schemas.department import *
from crud.department import *
from typing import List

from schemas.rooms import RoomRead
router = APIRouter()

@router.post("/create", response_model=DepartmentRead)
def create_department(department: DepartmentCreate, db: Session = Depends(get_db)):
    return create_department_crud(department, db)

@router.get("/all", response_model=List[DepartmentRead])
def get_departments(db: Session = Depends(get_db)):
    # Query all departments
    db_departments = get_departments_crud(db)
    
    # If no departments are found, raise an HTTP 404 error
    if not db_departments:
        raise HTTPException(status_code=404, detail="No departments found")
    return db_departments

@router.get("/{department_id}", response_model=DepartmentRead)
def read_department(department_id: int, db: Session = Depends(get_db)):
    db_department = get_department_crud(db, department_id)
    if db_department is None:
        raise HTTPException(status_code=404, detail="Department not found")
    return db_department


@router.get("/{department_id}/rooms", response_model=List[RoomRead])
def get_rooms_by_department(department_id: int, db: Session = Depends(get_db)):
    # Query rooms based on the department ID
    db_rooms = get_rooms_from_department_crud(db, department_id)
    
    # If no rooms are found, raise an HTTP 404 error
    if not db_rooms:
        raise HTTPException(status_code=404, detail="No rooms found for this department")
    return db_rooms

@router.delete("/{department_id}", response_model=DepartmentRead)
def delete_department(department_id: int, db: Session = Depends(get_db)):
    db_department = delete_department_crud(db, department_id)
    if db_department is None:
        raise HTTPException(status_code=404, detail="Department not found")
    return db_department