using System;
using System.Collections.Generic;

namespace TEC_RFID_RESTAPI.Models;

public partial class Empleado
{
    public int Id { get; set; }

    public string? Contraseña { get; set; }

    public string? Email { get; set; }

    public virtual ICollection<RegistroDeActivo> RegistroDeActivos { get; set; } = new List<RegistroDeActivo>();

    public virtual ICollection<Departamento> IdDepartamentos { get; set; } = new List<Departamento>();
}
