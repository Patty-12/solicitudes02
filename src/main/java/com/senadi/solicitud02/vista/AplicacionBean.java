package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.util.ArrayList;
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

    // para edit.xhtml (viewParam)
    private Long idAplicacion;

    // Filtro por nombre
    private String filtroNombre;

    @PostConstruct
    public void init() {
        formulario = new Aplicacion();
        cargarLista();
    }

    private void cargarLista() {
        try {
            lista = aplicacionCtrl.listarTodos();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error al cargar aplicaciones", e.getMessage())
            );
        }
    }

    // ==============================
    //   ACCIONES
    // ==============================

    /** Usado en create.xhtml y edit.xhtml */
    public String guardar() {
        try {
            if (formulario.getId() == null) {
                aplicacionCtrl.crear(formulario);
                FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Aplicación creada",
                            "La aplicación se guardó correctamente.")
                );
            } else {
                aplicacionCtrl.actualizar(formulario);
                FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Aplicación actualizada",
                            "La aplicación se actualizó correctamente.")
                );
            }

            cargarLista();
            return "/Aplicacion/index.xhtml?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error al guardar aplicación", e.getMessage())
            );
            return null;
        }
    }

    /** Usado en index.xhtml (lo podemos dejar por si algún día se usa de nuevo) */
    public String eliminar() {
        if (aplicacionSeleccionada == null || aplicacionSeleccionada.getId() == null) {
            return null;
        }

        try {
            aplicacionCtrl.eliminar(aplicacionSeleccionada.getId());
            cargarLista();
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Aplicación eliminada",
                        "La aplicación se eliminó correctamente.")
            );
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error al eliminar aplicación", e.getMessage())
            );
        }
        return null;  // mantenerse en el listado
    }

    /** Dummy para evitar PropertyNotFoundException si existe alguna EL "#{aplicacionBean.eliminar}" como propiedad. */
    public Object getEliminar() {
        return null;
    }

    /** Se llama desde edit.xhtml */
    public void cargarAplicacion() {
        if (idAplicacion == null) {
            return;
        }
        try {
            Aplicacion a = aplicacionCtrl.buscarPorId(idAplicacion);
            if (a != null) {
                formulario = a;
            } else {
                FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Aplicación no encontrada",
                            "ID: " + idAplicacion)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error al cargar la aplicación", e.getMessage())
            );
        }
    }

    // ==============================
    //   LISTA FILTRADA
    // ==============================

    public List<Aplicacion> getListaFiltrada() {
        if (lista == null) {
            return null;
        }
        if (filtroNombre == null || filtroNombre.trim().isEmpty()) {
            return lista;
        }
        String f = filtroNombre.trim().toLowerCase();
        List<Aplicacion> filtrada = new ArrayList<>();
        for (Aplicacion a : lista) {
            if (a.getNombre() != null &&
                a.getNombre().toLowerCase().contains(f)) {
                filtrada.add(a);
            }
        }
        return filtrada;
    }

    // ==============================
    //   GETTERS / SETTERS
    // ==============================

    public List<Aplicacion> getLista() {
        return lista;
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

    public String getFiltroNombre() {
        return filtroNombre;
    }

    public void setFiltroNombre(String filtroNombre) {
        this.filtroNombre = filtroNombre;
    }
}
