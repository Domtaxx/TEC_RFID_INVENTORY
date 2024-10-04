from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.ext.declarative import declarative_base
import urllib

# SQL Server connection string (replace with your actual credentials)
SQLALCHEMY_DATABASE_URL = "Driver={ODBC Driver 17 for SQL Server};"
params = urllib.parse.quote_plus(r'Driver={ODBC Driver 17 for SQL Server};Server=tcp:tecrfidserver.database.windows.net,1433;Database=RFID_TEC;Uid=TEC_ADMIN;Pwd=PROYECTO2024!;Encrypt=yes;TrustServerCertificate=no;Connection Timeout=30;') # urllib.parse.quote_plus for python 3

conn_str = 'mssql+pyodbc:///?odbc_connect={}'.format(params)
engine = create_engine(conn_str,echo=True)

# Create a session factory
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Base class for the models
Base = declarative_base()
