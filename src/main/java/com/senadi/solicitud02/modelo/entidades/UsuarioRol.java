package com.senadi.solicitud02.modelo.entidades;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_rol",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario","id_rol"}))
public class UsuarioRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion = LocalDateTime.now();

    // ================== GETTERS / SETTERS ==================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    @Override
    public String toString() {
        return "UsuarioRol [id=" + id
                + ", usuario=" + usuario
                + ", rol=" + rol
                + ", fechaAsignacion=" + fechaAsignacion
                + "]";
    }
}
