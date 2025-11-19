package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.senadi.solicitud02.controlador.FirmaControlador;
import com.senadi.solicitud02.controlador.SolicitudControlador;
import com.senadi.solicitud02.controlador.impl.FirmaControladorImpl;
import com.senadi.solicitud02.controlador.impl.SolicitudControladorImpl;
import com.senadi.solicitud02.modelo.entidades.Firma;
import com.senadi.solicitud02.modelo.entidades.Solicitud;

@ManagedBean(name = "firmaBean")
@ViewScoped
public class FirmaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private FirmaControlador firmaCtrl = new FirmaControladorImpl();
    private SolicitudControlador solicitudCtrl = new SolicitudControladorImpl();

    // Listado principal
    private List<Firma> lista;

    // Formulario (crear/editar)
    private Firma formulario;

    // Registro seleccionado para eliminar
    private Firma firmaSeleccionada;

    // Listado de solicitudes para el combo
    private List<Solicitud> solicitudes;

    // ID de solicitud seleccionada en el formulario
    private Long idSolicitudSeleccionada;

    // Para editar por ?id=XX
    private Long idFirma;

    @PostConstruct
    public void init() {
        listar();
        formulario = new Firma();
        cargarSolicitudes();
    }

    private void cargarSolicitudes() {
        solicitudes = solicitudCtrl.listarTodos();
    }

    public void listar() {
        lista = firmaCtrl.listarTodos();
    }

    /**
     * Cargar una firma específica para edición (usado por <f:viewAction>).
     */
    public String cargarFirma() {
        if (idFirma != null) {
            Firma f = firmaCtrl.buscarPorId(idFirma);
            if (f != null) {
                firmaSeleccionada = f;
                formulario = f;

                if (f.getSolicitud() != null) {
                    idSolicitudSeleccionada = f.getSolicitud().getId();
                }

                cargarSolicitudes();
            } else {
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Firma no encontrada",
                                "El registro solicitado no existe."));
            }
        }
        return null;
    }

    /**
     * Crear o actualizar una firma.
     */
    public String guardar() {
        try {
            // Resolver solicitud
            if (idSolicitudSeleccionada != null) {
                Solicitud s = solicitudCtrl.buscarPorId(idSolicitudSeleccionada);
                formulario.setSolicitud(s);
            } else {
                formulario.setSolicitud(null);
            }

            boolean esNuevo = (formulario.getId() == null);

            if (esNuevo) {
                if (formulario.getFechaFirma() == null) {
                    formulario.setFechaFirma(LocalDateTime.now());
                }
                firmaCtrl.crear(formulario);
            } else {
                firmaCtrl.actualizar(formulario);
            }

            listar();
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito",
                            "Firma guardada correctamente."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al guardar",
                            e.getMessage()));
        }
        return null;
    }

    /**
     * Eliminar la firma seleccionada.
     */
    public String eliminar() {
        if (firmaSeleccionada != null && firmaSeleccionada.getId() != null) {
            try {
                firmaCtrl.eliminar(firmaSeleccionada.getId());
                listar();
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Eliminado",
                                "La firma ha sido eliminada."));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error al eliminar",
                                e.getMessage()));
            }
        }
        return null;
    }

    public void cancelar() {
        formulario = new Firma();
        idSolicitudSeleccionada = null;
        cargarSolicitudes();
    }

    // ================= GETTERS / SETTERS =================

    public List<Firma> getLista() {
        return lista;
    }

    public void setLista(List<Firma> lista) {
        this.lista = lista;
    }

    public Firma getFormulario() {
        return formulario;
    }

    public void setFormulario(Firma formulario) {
        this.formulario = formulario;
    }

    public Firma getFirmaSeleccionada() {
        return firmaSeleccionada;
    }

    public void setFirmaSeleccionada(Firma firmaSeleccionada) {
        this.firmaSeleccionada = firmaSeleccionada;
    }

    public List<Solicitud> getSolicitudes() {
        return solicitudes;
    }

    public void setSolicitudes(List<Solicitud> solicitudes) {
        this.solicitudes = solicitudes;
    }

    public Long getIdSolicitudSeleccionada() {
        return idSolicitudSeleccionada;
    }

    public void setIdSolicitudSeleccionada(Long idSolicitudSeleccionada) {
        this.idSolicitudSeleccionada = idSolicitudSeleccionada;
    }

    public Long getIdFirma() {
        return idFirma;
    }

    public void setIdFirma(Long idFirma) {
        this.idFirma = idFirma;
    }
}
