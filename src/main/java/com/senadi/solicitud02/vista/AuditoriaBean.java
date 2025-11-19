package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.senadi.solicitud02.controlador.AuditoriaControlador;
import com.senadi.solicitud02.controlador.UsuarioControlador;

import com.senadi.solicitud02.controlador.impl.AuditoriaControladorImpl;
import com.senadi.solicitud02.controlador.impl.UsuarioControladorImpl;

import com.senadi.solicitud02.modelo.entidades.Auditoria;
import com.senadi.solicitud02.modelo.entidades.Usuario;

@ManagedBean(name = "auditoriaBean")
@ViewScoped
public class AuditoriaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private AuditoriaControlador auditoriaCtrl = new AuditoriaControladorImpl();
    private UsuarioControlador usuarioCtrl = new UsuarioControladorImpl();

    // Listado principal
    private List<Auditoria> lista;

    // Formulario (crear/editar)
    private Auditoria formulario;

    // Registro seleccionado para eliminar
    private Auditoria auditoriaSeleccionada;

    // Listado de usuarios para el combo
    private List<Usuario> usuarios;

    // ID de usuario seleccionado
    private Long idUsuarioSeleccionado;

    // Para editar por ?id=XX
    private Long idAuditoria;

    @PostConstruct
    public void init() {
        listar();
        formulario = new Auditoria();
        cargarUsuarios();
    }

    private void cargarUsuarios() {
        usuarios = usuarioCtrl.listarTodos();
    }

    public void listar() {
        lista = auditoriaCtrl.listarTodos();
    }

    /**
     * Cargar una auditoría específica para edición (usado por <f:viewAction>).
     */
    public String cargarAuditoria() {
        if (idAuditoria != null) {
            Auditoria a = auditoriaCtrl.buscarPorId(idAuditoria);
            if (a != null) {
                auditoriaSeleccionada = a;
                formulario = a;

                if (a.getUsuario() != null) {
                    idUsuarioSeleccionado = a.getUsuario().getId();
                }

                cargarUsuarios();
            } else {
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Registro no encontrado",
                                "La auditoría solicitada no existe."));
            }
        }
        return null;
    }

    /**
     * Crear o actualizar una entrada de auditoría.
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

            boolean esNuevo = (formulario.getId() == null);

            if (esNuevo) {
                if (formulario.getFechaEvento() == null) {
                    formulario.setFechaEvento(LocalDateTime.now());
                }
                auditoriaCtrl.crear(formulario);
            } else {
                auditoriaCtrl.actualizar(formulario);
            }

            listar();
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito",
                            "Registro de auditoría guardado correctamente."));
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
     * Eliminar el registro seleccionado.
     */
    public String eliminar() {
        if (auditoriaSeleccionada != null && auditoriaSeleccionada.getId() != null) {
            try {
                auditoriaCtrl.eliminar(auditoriaSeleccionada.getId());
                listar();
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Eliminado",
                                "El registro de auditoría ha sido eliminado."));
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
        formulario = new Auditoria();
        idUsuarioSeleccionado = null;
        cargarUsuarios();
    }

    // ================= GETTERS / SETTERS =================

    public List<Auditoria> getLista() {
        return lista;
    }

    public void setLista(List<Auditoria> lista) {
        this.lista = lista;
    }

    public Auditoria getFormulario() {
        return formulario;
    }

    public void setFormulario(Auditoria formulario) {
        this.formulario = formulario;
    }

    public Auditoria getAuditoriaSeleccionada() {
        return auditoriaSeleccionada;
    }

    public void setAuditoriaSeleccionada(Auditoria auditoriaSeleccionada) {
        this.auditoriaSeleccionada = auditoriaSeleccionada;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public Long getIdUsuarioSeleccionado() {
        return idUsuarioSeleccionado;
    }

    public void setIdUsuarioSeleccionado(Long idUsuarioSeleccionado) {
        this.idUsuarioSeleccionado = idUsuarioSeleccionado;
    }

    public Long getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Long idAuditoria) {
        this.idAuditoria = idAuditoria;
    }
}
