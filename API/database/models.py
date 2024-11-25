from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, BINARY, DECIMAL, Boolean
from sqlalchemy.orm import relationship
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()


class EmployeeRole(Base):
    __tablename__ = 'EMPLOYEE_ROLE'
    id = Column('ID', Integer, primary_key=True, autoincrement=True)
    role_name = Column('ROLE_NAME', String(64), nullable=False)
    summary = Column('SUMMARY', String(256))

    # Relationship
    employees = relationship('Employee', back_populates='role')

class Department(Base):
    __tablename__ = 'DEPARTMENT'
    id = Column('ID', Integer, primary_key=True, autoincrement=True)
    department_name = Column('DEPARTMENT_NAME', String(256), nullable=False)

    # Relationships
    rooms = relationship('Room', back_populates='department')
    employees = relationship('Employee', back_populates='department')
    items = relationship('Item', back_populates='department')

class Room(Base):
    __tablename__ = 'ROOM'
    id = Column('ID', Integer, primary_key=True, autoincrement=True)
    room_name = Column('ROOM_NAME', String(64), nullable=False)
    id_department = Column('ID_DEPARTMENT', Integer, ForeignKey('DEPARTMENT.ID'), nullable=False)

    # Relationship
    department = relationship('Department', back_populates='rooms')
    item_registries = relationship('ItemRegistry', back_populates='room')

class Employee(Base):
    __tablename__ = 'EMPLOYEE'
    id = Column('ID', Integer, primary_key=True, autoincrement=True)
    user_password = Column('USER_PASSWORD', BINARY(256), nullable=False)
    email = Column('EMAIL', String(255), nullable=False, unique=True)
    ssn = Column('SSN', String(20), nullable=False, unique=True)
    first_name = Column('FIRST_NAME', String(50), nullable=False)
    surname = Column('SURNAME', String(50), nullable=False)
    id_department = Column('ID_DEPARTMENT', Integer, ForeignKey('DEPARTMENT.ID'))
    id_role = Column('ID_ROLE', Integer, ForeignKey('EMPLOYEE_ROLE.ID'))

    # Relationships
    department = relationship('Department', back_populates='employees')
    role = relationship('EmployeeRole', back_populates='employees')
    item_registries = relationship('ItemRegistry', back_populates='employee')

class ItemState(Base):
    __tablename__ = 'ITEM_STATES'
    id = Column('ID', Integer, primary_key=True, autoincrement=True)
    state_name = Column('STATE_NAME', String(32))

    # Relationship
    items = relationship('Item', back_populates='state')

class Item(Base):
    __tablename__ = 'ITEM'
    id = Column('ID', Integer, primary_key=True, autoincrement=True)
    serial_number = Column('SERIAL_NUMBER', String(32))
    item_name = Column('ITEM_NAME', String(64), nullable=False)
    summary = Column('SUMMARY', String(512))
    id_department = Column('ID_DEPARTMENT', Integer, ForeignKey('DEPARTMENT.ID'))
    nfs = Column('NFS', String(128))
    id_state = Column('ID_STATE', Integer, ForeignKey('ITEM_STATES.ID'))
    responsible_email = Column('RESPONSIBLE_EMAIL', String(256))

    # Relationships
    department = relationship('Department', back_populates='items')
    state = relationship('ItemState', back_populates='items')
    item_registries = relationship('ItemRegistry', back_populates='item')

class ItemRegistry(Base):
    __tablename__ = 'ITEM_REGISTRY'
    id_employee = Column('ID_EMPLOYEE', Integer, ForeignKey('EMPLOYEE.ID'), primary_key=True)
    id_item = Column('ID_ITEM', Integer, ForeignKey('ITEM.ID'), primary_key=True)
    registry_date = Column('REGISTRY_DATE', DateTime, primary_key=True)
    id_room = Column('ID_ROOM', Integer, ForeignKey('ROOM.ID'))

    # Relationships
    employee = relationship('Employee', back_populates='item_registries')
    item = relationship('Item', back_populates='item_registries')
    room = relationship('Room', back_populates='item_registries')

