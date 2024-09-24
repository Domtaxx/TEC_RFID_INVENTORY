using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;
using TEC_RFID_RESTAPI.Models;

namespace TEC_RFID_RESTAPI.Data;

public partial class RfidTecContext : DbContext
{
    public RfidTecContext()
    {
    }

    public RfidTecContext(DbContextOptions<RfidTecContext> options)
        : base(options)
    {
    }

    public virtual DbSet<Activo> Activos { get; set; }

    public virtual DbSet<Departamento> Departamentos { get; set; }

    public virtual DbSet<Empleado> Empleados { get; set; }

    public virtual DbSet<RegistroDeActivo> RegistroDeActivos { get; set; }

    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
#warning To protect potentially sensitive information in your connection string, you should move it out of source code. You can avoid scaffolding the connection string by using the Name= syntax to read it from configuration - see https://go.microsoft.com/fwlink/?linkid=2131148. For more guidance on storing connection strings, see https://go.microsoft.com/fwlink/?LinkId=723263.
        => optionsBuilder.UseSqlServer("Data Source=tcp:tecrfidserver.database.windows.net,1433;Initial Catalog=RFID_TEC;Encrypt=True;TrustServerCertificate=False;Connection Timeout=260;Authentication=Active Directory Default");

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Activo>(entity =>
        {
            entity.ToTable("ACTIVO");

            entity.HasIndex(e => e.Id, "UQ__ACTIVO__3214EC264C51A599").IsUnique();

            entity.Property(e => e.Id)
                .ValueGeneratedNever()
                .HasColumnName("ID");
            entity.Property(e => e.Descripcion)
                .HasMaxLength(512)
                .IsUnicode(false);
            entity.Property(e => e.IdDepartamento).HasColumnName("ID_Departamento");
            entity.Property(e => e.Nfs).HasColumnName("NFS");
            entity.Property(e => e.Nombre)
                .HasMaxLength(64)
                .IsUnicode(false);

            entity.HasOne(d => d.IdDepartamentoNavigation).WithMany(p => p.Activos)
                .HasForeignKey(d => d.IdDepartamento)
                .HasConstraintName("FK_ACTIVO_DEPARTAMENTO");
        });

        modelBuilder.Entity<Departamento>(entity =>
        {
            entity.ToTable("DEPARTAMENTO");

            entity.HasIndex(e => e.Id, "UQ__DEPARTAM__3214EC26BB393B61").IsUnique();

            entity.Property(e => e.Id)
                .ValueGeneratedNever()
                .HasColumnName("ID");
            entity.Property(e => e.Nombre)
                .HasMaxLength(256)
                .IsUnicode(false);

            entity.HasMany(d => d.IdEmpleados).WithMany(p => p.IdDepartamentos)
                .UsingEntity<Dictionary<string, object>>(
                    "EmpleadoEnDepartamento",
                    r => r.HasOne<Empleado>().WithMany()
                        .HasForeignKey("IdEmpleado")
                        .OnDelete(DeleteBehavior.ClientSetNull)
                        .HasConstraintName("FK_EMPLEADO_EN_DEPARTAMENTO_EMPLEADO"),
                    l => l.HasOne<Departamento>().WithMany()
                        .HasForeignKey("IdDepartamento")
                        .OnDelete(DeleteBehavior.ClientSetNull)
                        .HasConstraintName("FK_EMPLEADO_EN_DEPARTAMENTO_DEPARTAMENTO"),
                    j =>
                    {
                        j.HasKey("IdDepartamento", "IdEmpleado");
                        j.ToTable("EMPLEADO_EN_DEPARTAMENTO");
                        j.IndexerProperty<int>("IdDepartamento").HasColumnName("ID_Departamento");
                        j.IndexerProperty<int>("IdEmpleado").HasColumnName("ID_Empleado");
                    });
        });

        modelBuilder.Entity<Empleado>(entity =>
        {
            entity.ToTable("EMPLEADO");

            entity.HasIndex(e => e.Id, "UQ__EMPLEADO__3214EC2688490FDC").IsUnique();

            entity.Property(e => e.Id)
                .ValueGeneratedNever()
                .HasColumnName("ID");
            entity.Property(e => e.Contraseña)
                .HasMaxLength(255)
                .IsUnicode(false);
            entity.Property(e => e.Email)
                .HasMaxLength(255)
                .IsUnicode(false);
        });

        modelBuilder.Entity<RegistroDeActivo>(entity =>
        {
            entity.HasKey(e => new { e.IdEmpleado, e.IdActivo });

            entity.ToTable("REGISTRO_DE_ACTIVOS");

            entity.Property(e => e.IdEmpleado).HasColumnName("ID_Empleado");
            entity.Property(e => e.IdActivo).HasColumnName("ID_Activo");
            entity.Property(e => e.Fecha).HasColumnType("datetime");
            entity.Property(e => e.Periodo)
                .HasMaxLength(255)
                .IsUnicode(false);

            entity.HasOne(d => d.IdActivoNavigation).WithMany(p => p.RegistroDeActivos)
                .HasForeignKey(d => d.IdActivo)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_REGISTRO_DE_ACTIVOS_ACTIVOS");

            entity.HasOne(d => d.IdEmpleadoNavigation).WithMany(p => p.RegistroDeActivos)
                .HasForeignKey(d => d.IdEmpleado)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_REGISTRO_DE_ACTIVOS_EMPLEADO");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
