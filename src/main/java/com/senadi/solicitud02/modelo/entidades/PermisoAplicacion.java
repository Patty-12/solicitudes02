package com.senadi.solicitud02.modelo.entidades;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "permiso_aplicacion")
public class PermisoAplicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_aplicacion", nullable = false)
    private Aplicacion aplicacion;

    // Permiso -> AccesoUsuario (1:N)
    @OneToMany(mappedBy = "permiso")
    private List<AccesoUsuario> accesos = new ArrayList<>();

    // ---- Campo solo para la vista (no se persiste en BD) ----
    @Transient
    private boolean seleccionado;

    // ================= GETTERS / SETTERS =================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Aplicacion getAplicacion() {
        return aplicacion;
    }

    public void setAplicacion(Aplicacion aplicacion) {
        this.aplicacion = aplicacion;
    }

    public List<AccesoUsuario> getAccesos() {
        return accesos;
    }

    public void setAccesos(List<AccesoUsuario> accesos) {
        this.accesos = accesos;
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
    }

    @Override
    public String toString() {
        return "PermisoAplicacion [id=" + id
                + ", nombre=" + nombre
                + ", descripcion=" + descripcion
                + ", aplicacion=" + aplicacion
                + ", accesos=" + accesos + "]";
    }
}
