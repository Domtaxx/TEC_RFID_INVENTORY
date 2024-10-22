from sqlalchemy.orm import Session
from core.security import verify_token
from database.models import Employee, Item, ItemRegistry
from schemas.items import *
from sqlalchemy.orm import joinedload

def create_item_crud(item: ItemCreate, db: Session):
    # Try to find an existing item by its nfs
    db_temp = db.query(Item).filter(Item.id == int(item.nfs)).first()

    if not db_temp:
        # Create a new Item if none exists
        db_item = Item(
            item_name=item.item_name,
            summary=item.summary,
            id_department=item.id_department,
            item_state = True
        )
        db.add(db_item)
        db.commit()
        
        # Set the nfs to the newly created item ID and update it
        db_item.nfs = str(db_item.id)
        db.commit()  # Commit the update after setting the nfs
        
        db.refresh(db_item)  # Refresh the item to reflect the updated nfs field
        item.nfs = str(db_item.id)
        # Call register_item_crud after creating and updating the item
        return register_item_crud(item, db)
    else:
        # If the item exists, continue to register the item
        return None, None
def update_item_crud(item_id: int, item_update: ItemCreate, db: Session):
    db_item = db.query(Item).filter(Item.id == item_id).first()
    if db_item is None:
        return None
    
    # Update item fields
    db_item.item_name = item_update.item_name or db_item.item_name
    db_item.summary = item_update.summary or db_item.summary
    db_item.id_department = item_update.id_department or db_item.id_department
    db_item.nfs = item_update.nfs or db_item.nfs
    db_item.item_state = item_update.state if item_update.state is not None else db_item.item_state  # Update state

    db.commit()
    db.refresh(db_item)

    register_item_crud(item_update, db)

    return db_item

def get_item_crud(item_id: str, db: Session):
    db_item = db.query(Item).options(joinedload(Item.state)).filter(Item.nfs == item_id).first()
    if db_item is None:
        return None
    return db_item

def get_items_from_employee_crud(email: str, db: Session):
    db_emp = db.query(Employee).filter(Employee.email == email).first()
    if db_emp is None:
        return None
    return db_emp.items

def register_item_crud(item: ItemCreate, db: Session):
    emp = db.query(Employee).filter(Employee.email == verify_token(item.token)).first()
    db_item = db.query(Item).filter((Item.id == int(item.nfs)) and (Item.item_state == True)).first()
    if not db_item or not emp:
        return None
    db_item_registry=ItemRegistry(
        id_employee = emp.id,
        id_item = db_item.id,
        registry_date = item.timestamp,
        id_cycle = item.id_cycle,
        id_room = item.room_id
    )
    db.add(db_item_registry)
    db.commit()
    db.refresh(db_item)
    return db_item.id, db_item.item_state