package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.senadi.solicitud02.controlador.AccesoUsuarioControlador;
import com.senadi.solicitud02.controlador.SolicitudControlador;
import com.senadi.solicitud02.controlador.PermisoAplicacionControlador;

import com.senadi.solicitud02.controlador.impl.AccesoUsuarioControladorImpl;
import com.senadi.solicitud02.controlador.impl.SolicitudControladorImpl;
import com.senadi.solicitud02.controlador.impl.PermisoAplicacionControladorImpl;

import com.senadi.solicitud02.modelo.entidades.AccesoUsuario;
import com.senadi.solicitud02.modelo.entidades.Solicitud;
import com.senadi.solicitud02.modelo.entidades.PermisoAplicacion;

@ManagedBean(name = "accesoUsuarioBean")
@ViewScoped
public class AccesoUsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private AccesoUsuarioControlador accesoCtrl = new AccesoUsuarioControladorImpl();
    private SolicitudControlador solicitudCtrl = new SolicitudControladorImpl();
    private PermisoAplicacionControlador permisoCtrl = new PermisoAplicacionControladorImpl();

    // Listado principal
    private List<AccesoUsuario> lista;

    // Formulario (crear/editar)
    private AccesoUsuario formulario;

    // Registro seleccionado para eliminar
    private AccesoUsuario accesoSeleccionado;

    // Listas de apoyo para combos
    private List<Solicitud> solicitudes;
    private List<PermisoAplicacion> permisos;

    // IDs seleccionados desde la vista
    private Long idSolicitudSeleccionada;
    private Long idPermisoSeleccionado;

    // Para editar por ?id=XX
    private Long idAcceso;

    @PostConstruct
    public void init() {
        listar();
        formulario = new AccesoUsuario();
        cargarSolicitudesYPermisos();
    }

    private void cargarSolicitudesYPermisos() {
        solicitudes = solicitudCtrl.listarTodos();
        permisos = permisoCtrl.listarTodos();
    }

    public void listar() {
        lista = accesoCtrl.listarTodos();
    }

    /**
     * Cargar un acceso específico para edición (usado por <f:viewAction>)
     */
    public String cargarAcceso() {
        if (idAcceso != null) {
            AccesoUsuario a = accesoCtrl.buscarPorId(idAcceso);
            if (a != null) {
                accesoSeleccionado = a;
                formulario = a;

                if (a.getSolicitud() != null) {
                    idSolicitudSeleccionada = a.getSolicitud().getId();
                }
                if (a.getPermiso() != null) {
                    idPermisoSeleccionado = a.getPermiso().getId();
                }
                cargarSolicitudesYPermisos();
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Acceso no encontrado",
                                "El registro solicitado no existe."));
            }
        }
        return null;
    }

    /**
     * Crear o actualizar un acceso.
     */
    public String guardar() {
        try {
            // Resolver Solicitud
            if (idSolicitudSeleccionada != null) {
                Solicitud s = solicitudCtrl.buscarPorId(idSolicitudSeleccionada);
                formulario.setSolicitud(s);
            } else {
                formulario.setSolicitud(null);
            }

            // Resolver Permiso
            if (idPermisoSeleccionado != null) {
                PermisoAplicacion p = permisoCtrl.buscarPorId(idPermisoSeleccionado);
                formulario.setPermiso(p);
            } else {
                formulario.setPermiso(null);
            }

            boolean esNuevo = (formulario.getId() == null);

            if (esNuevo) {
                if (formulario.getFechaCarga() == null) {
                    formulario.setFechaCarga(LocalDateTime.now());
                }
                accesoCtrl.crear(formulario);
            } else {
                accesoCtrl.actualizar(formulario);
            }

            listar();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito",
                            "Acceso guardado correctamente."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al guardar",
                            e.getMessage()));
        }
        return null;
    }

    /**
     * Eliminar el acceso seleccionado.
     */
    public String eliminar() {
        if (accesoSeleccionado != null && accesoSeleccionado.getId() != null) {
            try {
                accesoCtrl.eliminar(accesoSeleccionado.getId());
                listar();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Eliminado",
                                "El acceso ha sido eliminado."));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error al eliminar",
                                e.getMessage()));
            }
        }
        return null;
    }

    public void cancelar() {
        formulario = new AccesoUsuario();
        idSolicitudSeleccionada = null;
        idPermisoSeleccionado = null;
        cargarSolicitudesYPermisos();
    }

    // ================= GETTERS / SETTERS =================

    public List<AccesoUsuario> getLista() {
        return lista;
    }

    public void setLista(List<AccesoUsuario> lista) {
        this.lista = lista;
    }

    public AccesoUsuario getFormulario() {
        return formulario;
    }

    public void setFormulario(AccesoUsuario formulario) {
        this.formulario = formulario;
    }

    public AccesoUsuario getAccesoSeleccionado() {
        return accesoSeleccionado;
    }

    public void setAccesoSeleccionado(AccesoUsuario accesoSeleccionado) {
        this.accesoSeleccionado = accesoSeleccionado;
    }

    public List<Solicitud> getSolicitudes() {
        return solicitudes;
    }

    public void setSolicitudes(List<Solicitud> solicitudes) {
        this.solicitudes = solicitudes;
    }

    public List<PermisoAplicacion> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<PermisoAplicacion> permisos) {
        this.permisos = permisos;
    }

    public Long getIdSolicitudSeleccionada() {
        return idSolicitudSeleccionada;
    }

    public void setIdSolicitudSeleccionada(Long idSolicitudSeleccionada) {
        this.idSolicitudSeleccionada = idSolicitudSeleccionada;
    }

    public Long getIdPermisoSeleccionado() {
        return idPermisoSeleccionado;
    }

    public void setIdPermisoSeleccionado(Long idPermisoSeleccionado) {
        this.idPermisoSeleccionado = idPermisoSeleccionado;
    }

    public Long getIdAcceso() {
        return idAcceso;
    }

    public void setIdAcceso(Long idAcceso) {
        this.idAcceso = idAcceso;
    }
}
