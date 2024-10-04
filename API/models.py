from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, BINARY, DECIMAL
from sqlalchemy.orm import relationship
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class Department(Base):
    __tablename__ = "departments"
    
    id = Column(Integer, primary_key=True, index=True)
    department_name = Column(String, nullable=False)
    rooms = relationship("Room", back_populates="department")
    employees = relationship("Employee", back_populates="department")

class Room(Base):
    __tablename__ = "rooms"
    
    id = Column(Integer, primary_key=True, index=True)
    room_name = Column(String, nullable=False)
    id_department = Column(Integer, ForeignKey("departments.id"))
    latitude = Column(DECIMAL(9, 6))
    longitude = Column(DECIMAL(9, 6))

    department = relationship("Department", back_populates="rooms")

class Employee(Base):
    __tablename__ = "employees"
    
    id = Column(Integer, primary_key=True, index=True)
    user_password = Column(BINARY(60), nullable=False)  # For storing hashed passwords
    email = Column(String(255), nullable=False, unique=True)
    ssn = Column(String(20), nullable=False, unique=True)
    first_name = Column(String(50), nullable=False)
    surname = Column(String(50), nullable=False)
    id_department = Column(Integer, ForeignKey("departments.id"))
    id_role = Column(Integer, ForeignKey("roles.id"))

    department = relationship("Department", back_populates="employees")
    role = relationship("Role", back_populates="employees")

class Role(Base):
    __tablename__ = "roles"
    
    id = Column(Integer, primary_key=True, index=True)
    role_name = Column(String, nullable=False)
    summary = Column(String)

    employees = relationship("Employee", back_populates="role")

class Item(Base):
    __tablename__ = "items"
    
    id = Column(Integer, primary_key=True, index=True)
    item_name = Column(String, nullable=False)
    summary = Column(String(512))
    id_department = Column(Integer, ForeignKey("departments.id"))
    nfs = Column(Integer)
    
    department = relationship("Department")

class Cycle(Base):
    __tablename__ = "cycles"
    
    id = Column(Integer, primary_key=True, index=True)
    cycle_name = Column(String, nullable=False)
    description = Column(String(512))

class ItemRegistry(Base):
    __tablename__ = "item_registry"
    
    id_employee = Column(Integer, ForeignKey("employees.id"), primary_key=True)
    id_item = Column(Integer, ForeignKey("items.id"), primary_key=True)
    registry_date = Column(DateTime, nullable=False)
    id_cycle = Column(Integer, ForeignKey("cycles.id"), nullable=False)
    place = Column(String(255), nullable=False)
    
    employee = relationship("Employee")
    item = relationship("Item")
    cycle = relationship("Cycle")
