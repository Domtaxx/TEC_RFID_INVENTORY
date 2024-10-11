
from sqlalchemy.orm import Session
from database.models import Department, Room
from schemas.rooms import RoomCreate

def create_room_crud(db: Session, room: RoomCreate):
    db_room = Room(
        room_name=room.room_name,
        id_department=room.id_department,
        latitude=room.latitude,
        longitude=room.longitude
    )
    db.add(db_room)
    db.commit()
    db.refresh(db_room)
    return db_room

def get_room_crud(db: Session, room_id: int):
    return db.query(Room).filter(Room.id == room_id).first()

def get_all_room_crud(db: Session):
    return db.query(Room).all()
