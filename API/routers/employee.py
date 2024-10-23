from typing import List
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from database.session import get_db
from schemas.employee import EmployeeCreate, EmployeeRead
from crud.employee import *
from schemas.item_registration import ItemRegistryResponse

router = APIRouter()

@router.post("/create", response_model=EmployeeRead)
def create_new_employee(employee: EmployeeCreate, db: Session = Depends(get_db)):
    return create_employee_crud(db, employee)

@router.get("/departments/{department_id}/employees", response_model=List[EmployeeRead])
def get_employees_by_department(department_id: int, db: Session = Depends(get_db)):
    # Query employees based on the department ID
    employees = db.query(Employee).filter(Employee.id_department == department_id).all()
    
    if not employees:
        raise HTTPException(status_code=404, detail="No employees found for the given department")
    return employees


@router.get("/{employee_id}", response_model=EmployeeRead)
def read_employee(employee_id: int, db: Session = Depends(get_db)):
    db_employee = get_employee_crud(db, employee_id)
    if not db_employee:
        raise HTTPException(status_code=404, detail="Employee not found")
    return db_employee

@router.get("/items/{token}", response_model=List[ItemRegistryResponse])
def get_items_by_token(token: str, db: Session = Depends(get_db)):
    db_items = get_items_by_email_crud(db, verify_token(token))
    if not db_items:
        raise HTTPException(status_code=404, detail="Items associated to employee not found")
    return [
        ItemRegistryResponse(
            id=registry.item.id,  # Assuming `item` has `id`
            item_name=registry.item.item_name,
            item_id = registry.item.id,
            registry_date=format_datetime(registry.registry_date),
            department_name=registry.room.department.department_name,  # Assuming `item` has a `department` relationship
            department_id = registry.room.department.id,
            room_name=registry.room.room_name,
            room_id=registry.room.id
        )
        for registry in db_items
    ]
def format_datetime(dt: datetime) -> str:
    return dt.isoformat()
@router.delete("/{employee_id}", response_model=EmployeeRead)
def remove_employee(employee_id: int, db: Session = Depends(get_db)):
    db_employee = delete_employee_crud(db, employee_id)
    if not db_employee:
        raise HTTPException(status_code=404, detail="Employee not found")
    return db_employee