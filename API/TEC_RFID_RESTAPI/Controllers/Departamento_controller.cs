using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Newtonsoft.Json;
using TEC_RFID_RESTAPI.Data;
using TEC_RFID_RESTAPI.Models;

namespace TEC_RFID_RESTAPI.Controllers
{
    [ApiController]
    [Route("[controller]/[action]")]
    public class Departamento_controller : ControllerBase
    {
        private readonly ILogger<Departamento_controller> _logger;
        public Departamento_controller(ILogger<Departamento_controller> logger)
        {
            _logger = logger;
        }
        [HttpGet(Name = "get_departamentos")]
        public ActionResult get_departamentos()
        {
            try
            {
                RfidTecContext Db = new RfidTecContext();
                var dep_list = Db.Departamentos.Include(d => d.IdEmpleados).Include(d => d.Activos).ToList();

                return Ok(JsonConvert.SerializeObject(dep_list, Formatting.Indented, new JsonSerializerSettings { ReferenceLoopHandling = ReferenceLoopHandling.Ignore }));

            }
            catch (Exception e)
            {
                return BadRequest("No se logro encontrar departamentos");
            }
        }
        [HttpPut(Name = "modificar_departamento")]
        public ActionResult modificar_departamento([FromForm] crear_departamento departamento)
        {
            try
            {
                RfidTecContext Db = new RfidTecContext();
                var dep_to_mod = Db.Departamentos.ToList().Where(d => d.Id == departamento.ID).Single();
                var deps_with_new_name = Db.Departamentos.ToList().Where(d => d.Nombre == departamento.nombreDepartamento);
                if (deps_with_new_name.Count() == 0)
                {
                    dep_to_mod.Nombre = departamento.nombreDepartamento;
                    Db.SaveChanges();
                    return Ok("Departamento ha sido modificado");
                }
                return BadRequest("El nombre del departamento ya existe");

            }
            catch (Exception e)
            {
                return BadRequest("No se logro encontrar el departamento a modificar");
            }
        }
        [HttpDelete(Name = "eliminar_de_departamento")]
        public ActionResult eliminar_de_departamento(int id_escuela, int id_empleado)
        {
            try
            {
                RfidTecContext Db = new RfidTecContext();
                var dep_to_mod = Db.Departamentos.Include(d => d.IdEmpleados).Include(d => d.Activos).ToList().Where(d => d.Id == id_escuela).Single();
                var emp_to_del = dep_to_mod.IdEmpleados.ToList().Where(e => e.Id == id_empleado).Single();
                dep_to_mod.IdEmpleados.Remove(emp_to_del);
                Db.SaveChanges();
                return Ok("Departamento ha sido modificado, se elimino el empleado");
            }
            catch (Exception e)
            {
                return BadRequest("No se logro encontrar el departamento a modificar");
            }
        }

        [HttpPost(Name = "crear_departamento")]
        public ActionResult crear_departamento([FromForm] crear_departamento departamento)
        {
            try
            {
                RfidTecContext Db = new RfidTecContext();
                int new_id = 1;
                //parse form to DB object
                var to_add = new Departamento();
                var all_dep = Db.Departamentos.ToList();
                if (all_dep.Count() > 0)
                {
                    new_id = Db.Departamentos.Max(p => p.Id) + 1;
                }
                to_add.Id = new_id;

                if(all_dep.Where(d => d.Nombre == departamento.nombreDepartamento).Count() == 0)
                {
                    to_add.Nombre = departamento.nombreDepartamento;
                    Db.Departamentos.Add(to_add);
                    Db.SaveChanges();
                    return Ok("Departamento ha sido registrado");
                }
                return BadRequest("No se logro crear el departamento, este ya existe");

            }
            catch (Exception e)
            {
                return BadRequest("No se logro crear el departamento");
            }
        }
    }
}
