using System;
using System.Collections.Generic;

namespace TEC_RFID_RESTAPI.Models;

public partial class Activo
{
    public int Id { get; set; }

    public string? Nombre { get; set; }

    public string? Descripcion { get; set; }

    public int? IdDepartamento { get; set; }

    public int? Nfs { get; set; }

    public virtual Departamento? IdDepartamentoNavigation { get; set; }

    public virtual ICollection<RegistroDeActivo> RegistroDeActivos { get; set; } = new List<RegistroDeActivo>();
}
