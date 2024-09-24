using System;
using System.Collections.Generic;

namespace TEC_RFID_RESTAPI.Models;

public partial class Departamento
{
    public int Id { get; set; }

    public string? Nombre { get; set; }

    public virtual ICollection<Activo> Activos { get; set; } = new List<Activo>();

    public virtual ICollection<Empleado> IdEmpleados { get; set; } = new List<Empleado>();
}
