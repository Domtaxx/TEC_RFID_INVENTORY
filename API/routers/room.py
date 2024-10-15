from fastapi import APIRouter, Depends, HTTPException, logger
from sqlalchemy.orm import Session
from database.session import get_db
from schemas.rooms import *
from crud.room import *
from typing import List

from schemas.rooms import RoomRead
router = APIRouter()


# ---- CRUD for Rooms ----

@router.post("/create", response_model=None)
def create_room(room_create: RoomCreate, db: Session = Depends(get_db)):
    try:
        department = db.query(Department).filter(Department.id == room_create.id_department).first()
        if not department:
            raise HTTPException(status_code=404, detail="Department not found")

        new_room = Room(room_name=room_create.room_name, id_department=room_create.id_department)
        db.add(new_room)
        db.commit()
        db.refresh(new_room)
        return {"msg": "Room created successfully"}
    except Exception as e:
        db.rollback()
        logger.error(f"Error creating room: {e}")
        raise HTTPException(status_code=500, detail="An error occurred while creating the room")


@router.get("/all", response_model=List[RoomRead])
def get_departments(db: Session = Depends(get_db)):
    # Query all rooms
    db_rooms = get_all_room_crud(db)
    
    # If no rooms are found, raise an HTTP 404 error
    if not db_rooms:
        raise HTTPException(status_code=404, detail="No rooms found")
    return db_rooms

@router.get("/{room_id}", response_model=RoomRead)
def read_room(room_id: int, db: Session = Depends(get_db)):
    db_room = get_room_crud(db, room_id)
    if db_room is None:
        raise HTTPException(status_code=404, detail="Room not found")
    return db_room

@router.put("/update", response_model=RoomResponse)
def update_room(room_update: RoomUpdate, db: Session = Depends(get_db)):
    room = db.query(Room).filter(Room.id == room_update.id).first()
    
    if not room:
        raise HTTPException(status_code=404, detail="Room not found")

    room.room_name = room_update.room_name

    db.commit()
    db.refresh(room)

    return room
