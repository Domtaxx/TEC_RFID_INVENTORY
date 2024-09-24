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
    public class Empleado_controller : ControllerBase
    {
        private readonly ILogger<Empleado_controller> _logger;

        public Empleado_controller(ILogger<Empleado_controller> logger)
        {
            _logger = logger;
        }

        [HttpGet(Name = "Get_empleados")]
        public ActionResult Get_all_users()
        {
            RfidTecContext Db = new RfidTecContext();
            var emp_list = Db.Empleados.Include(e => e.IdDepartamentos).ToList();

            return Ok(JsonConvert.SerializeObject(emp_list, Formatting.Indented, new JsonSerializerSettings { ReferenceLoopHandling = ReferenceLoopHandling.Ignore }));
        }
        [HttpGet(Name = "login")]
        public ActionResult login(string Email, string password)
        {
            try
            {
                RfidTecContext Db = new RfidTecContext();
                var client = Db.Empleados.ToList().Where(
                    m => (m.Email == Email) & (m.Contraseña == password)
                ).Single();
                return Ok(JsonConvert.SerializeObject(client, Formatting.Indented, new JsonSerializerSettings { ReferenceLoopHandling = ReferenceLoopHandling.Ignore })
                );
            }
            catch (Exception e)
            {
                return BadRequest("No existe un cliente valido");
            }
        }
        [HttpPut(Name = "modificar_contra")]
        public ActionResult modificar_contra([FromForm] crear_empleado empleado)
        {
            try
            {
                RfidTecContext Db = new RfidTecContext();
                var emp_to_mod = Db.Empleados.ToList().Where(d => d.Email == empleado.Email).Single();
                emp_to_mod.Contraseña = empleado.Contraseña;
                Db.SaveChanges();
                return Ok("Departamento ha sido modificado");
            }
            catch (Exception e)
            {
                return BadRequest("No se logro encontrar el cliente a modificar");
            }
        }
        [HttpPost(Name = "crear_usuario")]
        public ActionResult crear_usuario([FromForm]crear_empleado empleado)
        {
            try
            {
                RfidTecContext Db = new RfidTecContext();
                //parse form to DB object
                var to_add = new Empleado();
                int new_id = 1;
                var all_emp = Db.Empleados.Include(e => e.IdDepartamentos).ToList();
                if (all_emp.Count() > 0)
                {
                    new_id = Db.Empleados.Max(p => p.Id) + 1;
                }
                to_add.Id = new_id;
                if (all_emp.Where(d => d.Email == empleado.Email).Count() == 0)
                {
                    to_add.Email = empleado.Email;
                    to_add.Contraseña = empleado.Contraseña;
                    to_add.IdDepartamentos = [];
                    if (Db.Departamentos.ToList().Where(d => d.Id == empleado.IdDepartamento).Count() == 1)
                    {
                        to_add.IdDepartamentos.Add(Db.Departamentos.Include(d => d.IdEmpleados).ToList().Where(d => d.Id == empleado.IdDepartamento).Single());
                        Db.Empleados.Add(to_add);
                        Db.SaveChanges();
                        return Ok("Empleado ha sido registrado");
                    }
                    else {
                        return BadRequest("El departamento no es valido");
                    }
                }
                return BadRequest("Ese correo ya esta registrado");
            }
            catch (Exception e)
            {
                return BadRequest("No existe un cliente valido");
            }
        }
    }
}
