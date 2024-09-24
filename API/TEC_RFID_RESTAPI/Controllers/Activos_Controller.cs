using Microsoft.AspNetCore.Mvc;
using TEC_RFID_RESTAPI.Models;
using TEC_RFID_RESTAPI.Data;
using System.Diagnostics.Contracts;
using Newtonsoft.Json;
using Microsoft.EntityFrameworkCore;

namespace TEC_RFID_RESTAPI.Controllers
{
    [ApiController]
    [Route("[controller]/[action]")]
    public class Activos_Controller : ControllerBase
    {
        private readonly ILogger<Activos_Controller> _logger;

        public Activos_Controller(ILogger<Activos_Controller> logger)
        {
            _logger = logger;
        }

        [HttpGet(Name = "Get_Activos")]
        public ActionResult Get_all_Activos()
        {
            RfidTecContext Db = new RfidTecContext();
            var activos_list = Db.Activos.ToList();

            return Ok(JsonConvert.SerializeObject(activos_list, Formatting.Indented, new JsonSerializerSettings { ReferenceLoopHandling = ReferenceLoopHandling.Ignore }));
        }
        [HttpPut(Name = "modificar_activo")]
        public ActionResult modificar_activo([FromForm] crear_activo activo)
        {
            try
            {
                RfidTecContext Db = new RfidTecContext();
                Db.SaveChanges();
                return Ok("Departamento ha sido modificado");
            }
            catch (Exception e)
            {
                return BadRequest("No se logro encontrar el cliente a modificar");
            }
        }
        [HttpPost(Name = "crear_activo")]
        public ActionResult crear_activo([FromForm] crear_activo activo)
        {
            try
            {
                RfidTecContext Db = new RfidTecContext();
                //parse form to DB object
                var to_add = new Activo();
                int new_id = 1;
                var all_activos = Db.Activos.ToList();
                if (all_activos.Count() > 0)
                {
                    new_id = Db.Activos.Max(p => p.Id) + 1;
                }
                to_add.Id = new_id;
                if (all_activos.Where(d => d.Nfs == activo.Nfs).Count() == 0)
                {
                    to_add.Nfs = activo.Nfs;
                    to_add.Descripcion = activo.Descripcion;
                    to_add.Nombre  = activo.Nombre;
                    if (Db.Departamentos.ToList().Where(d => d.Id == activo.IdDepartamento).Count() == 1)
                    {
                        to_add.IdDepartamento = activo.IdDepartamento;
                        Db.Activos.Add(to_add);
                        Db.SaveChanges();
                        return Ok("activo ha sido registrado");
                    }
                    else
                    {
                        return BadRequest("El departamento al que se registra el activo no es valido");
                    }
                }
                return BadRequest("El activo no es valido, su codigo NFS esta registrado a otro activo");
            }
            catch (Exception e)
            {
                return BadRequest("No existe un cliente valido");
            }
        }
    }
}
