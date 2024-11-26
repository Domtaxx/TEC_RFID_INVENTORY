import pandas as pd

# Load the Excel file
file_path = "InventarioMayo2024 bienesIngenieríaEnComputadores (1).xlsx"  # Update with your file path
sheet_name = 0  # Update if the data is in a specific sheet

# Read the Excel file into a DataFrame
data = pd.read_excel(file_path, sheet_name=sheet_name)

# Generate SQL Insert Statements
table_name = "assets"  # Update with your database table name
sql_statements = []
habitaciones = ['F2-07','F5-01', 'F2-02']
for index, row in data.iterrows():
    placa = row['Placa']
    descripcion = row['Descripción del bien']
    responsable = row['Responsable del bien']
    ubicacion = row['Ubicación Fisica']
    marca = row['Marca']
    serie = row['Serie']
    estado = row['Estado del bien']
    
    # Escaping single quotes in strings
    descripcion = descripcion.replace("'", "''") if isinstance(descripcion, str) else descripcion
    responsable = responsable.replace("'", "''") if isinstance(responsable, str) else responsable
    ubicacion = ubicacion.replace("'", "''") if isinstance(ubicacion, str) else ubicacion
    estado = estado.replace("'", "''") if isinstance(estado, str) else estado
    if ubicacion not in habitaciones:
        sql = f"INSERT INTO ROOM (ROOM_NAME, ID_DEPARTMENT) VALUES ('{ubicacion}', 1);"
        sql_statements.append(sql)
        habitaciones.append(ubicacion)
    # Create SQL INSERT statement
    sql = f"INSERT INTO ITEM (ID, ITEM_NAME, SUMMARY, ID_DEPARTMENT, NFS, ID_STATE, RESPONSIBLE_EMAIL) VALUES ({str(placa)[0:5]}, '{descripcion[:64]}','{descripcion}',1,'{str(placa)[0:5]}', 1, '{responsable}');"
    sql_statements.append(sql)

# Write the SQL statements to a file
with open("populate_assets.sql", "w", encoding="utf-8") as f:
    for statement in sql_statements:
        f.write(statement + "\n")

print(f"SQL script generated: populate_assets.sql")