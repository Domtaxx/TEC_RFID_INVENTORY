using System;
using System.Collections.Generic;

namespace TEC_RFID_RESTAPI.Models;

public class crear_empleado
{
    public string Email { get; set; }
    public string Contraseña { get; set; }
    public int IdDepartamento { get; set; }
}
