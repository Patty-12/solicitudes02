package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.senadi.solicitud02.controlador.PermisoAplicacionControlador;
import com.senadi.solicitud02.controlador.AplicacionControlador;
import com.senadi.solicitud02.controlador.impl.PermisoAplicacionControladorImpl;
import com.senadi.solicitud02.controlador.impl.AplicacionControladorImpl;
import com.senadi.solicitud02.modelo.entidades.PermisoAplicacion;
import com.senadi.solicitud02.modelo.entidades.Aplicacion;

@ManagedBean(name = "permisoAplicacionBean")
@ViewScoped
public class PermisoAplicacionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private PermisoAplicacionControlador permisoCtrl = new PermisoAplicacionControladorImpl();
    private AplicacionControlador aplicacionCtrl = new AplicacionControladorImpl();

    // Listado principal
    private List<PermisoAplicacion> lista;

    // Formulario (crear / editar)
    private PermisoAplicacion formulario;

    // Registro seleccionado para eliminar
    private PermisoAplicacion permisoSeleccionado;

    // Soporte para combos
    private List<Aplicacion> aplicaciones;
    private Long idAplicacionSeleccionada;

    // Para edición vía ?id=XX
    private Long idPermisoAplicacion;

    @PostConstruct
    public void init() {
        listar();
        formulario = new PermisoAplicacion();
        cargarAplicaciones();
    }

    private void cargarAplicaciones() {
        aplicaciones = aplicacionCtrl.listarTodos();
    }

    public void listar() {
        lista = permisoCtrl.listarTodos();
    }

    /**
     * Cargar un permiso específico para edición (usado por <f:viewAction> en edit.xhtml).
     */
    public String cargarPermisoAplicacion() {
        if (idPermisoAplicacion != null) {
            PermisoAplicacion p = permisoCtrl.buscarPorId(idPermisoAplicacion);
            if (p != null) {
                permisoSeleccionado = p;
                formulario = p;

                if (p.getAplicacion() != null) {
                    idAplicacionSeleccionada = p.getAplicacion().getId();
                }
                cargarAplicaciones();
            } else {
                FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Permiso no encontrado",
                            "El registro solicitado no existe."));
            }
        }
        return null;
    }

    /**
     * Crear o actualizar permiso.
     */
    public String guardar() {
        try {
            // Resolver aplicación
            if (idAplicacionSeleccionada != null) {
                Aplicacion a = aplicacionCtrl.buscarPorId(idAplicacionSeleccionada);
                formulario.setAplicacion(a);
            } else {
                formulario.setAplicacion(null);
            }

            boolean esNuevo = (formulario.getId() == null);

            if (esNuevo) {
                permisoCtrl.crear(formulario);
            } else {
                permisoCtrl.actualizar(formulario);
            }

            listar();
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Éxito",
                        "Permiso de aplicación guardado correctamente."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error al guardar",
                        e.getMessage()));
        }
        return null; // por ahora nos quedamos en la misma vista
    }

    /**
     * Eliminar el permiso seleccionado.
     */
    public String eliminar() {
        if (permisoSeleccionado != null && permisoSeleccionado.getId() != null) {
            try {
                // Verificar si tiene accesos asociados
                if (permisoSeleccionado.getAccesos() != null &&
                    !permisoSeleccionado.getAccesos().isEmpty()) {

                    FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "No se puede eliminar",
                            "El permiso tiene accesos de usuario asociados. " +
                            "Primero elimine o reasigne esos accesos.")
                    );
                    return null;
                }

                // Eliminar si no tiene hijos
                permisoCtrl.eliminar(permisoSeleccionado.getId());
                listar();

                FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        "Eliminado",
                        "El permiso ha sido eliminado correctamente.")
                );

            } catch (Exception e) {
                e.printStackTrace(); // detalle en consola

                String detalle = (e.getCause() != null && e.getCause().getMessage() != null)
                        ? e.getCause().getMessage()
                        : e.getMessage();

                FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Error al eliminar",
                        detalle)
                );
            }
        }
        return null;
    }

    public void cancelar() {
        formulario = new PermisoAplicacion();
        idAplicacionSeleccionada = null;
        cargarAplicaciones();
    }

    // ================= GETTERS / SETTERS =================

    public List<PermisoAplicacion> getLista() {
        return lista;
    }

    public void setLista(List<PermisoAplicacion> lista) {
        this.lista = lista;
    }

    public PermisoAplicacion getFormulario() {
        return formulario;
    }

    public void setFormulario(PermisoAplicacion formulario) {
        this.formulario = formulario;
    }

    public PermisoAplicacion getPermisoSeleccionado() {
        return permisoSeleccionado;
    }

    public void setPermisoSeleccionado(PermisoAplicacion permisoSeleccionado) {
        this.permisoSeleccionado = permisoSeleccionado;
    }

    public List<Aplicacion> getAplicaciones() {
        return aplicaciones;
    }

    public void setAplicaciones(List<Aplicacion> aplicaciones) {
        this.aplicaciones = aplicaciones;
    }

    public Long getIdAplicacionSeleccionada() {
        return idAplicacionSeleccionada;
    }

    public void setIdAplicacionSeleccionada(Long idAplicacionSeleccionada) {
        this.idAplicacionSeleccionada = idAplicacionSeleccionada;
    }

    public Long getIdPermisoAplicacion() {
        return idPermisoAplicacion;
    }

    public void setIdPermisoAplicacion(Long idPermisoAplicacion) {
        this.idPermisoAplicacion = idPermisoAplicacion;
    }
}
