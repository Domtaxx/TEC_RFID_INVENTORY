# app/routers/items.py

from typing import List
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from crud.item import create_item_crud, get_item_crud, register_item_crud, update_item_crud
from database.session import get_db
from schemas.cycle import CycleRead
from schemas.items import *
from database.models import Cycle

router = APIRouter()

@router.post("/", response_model=ItemResponse)
def create_item(item: ItemCreate, db: Session = Depends(get_db)):
    item_id, item_state = create_item_crud(item, db)
    if item_id:
        res = ItemResponse(id=item_id, state = item_state)  # Pass the id directly to the constructor
        return res
    else:
        raise HTTPException(status_code=401, detail="Tag already registered")


@router.get("/{item_id}", response_model=ItemRead)
def get_item(item_id: str, db: Session = Depends(get_db)):
    db_item = get_item_crud(item_id, db)
    if db_item is None:
        raise HTTPException(status_code=404, detail="Item not found")
    return db_item

@router.put("/{item_id}", response_model=ItemRead)
def update_item(item_id: int, item: ItemCreate, db: Session = Depends(get_db)):
    db_item = update_item_crud(item_id, item, db)
    if db_item is None:
        raise HTTPException(status_code=404, detail="Item not found")
    return db_item

@router.post("/register/{item_id}", response_model=ItemResponse)
def update_item(item_id: int, item: ItemCreate, db: Session = Depends(get_db)):
    item_id, item_state = register_item_crud(item, db)
    if item_id:
        res = ItemResponse(id=item_id, state = item_state)  # Pass the id directly to the constructor
        return res
    else:
        raise HTTPException(status_code=401, detail="Tag Inactivo")

@router.get("/cycles/", response_model=List[CycleRead])
def get_cycles(db: Session = Depends(get_db)):
    # Fetch all cycles from the database
    cycles = db.query(Cycle).all()
    
    # If no cycles are found, raise an HTTP 404 error
    if not cycles:
        raise HTTPException(status_code=404, detail="No cycles found")
    
    return cycles