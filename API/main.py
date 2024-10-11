from fastapi import FastAPI
from database.session import engine, Base
from fastapi.security import OAuth2PasswordBearer


from routers import employee, login, department, room
# Create the database tables
Base.metadata.create_all(bind=engine)
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")
app = FastAPI()

app.include_router(employee.router, prefix="/employees", tags=["employees"])
app.include_router(login.router, prefix="/login", tags=["employees"])
app.include_router(department.router, prefix="/departments", tags=["departments"])
app.include_router(room.router, prefix="/rooms", tags=["rooms"])

