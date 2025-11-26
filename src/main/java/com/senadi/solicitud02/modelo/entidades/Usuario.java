package com.senadi.solicitud02.modelo.entidades;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 80, nullable = false)
    private String nombre;

    @Column(length = 80, nullable = false)
    private String apellido;

    // NUEVO: Cédula
    @Column(name = "CEDULA", length = 20)
    private String cedula;

    @Column(length = 80)
    private String cargo;

    // NUEVO: Unidad/Gestión
    @Column(name = "UNIDAD_GESTION", length = 120)
    private String unidadGestion;

    @Column(length = 120, nullable = false, unique = true)
    private String correo;

    @Column(length = 120, nullable = false, unique = true)
    private String password;

    // Relación con Solicitud
    @OneToMany(mappedBy = "usuario")
    private List<Solicitud> solicitudes = new ArrayList<>();

    // Relación con UsuarioRol
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsuarioRol> rolesAsignados = new ArrayList<>();

    // Relación con Auditoria
    @OneToMany(mappedBy = "usuario")
    private List<Auditoria> auditorias = new ArrayList<>();

    // ====== GETTERS / SETTERS ======

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getUnidadGestion() { return unidadGestion; }
    public void setUnidadGestion(String unidadGestion) { this.unidadGestion = unidadGestion; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<Solicitud> getSolicitudes() { return solicitudes; }
    public void setSolicitudes(List<Solicitud> solicitudes) { this.solicitudes = solicitudes; }

    public List<UsuarioRol> getRolesAsignados() { return rolesAsignados; }
    public void setRolesAsignados(List<UsuarioRol> rolesAsignados) { this.rolesAsignados = rolesAsignados; }

    public List<Auditoria> getAuditorias() { return auditorias; }
    public void setAuditorias(List<Auditoria> auditorias) { this.auditorias = auditorias; }

    @Override
    public String toString() {
        return "Usuario [id=" + id
                + ", nombre=" + nombre
                + ", apellido=" + apellido
                + ", cedula=" + cedula
                + ", unidadGestion=" + unidadGestion
                + ", correo=" + correo
                + ", cargo=" + cargo + "]";
    }
}
