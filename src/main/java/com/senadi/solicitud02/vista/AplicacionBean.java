package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.senadi.solicitud02.controlador.AplicacionControlador;
import com.senadi.solicitud02.controlador.impl.AplicacionControladorImpl;
import com.senadi.solicitud02.modelo.entidades.Aplicacion;

@ManagedBean(name = "aplicacionBean")
@ViewScoped
public class AplicacionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private AplicacionControlador aplicacionCtrl = new AplicacionControladorImpl();

    private List<Aplicacion> lista;
    private Aplicacion formulario;
    private Aplicacion aplicacionSeleccionada;

    // Para cargar por parámetro en edit.xhtml (?id=XX)
    private Long idAplicacion;

    @PostConstruct
    public void init() {
        listar();
        formulario = new Aplicacion();
    }

    public void listar() {
        lista = aplicacionCtrl.listarTodos();
    }

    /** Preparar para nueva aplicación (create.xhtml) */
    public void prepararNuevo() {
        formulario = new Aplicacion();
        formulario.setFechaCreacion(LocalDateTime.now());
    }

    /** Preparar para edición cuando ya tenemos aplicacionSeleccionada */
    public void prepararEdicion() {
        if (aplicacionSeleccionada != null) {
            formulario = aplicacionSeleccionada;
        }
    }

    /** Usado desde edit.xhtml con <f:viewParam> */
    public void cargarAplicacion() {
        if (idAplicacion != null) {
            Aplicacion app = aplicacionCtrl.buscarPorId(idAplicacion);
            if (app != null) {
                this.aplicacionSeleccionada = app;
                prepararEdicion();
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                         "Aplicación no encontrada",
                                         "El registro solicitado no existe."));
            }
        }
    }

    public void guardar() {
        try {
            boolean esNueva = (formulario.getId() == null);

            if (esNueva) {
                if (formulario.getFechaCreacion() == null) {
                    formulario.setFechaCreacion(LocalDateTime.now());
                }
                aplicacionCtrl.crear(formulario);
            } else {
                aplicacionCtrl.actualizar(formulario);
            }

            listar();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                                     "Éxito",
                                     "Aplicación guardada correctamente."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                     "Error al guardar",
                                     e.getMessage()));
        }
    }

    public void eliminar() {
        if (aplicacionSeleccionada != null && aplicacionSeleccionada.getId() != null) {
            try {
                aplicacionCtrl.eliminar(aplicacionSeleccionada.getId());
                listar();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                         "Eliminada",
                                         "La aplicación ha sido eliminada."));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                         "Error al eliminar",
                                         e.getMessage()));
            }
        }
    }

    public void cancelar() {
        formulario = new Aplicacion();
    }

    // ===== GETTERS y SETTERS =====

    public List<Aplicacion> getLista() {
        return lista;
    }

    public void setLista(List<Aplicacion> lista) {
        this.lista = lista;
    }

    public Aplicacion getFormulario() {
        return formulario;
    }

    public void setFormulario(Aplicacion formulario) {
        this.formulario = formulario;
    }

    public Aplicacion getAplicacionSeleccionada() {
        return aplicacionSeleccionada;
    }

    public void setAplicacionSeleccionada(Aplicacion aplicacionSeleccionada) {
        this.aplicacionSeleccionada = aplicacionSeleccionada;
    }

    public Long getIdAplicacion() {
        return idAplicacion;
    }

    public void setIdAplicacion(Long idAplicacion) {
        this.idAplicacion = idAplicacion;
    }
}
