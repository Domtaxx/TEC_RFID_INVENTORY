from fastapi import Depends
from sqlalchemy.orm import Session
from database.models import Employee, ItemRegistry
from schemas.employee import *
from core.security import *

# Create a new employee (User)
#@app.post("/employees/", response_model=EmployeeRead)
def create_employee_crud(db: Session, employee:EmployeeCreate):
    # Hash the password
    hashed_password = hash_password(employee.password)
    
    # Create new Employee instance
    db_employee = Employee(
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

def get_employee_crud(db: Session, employee_id: int):
    return db.query(Employee).filter(Employee.id == employee_id).first()

def get_employee_by_email_crud(db: Session, employee_email: str):
    return db.query(Employee).filter(Employee.email == employee_email).first()

def get_items_by_email_crud(db: Session, employee_email: str):
    db_emp =  db.query(Employee).filter(Employee.email == employee_email).first()
    if not db_emp:
        return None
    db_items = db.query(ItemRegistry).filter(ItemRegistry.id_employee == db_emp.id).all()
    return db_items

def delete_employee_crud(db: Session, employee_id: int):
    db_employee = db.query(Employee).filter(Employee.id == employee_id).first()
    if db_employee:
        db.delete(db_employee)
        db.commit()
    return db_employee