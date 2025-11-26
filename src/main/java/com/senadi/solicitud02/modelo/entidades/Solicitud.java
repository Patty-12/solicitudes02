package com.senadi.solicitud02.modelo.entidades;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solicitud")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(length = 30, nullable = false)
    private String estado = "CREADA";

    // Solicitante (usuario que pide los accesos)
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    // Jefe inmediato / servidor que autoriza
    @ManyToOne
    @JoinColumn(name = "id_jefe_autoriza")
    private Usuario jefeAutoriza;

    // Solicitud -> Firma (1:N)
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Firma> firmas = new ArrayList<>();

    // Solicitud -> AccesoUsuario (1:N)
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccesoUsuario> accesos = new ArrayList<>();

    // =======================
    // Getters y Setters
    // =======================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getJefeAutoriza() {
        return jefeAutoriza;
    }

    public void setJefeAutoriza(Usuario jefeAutoriza) {
        this.jefeAutoriza = jefeAutoriza;
    }

    public List<Firma> getFirmas() {
        return firmas;
    }

    public void setFirmas(List<Firma> firmas) {
        this.firmas = firmas;
    }

    public List<AccesoUsuario> getAccesos() {
        return accesos;
    }

    public void setAccesos(List<AccesoUsuario> accesos) {
        this.accesos = accesos;
    }

    @Override
    public String toString() {
        return "Solicitud{" +
                "id=" + id +
                ", fechaCreacion=" + fechaCreacion +
                ", estado='" + estado + '\'' +
                ", usuario=" + usuario +
                ", jefeAutoriza=" + jefeAutoriza +
                ", firmas=" + firmas +
                ", accesos=" + accesos +
                '}';
    }
}
