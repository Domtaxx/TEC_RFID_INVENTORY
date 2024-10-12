from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, BINARY, DECIMAL, Boolean
from sqlalchemy.orm import relationship
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class Department(Base):
    __tablename__ = "department"
    
    id = Column(Integer, primary_key=True, index=True)
    department_name = Column(String, nullable=False)

    # One-to-many relationship with Room
    rooms = relationship("Room", back_populates="department")
    items = relationship("Item", back_populates="department")
    employees = relationship("Employee", back_populates="department")

class Room(Base):
    __tablename__ = "room"
    
    id = Column(Integer, primary_key=True, index=True)
    room_name = Column(String, nullable=False)
    
    # Foreign key linking to the department table
    id_department = Column(Integer, ForeignKey('department.id'), nullable=False)
    
    # Relationship to Department model
    department = relationship("Department", back_populates="rooms")
    item_registries = relationship("ItemRegistry", back_populates="room")

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
    item_registries = relationship("ItemRegistry", back_populates="employee")

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
    nfs = Column(String(128), nullable=True)
    item_state = Column(Boolean, nullable=True)
    
    department = relationship("Department", back_populates="items")
    item_registries = relationship("ItemRegistry", back_populates="item")

class Cycle(Base):
    __tablename__ = "cycle"
    
    id = Column(Integer, primary_key=True, index=True)
    cycle_name = Column(String, nullable=False)

    item_registries = relationship("ItemRegistry", back_populates="cycle")

class ItemRegistry(Base):
    __tablename__ = "item_registry"
    
    id_employee = Column(Integer, ForeignKey("employee.id"), primary_key=True)
    id_item = Column(Integer, ForeignKey("item.id"), primary_key=True)
    registry_date = Column(DateTime, nullable=False)
    id_cycle = Column(Integer, ForeignKey("cycle.id"), nullable=False)
    id_room = Column(Integer, ForeignKey("room.id"), nullable=False)
    
    employee = relationship("Employee", back_populates="item_registries")
    item = relationship("Item", back_populates="item_registries")
    room = relationship("Room", back_populates="item_registries")
    cycle = relationship("Cycle", back_populates="item_registries")
