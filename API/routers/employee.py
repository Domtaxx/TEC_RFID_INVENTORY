from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from database.session import get_db
from schemas.employee import EmployeeCreate, EmployeeRead
from crud.employee import *

router = APIRouter()

@router.post("/create", response_model=EmployeeRead)
def create_new_employee(employee: EmployeeCreate, db: Session = Depends(get_db)):
    return create_employee_crud(db, employee)

@router.get("/{employee_id}", response_model=EmployeeRead)
def read_employee(employee_id: int, db: Session = Depends(get_db)):
    db_employee = get_employee_crud(db, employee_id)
    if not db_employee:
        raise HTTPException(status_code=404, detail="Employee not found")
    return db_employee

@router.delete("/{employee_id}", response_model=EmployeeRead)
def remove_employee(employee_id: int, db: Session = Depends(get_db)):
    db_employee = delete_employee_crud(db, employee_id)
    if not db_employee:
        raise HTTPException(status_code=404, detail="Employee not found")
    return db_employee