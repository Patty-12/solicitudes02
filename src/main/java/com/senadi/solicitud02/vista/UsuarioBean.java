package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.senadi.solicitud02.controlador.UsuarioControlador;
import com.senadi.solicitud02.controlador.impl.UsuarioControladorImpl;
import com.senadi.solicitud02.modelo.entidades.Usuario;

@ManagedBean(name = "usuarioBean")
@ViewScoped
public class UsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private UsuarioControlador usuarioCtrl = new UsuarioControladorImpl();

    private List<Usuario> lista;
    private Usuario formulario;
    private Usuario usuarioSeleccionado;

    // Para editar por ?id=XX
    private Long idUsuario;

    @PostConstruct
    public void init() {
        listar();
        formulario = new Usuario();
    }

    public void listar() {
        lista = usuarioCtrl.listarTodos();
    }

    // ====== Cargar usuario para edición (llamado desde <f:viewAction>) ======
    public String cargarUsuario() {
        if (idUsuario != null) {
            Usuario u = usuarioCtrl.buscarPorId(idUsuario);
            if (u != null) {
                usuarioSeleccionado = u;
                formulario = u;
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Usuario no encontrado",
                                "El registro solicitado no existe."));
            }
        }
        return null; // sin navegación
    }

    // ====== GUARDAR (crear / actualizar) ======
    // Firma correcta para action="#{usuarioBean.guardar}"
    public String guardar() {
        try {
            boolean esNuevo = (formulario.getId() == null);

            if (esNuevo) {
                usuarioCtrl.crear(formulario);
            } else {
                usuarioCtrl.actualizar(formulario);
            }

            listar();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito",
                            "Usuario guardado correctamente."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al guardar",
                            e.getMessage()));
        }
        return null; // se queda en la misma vista
    }

    // ====== ELIMINAR ======
    // Firma correcta para action="#{usuarioBean.eliminar}"
    public String eliminar() {
        if (usuarioSeleccionado != null && usuarioSeleccionado.getId() != null) {
            try {
                usuarioCtrl.eliminar(usuarioSeleccionado.getId());
                listar();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Eliminado",
                                "El usuario ha sido eliminado."));
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
        formulario = new Usuario();
    }

    // ====== GETTERS / SETTERS ======

    public List<Usuario> getLista() {
        return lista;
    }

    public void setLista(List<Usuario> lista) {
        this.lista = lista;
    }

    public Usuario getFormulario() {
        return formulario;
    }

    public void setFormulario(Usuario formulario) {
        this.formulario = formulario;
    }

    public Usuario getUsuarioSeleccionado() {
        return usuarioSeleccionado;
    }

    public void setUsuarioSeleccionado(Usuario usuarioSeleccionado) {
        this.usuarioSeleccionado = usuarioSeleccionado;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
}
