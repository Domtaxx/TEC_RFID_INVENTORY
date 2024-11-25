Para ejecutar la app, se debe ejecutar desde la maquina que fue asignada.
Esto se debe a que existen varias configuraciones especificas:
    - como el servidor nombre del servidor para microsoft SQL server
    - permisos para el tunnel de cloudflared, este usa la DNS https://tecrfidinventory.us/

Con esto en mente,
los comandos para ejecutar el backend son:
1. Abrir una consola en la carpeta GIT
2. cd .\TEC_RFID_INVENTORY\API\ # este comando asume que empieza de la carpeta GIT en documentos
3. python -m uvicorn main:app --reload
4. Abrir otra consola
5. cloudflared tunnel run tecrfidtunnel


Si la el puerto designado por default no es el 8000, cambiar la configuración del tunnel en el archivo:
C:\Users\Activos\.cloudflared\config.yml