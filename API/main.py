from fastapi import FastAPI
from database.session import engine, Base
from fastapi.security import OAuth2PasswordBearer


from routers import employee, login, department, room, item, user
# Create the database tables
Base.metadata.create_all(bind=engine)

app = FastAPI()

app.include_router(employee.router, prefix="/employees", tags=["employees"])
app.include_router(login.router, prefix="/login", tags=["employees"])
app.include_router(department.router, prefix="/departments", tags=["departments"])
app.include_router(room.router, prefix="/rooms", tags=["rooms"])
app.include_router(item.router, prefix="/items", tags=["items"])
app.include_router(user.router, prefix="/users", tags=["items"])

