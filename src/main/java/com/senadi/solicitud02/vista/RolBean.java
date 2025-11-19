package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.senadi.solicitud02.controlador.RolControlador;
import com.senadi.solicitud02.controlador.impl.RolControladorImpl;
import com.senadi.solicitud02.modelo.entidades.Rol;

@ManagedBean(name = "rolBean")
@ViewScoped
public class RolBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private RolControlador rolCtrl = new RolControladorImpl();

    private List<Rol> lista;
    private Rol formulario;
    private Rol rolSeleccionado;

    // Para editar por ?id=XX
    private Long idRol;

    @PostConstruct
    public void init() {
        listar();
        formulario = new Rol();
    }

    public void listar() {
        lista = rolCtrl.listarTodos();
    }

    /**
     * Cargar un rol específico para edición (usado por <f:viewAction>).
     */
    public String cargarRol() {
        if (idRol != null) {
            Rol r = rolCtrl.buscarPorId(idRol);
            if (r != null) {
                rolSeleccionado = r;
                formulario = r;
            } else {
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Rol no encontrado",
                                "El registro solicitado no existe."));
            }
        }
        return null;
    }

    /**
     * Crear o actualizar un rol.
     */
    public String guardar() {
        try {
            boolean esNuevo = (formulario.getId() == null);

            if (esNuevo) {
                rolCtrl.crear(formulario);
            } else {
                rolCtrl.actualizar(formulario);
            }

            listar();
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito",
                            "Rol guardado correctamente."));
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
     * Eliminar el rol seleccionado.
     */
    public String eliminar() {
        if (rolSeleccionado != null && rolSeleccionado.getId() != null) {
            try {
                rolCtrl.eliminar(rolSeleccionado.getId());
                listar();
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Eliminado",
                                "El rol ha sido eliminado."));
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
        formulario = new Rol();
    }

    // ================= GETTERS / SETTERS =================

    public List<Rol> getLista() {
        return lista;
    }

    public void setLista(List<Rol> lista) {
        this.lista = lista;
    }

    public Rol getFormulario() {
        return formulario;
    }

    public void setFormulario(Rol formulario) {
        this.formulario = formulario;
    }

    public Rol getRolSeleccionado() {
        return rolSeleccionado;
    }

    public void setRolSeleccionado(Rol rolSeleccionado) {
        this.rolSeleccionado = rolSeleccionado;
    }

    public Long getIdRol() {
        return idRol;
    }

    public void setIdRol(Long idRol) {
        this.idRol = idRol;
    }
}
