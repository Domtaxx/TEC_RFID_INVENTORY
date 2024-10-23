import openpyxl
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session, aliased
from sqlalchemy import func, and_
from fastapi.responses import StreamingResponse
from io import BytesIO
from database.session import get_db
from database.models import Item, Room, Department, ItemRegistry

router = APIRouter()

from sqlalchemy import func, and_
from sqlalchemy.orm import aliased

def get_items_by_department(department_id: int, db: Session):
    # Subquery to get the latest registry date for each item
    latest_registry_subquery = (
        db.query(
            ItemRegistry.id_item,
            func.max(ItemRegistry.registry_date).label("latest_registry_date")
        )
        .join(Room, ItemRegistry.id_room == Room.id)
        .filter(Room.id_department == department_id)  # Filter by department
        .group_by(ItemRegistry.id_item)
        .subquery()
    )

    # Main query to get the latest registry details for each item
    return (
        db.query(Item, ItemRegistry, Room, Department)
        .join(latest_registry_subquery, and_(
            Item.id == latest_registry_subquery.c.id_item
        ))
        .join(ItemRegistry, and_(
            ItemRegistry.id_item == latest_registry_subquery.c.id_item,
            ItemRegistry.registry_date == latest_registry_subquery.c.latest_registry_date
        ))
        .join(Room, ItemRegistry.id_room == Room.id)
        .join(Department, Room.id_department == Department.id)
        .filter(Room.id_department == department_id)  # Filter by department
        .all()
    )


@router.get("/items_by_department/{department_id}")
def generate_department_report(department_id: int, db: Session = Depends(get_db)):
    items = get_items_by_department(department_id, db)
    
    # Create a new workbook and add a worksheet
    wb = openpyxl.Workbook()
    ws = wb.active
    ws.title = "Items Report"

    # Add headers
    ws.append(["ID del Activo","Nombre del Activo", "Numero Serial", "Nombre del departamento", "Ultima ubicacion conocida", "Dia registrado"])

    # Add data rows
    for item, registry, room, department in items:
        ws.append([item.id ,item.item_name, item.serial_number, department.department_name, room.room_name, registry.registry_date])

    # Save the file to a BytesIO buffer
    buffer = BytesIO()
    wb.save(buffer)
    buffer.seek(0)

    # Return the Excel file as a response
    headers = {
        'Content-Disposition': f'attachment; filename=items_by_department_{department_id}.xlsx'
    }
    return StreamingResponse(buffer, media_type='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', headers=headers)