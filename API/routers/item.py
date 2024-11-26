# app/routers/items.py

from typing import List
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from crud.item import create_item_crud, get_item_crud, register_item_crud, update_item_crud, update_register_item_crud
from database.session import get_db
from database.models import ItemState
from schemas.State import StateRead
from schemas.item_registration import ItemRegistryUpdate
from schemas.items import *
from core.security import verify_token

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
def get_item(item_id: int, db: Session = Depends(get_db)):
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

@router.get("/Item_States/", response_model=List[StateRead])
def get_cycles(db: Session = Depends(get_db)):
    # Fetch all States from the database
    cycles = db.query(ItemState).all()
    
    # If no States are found, raise an HTTP 404 error
    if not cycles:
        raise HTTPException(status_code=404, detail="No States found")
    
    return cycles


@router.get("/Item_States/", response_model=List[StateRead])
def get_cycles(db: Session = Depends(get_db)):
    # Fetch all States from the database
    cycles = db.query(ItemState).all()
    
    # If no States are found, raise an HTTP 404 error
    if not cycles:
        raise HTTPException(status_code=404, detail="No States found")
    
    return cycles

@router.post("/item_registries/update", response_model=None)
def update_item(registry: ItemRegistryUpdate, db: Session = Depends(get_db)):
    registry = update_register_item_crud(registry, db)
    if registry:
        return 
    else:
        raise HTTPException(status_code=401, detail="Tag Inactivo")