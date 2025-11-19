package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.senadi.solicitud02.controlador.UsuarioRolControlador;
import com.senadi.solicitud02.controlador.UsuarioControlador;
import com.senadi.solicitud02.controlador.RolControlador;

import com.senadi.solicitud02.controlador.impl.UsuarioRolControladorImpl;
import com.senadi.solicitud02.controlador.impl.UsuarioControladorImpl;
import com.senadi.solicitud02.controlador.impl.RolControladorImpl;

import com.senadi.solicitud02.modelo.entidades.UsuarioRol;
import com.senadi.solicitud02.modelo.entidades.Usuario;
import com.senadi.solicitud02.modelo.entidades.Rol;

@ManagedBean(name = "usuarioRolBean")
@ViewScoped
public class UsuarioRolBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private UsuarioRolControlador usuarioRolCtrl = new UsuarioRolControladorImpl();
    private UsuarioControlador usuarioCtrl = new UsuarioControladorImpl();
    private RolControlador rolCtrl = new RolControladorImpl();

    // Listado principal
    private List<UsuarioRol> lista;

    // Formulario (crear/editar)
    private UsuarioRol formulario;

    // Registro seleccionado para eliminar
    private UsuarioRol usuarioRolSeleccionado;

    // Listas de apoyo (combos)
    private List<Usuario> usuarios;
    private List<Rol> roles;

    // IDs seleccionados desde la vista
    private Long idUsuarioSeleccionado;
    private Long idRolSeleccionado;

    // Para editar por ?id=XX
    private Long idUsuarioRol;

    @PostConstruct
    public void init() {
        listar();
        formulario = new UsuarioRol();
        cargarUsuariosYRoles();
    }

    private void cargarUsuariosYRoles() {
        usuarios = usuarioCtrl.listarTodos();
        roles = rolCtrl.listarTodos();
    }

    public void listar() {
        lista = usuarioRolCtrl.listarTodos();
    }

    /**
     * Cargar un UsuarioRol específico para edición (usado por <f:viewAction>).
     */
    public String cargarUsuarioRol() {
        if (idUsuarioRol != null) {
            UsuarioRol ur = usuarioRolCtrl.buscarPorId(idUsuarioRol);
            if (ur != null) {
                usuarioRolSeleccionado = ur;
                formulario = ur;

                if (ur.getUsuario() != null) {
                    idUsuarioSeleccionado = ur.getUsuario().getId();
                }
                if (ur.getRol() != null) {
                    idRolSeleccionado = ur.getRol().getId();
                }
                cargarUsuariosYRoles();
            } else {
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Asignación no encontrada",
                                "El registro solicitado no existe."));
            }
        }
        return null;
    }

    /**
     * Crear o actualizar una asignación Usuario–Rol.
     */
    public String guardar() {
        try {
            // Resolver usuario
            if (idUsuarioSeleccionado != null) {
                Usuario u = usuarioCtrl.buscarPorId(idUsuarioSeleccionado);
                formulario.setUsuario(u);
            } else {
                formulario.setUsuario(null);
            }

            // Resolver rol
            if (idRolSeleccionado != null) {
                Rol r = rolCtrl.buscarPorId(idRolSeleccionado);
                formulario.setRol(r);
            } else {
                formulario.setRol(null);
            }

            boolean esNuevo = (formulario.getId() == null);

            if (esNuevo) {
                if (formulario.getFechaAsignacion() == null) {
                    formulario.setFechaAsignacion(LocalDateTime.now());
                }
                usuarioRolCtrl.crear(formulario);
            } else {
                usuarioRolCtrl.actualizar(formulario);
            }

            listar();
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito",
                            "Asignación Usuario–Rol guardada correctamente."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al guardar",
                            e.getMessage()));
        }
        return null; // por ahora se queda en la misma vista
    }

    /**
     * Eliminar la asignación seleccionada.
     */
    public String eliminar() {
        if (usuarioRolSeleccionado != null && usuarioRolSeleccionado.getId() != null) {
            try {
                usuarioRolCtrl.eliminar(usuarioRolSeleccionado.getId());
                listar();
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Eliminado",
                                "La asignación ha sido eliminada."));
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
        formulario = new UsuarioRol();
        idUsuarioSeleccionado = null;
        idRolSeleccionado = null;
        cargarUsuariosYRoles();
    }

    // ================= GETTERS / SETTERS =================

    public List<UsuarioRol> getLista() {
        return lista;
    }

    public void setLista(List<UsuarioRol> lista) {
        this.lista = lista;
    }

    public UsuarioRol getFormulario() {
        return formulario;
    }

    public void setFormulario(UsuarioRol formulario) {
        this.formulario = formulario;
    }

    public UsuarioRol getUsuarioRolSeleccionado() {
        return usuarioRolSeleccionado;
    }

    public void setUsuarioRolSeleccionado(UsuarioRol usuarioRolSeleccionado) {
        this.usuarioRolSeleccionado = usuarioRolSeleccionado;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public List<Rol> getRoles() {
        return roles;
    }

    public void setRoles(List<Rol> roles) {
        this.roles = roles;
    }

    public Long getIdUsuarioSeleccionado() {
        return idUsuarioSeleccionado;
    }

    public void setIdUsuarioSeleccionado(Long idUsuarioSeleccionado) {
        this.idUsuarioSeleccionado = idUsuarioSeleccionado;
    }

    public Long getIdRolSeleccionado() {
        return idRolSeleccionado;
    }

    public void setIdRolSeleccionado(Long idRolSeleccionado) {
        this.idRolSeleccionado = idRolSeleccionado;
    }

    public Long getIdUsuarioRol() {
        return idUsuarioRol;
    }

    public void setIdUsuarioRol(Long idUsuarioRol) {
        this.idUsuarioRol = idUsuarioRol;
    }
}
