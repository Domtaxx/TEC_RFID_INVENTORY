
from sqlalchemy.orm import Session
from database.models import Department, Room
from schemas.department import DepartmentCreate

def create_department_crud(department: DepartmentCreate, db: Session):
    db_department = Department(department_name=department.department_name)
    db.add(db_department)
    db.commit()
    db.refresh(db_department)
    return db_department

def get_departments_crud(db: Session):
    # Query all departments
    return db.query(Department).all()

def get_department_crud(db: Session, department_id: int):
    return db.query(Department).filter(Department.id == department_id).first()

def get_rooms_from_department_crud(db: Session, department_id: int):
    return db.query(Room).filter(Room.id_department == department_id).all()

def delete_department_crud(db: Session, department_id: int):
    db_department = get_department_crud(db, department_id)
    if db_department is None:
        return
    db.delete(db_department)
    db.commit()
    return db_department
