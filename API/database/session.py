from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
import urllib

from database import models

# SQL Server connection string (replace with your actual credentials)
#params = urllib.parse.quote_plus( "Driver={ODBC Driver 17 for SQL Server};" "Server=DESKTOP-7UFP9HF;" "Database=RFID_TEC;" "UID=TEC_ADMIN;" "PWD=PROYECTO2024!;" "Trusted_Connection=yes;" ) 
params = urllib.parse.quote_plus( "Driver={ODBC Driver 17 for SQL Server};" "Server=BRIAN\MSSQLSERVER01;" "Database=RFID_TEC;" "UID=TEC_ADMIN;" "PWD=PROYECTO2024!;" "Trusted_Connection=yes;" ) 
conn_str = f"mssql+pyodbc:///?odbc_connect={params}" 
engine = create_engine(conn_str, echo=True)

# Create a session factory
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Base class for the models
Base = models.Base

# Dependency to get the database session
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()