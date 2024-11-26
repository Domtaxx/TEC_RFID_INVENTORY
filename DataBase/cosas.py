import pandas as pd

# Load the Excel file
file_path = "InventarioMayo2024 bienesIngenieríaEnComputadores (1).xlsx"  # Update with your file path
sheet_name = 0  # Update if the data is in a specific sheet

# Read the Excel file into a DataFrame
data = pd.read_excel(file_path, sheet_name=sheet_name)

# Generate SQL Insert Statements
table_name = "assets"  # Update with your database table name
sql_statements = []

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
    marca = marca.replace("'", "''") if isinstance(marca, str) else marca
    estado = estado.replace("'", "''") if isinstance(estado, str) else estado
    
    # Create SQL INSERT statement
    sql = f"INSERT INTO {table_name} (placa, descripcion, responsable, ubicacion, marca, serie, estado) VALUES ('{placa}', '{descripcion}', '{responsable}', '{ubicacion}', '{marca}', '{serie}', '{estado}');"
    sql_statements.append(sql)

# Write the SQL statements to a file
with open("populate_assets.sql", "w", encoding="utf-8") as f:
    for statement in sql_statements:
        f.write(statement + "\n")

print(f"SQL script generated: populate_assets.sql")