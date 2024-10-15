
from sqlalchemy.orm import Session
from database.models import Department, Room
from schemas.rooms import RoomCreate

def get_room_crud(db: Session, room_id: int):
    return db.query(Room).filter(Room.id == room_id).first()

def get_all_room_crud(db: Session):
    return db.query(Room).all()
