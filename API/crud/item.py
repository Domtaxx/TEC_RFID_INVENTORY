from sqlalchemy.orm import Session
from core.security import verify_token
from database.models import Employee, Item, ItemRegistry
from schemas.items import *

def create_item_crud(item: ItemCreate, db: Session):
# Use current_user (email) as needed, e.g., set as owner of the item
    db_temp = db.query(Item).filter(Item.nfs == item.nfs).first()
    if not db_temp:
        db_item = Item(
            item_name=item.item_name,
            summary=item.summary,
            id_department=item.id_department,
            nfs=item.nfs
        )
        db.add(db_item)
        db.commit()
        db.refresh(db_item)
        emp = db.query(Employee).filter(Employee.email == verify_token(item.token)).first()
        db_item = db.query(Item).filter(Item.nfs == item.nfs).first()
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

        return db_item.id
    else:
        return db_temp.id