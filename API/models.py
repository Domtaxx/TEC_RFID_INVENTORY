from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, BINARY, DECIMAL
from sqlalchemy.orm import relationship
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class Department(Base):
    __tablename__ = "department"
    
    id = Column(Integer, primary_key=True, index=True)
    department_name = Column(String, nullable=False)

    # One-to-many relationship with Room
    rooms = relationship("Room", back_populates="department")
    employees = relationship("Employee", back_populates="department")

class Room(Base):
    __tablename__ = "room"
    
    id = Column(Integer, primary_key=True, index=True)
    room_name = Column(String, nullable=False)
    
    # Foreign key linking to the department table
    id_department = Column(Integer, ForeignKey('department.id'), nullable=False)
    
    # Relationship to Department model
    department = relationship("Department", back_populates="rooms")

class Employee(Base):
    __tablename__ = "employee"
    
    id = Column(Integer, primary_key=True, index=True)
    user_password = Column(BINARY(60), nullable=False)  # For storing hashed passwords
    email = Column(String(255), nullable=False, unique=True)
    ssn = Column(String(20), nullable=False, unique=True)
    first_name = Column(String(50), nullable=False)
    surname = Column(String(50), nullable=False)
    id_department = Column(Integer, ForeignKey("department.id"))
    id_role = Column(Integer, ForeignKey("role.id"))

    department = relationship("Department", back_populates="employees")
    role = relationship("Role", back_populates="employees")

class Role(Base):
    __tablename__ = "role"
    
    id = Column(Integer, primary_key=True, index=True)
    role_name = Column(String, nullable=False)
    summary = Column(String)

    employees = relationship("Employee", back_populates="role")

class Item(Base):
    __tablename__ = "item"
    
    id = Column(Integer, primary_key=True, index=True)
    item_name = Column(String, nullable=False)
    summary = Column(String(512))
    id_department = Column(Integer, ForeignKey("department.id"))
    nfs = Column(Integer)
    
    department = relationship("Department")

class Cycle(Base):
    __tablename__ = "cycle"
    
    id = Column(Integer, primary_key=True, index=True)
    cycle_name = Column(String, nullable=False)
    description = Column(String(512))

class ItemRegistry(Base):
    __tablename__ = "item_registry"
    
    id_employee = Column(Integer, ForeignKey("employee.id"), primary_key=True)
    id_item = Column(Integer, ForeignKey("item.id"), primary_key=True)
    registry_date = Column(DateTime, nullable=False)
    id_cycle = Column(Integer, ForeignKey("cycle.id"), nullable=False)
    place = Column(String(255), nullable=False)
    
    employee = relationship("Employee")
    item = relationship("Item")
    cycle = relationship("Cycle")
