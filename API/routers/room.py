from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from database.session import get_db
from schemas.rooms import *
from crud.room import *
from typing import List

from schemas.rooms import RoomRead
router = APIRouter()


# ---- CRUD for Rooms ----

@router.post("/create", response_model=RoomRead)
def create_room(room: RoomCreate, db: Session = Depends(get_db)):
    return create_room_crud(db, room)

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