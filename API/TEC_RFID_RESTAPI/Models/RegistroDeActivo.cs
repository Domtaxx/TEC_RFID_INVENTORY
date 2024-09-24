using System;
using System.Collections.Generic;

namespace TEC_RFID_RESTAPI.Models;

public partial class RegistroDeActivo
{
    public int IdEmpleado { get; set; }

    public int IdActivo { get; set; }

    public DateTime? Fecha { get; set; }

    public string? Periodo { get; set; }

    public virtual Activo IdActivoNavigation { get; set; } = null!;

    public virtual Empleado IdEmpleadoNavigation { get; set; } = null!;
}
