from sqlalchemy.orm import Session
from core.security import verify_token
from database.models import Item, ItemRegistry
from schemas.items import *

def create_item_crud(item: ItemCreate, db: Session):
# Use current_user (email) as needed, e.g., set as owner of the item
    db_item = Item(
        item_name=item.item_name,
        summary=item.summary,
        id_department=item.id_department,
        nfs=item.nfs
    )
    db.add(db_item)
    db.commit()
    db.refresh(db_item)

    db_item_registry=ItemRegistry(
        id_employee = verify_token(item.token),
        id_item = db_item.id,
        registry_date = item.timestamp,
        id_cycle = item.cycle,
        id_room = item.room_id
    )
    db.add(db_item_registry)
    db.commit()
    db.refresh(db_item)

    return db_item.id