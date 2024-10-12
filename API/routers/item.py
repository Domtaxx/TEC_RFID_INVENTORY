# app/routers/items.py

from typing import List
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from crud.item import create_item_crud
from database.session import get_db
from schemas.cycle import CycleRead
from schemas.items import *
from database.models import Cycle

router = APIRouter()

@router.post("/", response_model=ItemResponse)
def create_item(item: ItemCreate, db: Session = Depends(get_db)):
    item_id = create_item_crud(item, db)
    if item_id:
        res = ItemResponse(id=item_id)  # Pass the id directly to the constructor
        return res
    else:
        raise HTTPException(status_code=401, detail="Tag already registered")

@router.get("/cycles/", response_model=List[CycleRead])
def get_cycles(db: Session = Depends(get_db)):
    # Fetch all cycles from the database
    cycles = db.query(Cycle).all()
    
    # If no cycles are found, raise an HTTP 404 error
    if not cycles:
        raise HTTPException(status_code=404, detail="No cycles found")
    
    return cycles