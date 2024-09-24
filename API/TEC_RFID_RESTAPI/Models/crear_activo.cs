using System;
using System.Collections.Generic;

namespace TEC_RFID_RESTAPI.Models;

public class crear_activo
{
    public string? Nombre { get; set; }

    public string? Descripcion { get; set; }

    public int? IdDepartamento { get; set; }

    public int? Nfs { get; set; }
}
