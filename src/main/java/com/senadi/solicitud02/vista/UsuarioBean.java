package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import com.senadi.solicitud02.controlador.UsuarioControlador;
import com.senadi.solicitud02.controlador.impl.UsuarioControladorImpl;
import com.senadi.solicitud02.modelo.entidades.Usuario;

@ManagedBean(name = "usuarioBean")
@ViewScoped
public class UsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private UsuarioControlador usuarioCtrl = new UsuarioControladorImpl();

    // Lista completa desde BD
    private List<Usuario> lista;
    // Lista filtrada que se muestra en la tabla
    private List<Usuario> listaFiltrada;

    // Campo de filtro (texto libre)
    private String filtro;

    private Usuario formulario;
    private Usuario usuarioSeleccionado;

    // Para editar por ?id=XX (no se usa en la vista actual, pero se mantiene)
    private Long idUsuario;

    @PostConstruct
    public void init() {
        listar();
        formulario = new Usuario();
        // Al inicio, la lista filtrada es toda la lista
        if (lista != null) {
            listaFiltrada = new ArrayList<>(lista);
        } else {
            listaFiltrada = new ArrayList<>();
        }
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
    // Se mantiene por compatibilidad, aunque en la vista actual no se usa.
    public String guardar() {
        try {
            boolean esNuevo = (formulario.getId() == null);

            if (esNuevo) {
                usuarioCtrl.crear(formulario);
            } else {
                usuarioCtrl.actualizar(formulario);
            }

            listar();
            // Actualizar lista filtrada también
            aplicarFiltro();

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
    // Se mantiene por compatibilidad, aunque en la vista actual no se usa.
    public String eliminar() {
        if (usuarioSeleccionado != null && usuarioSeleccionado.getId() != null) {
            try {
                usuarioCtrl.eliminar(usuarioSeleccionado.getId());
                listar();
                aplicarFiltro();
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

    // ==========================
    // FILTRO RÁPIDO
    // ==========================

    /**
     * Versión sin parámetros, se puede invocar desde EL o internamente.
     */
    public void aplicarFiltro() {
        if (lista == null) {
            listaFiltrada = new ArrayList<>();
            return;
        }

        String texto = (filtro != null) ? filtro.trim().toLowerCase() : "";

        // Si no hay texto, se muestra todo
        if (texto.isEmpty()) {
            listaFiltrada = new ArrayList<>(lista);
            return;
        }

        List<Usuario> resultado = new ArrayList<>();
        for (Usuario u : lista) {
            if (u == null) continue;

            String nombre = (u.getNombre() != null) ? u.getNombre().toLowerCase() : "";
            String apellido = (u.getApellido() != null) ? u.getApellido().toLowerCase() : "";
            String correo = (u.getCorreo() != null) ? u.getCorreo().toLowerCase() : "";

            if (nombre.contains(texto)
                    || apellido.contains(texto)
                    || correo.contains(texto)) {
                resultado.add(u);
            }
        }

        listaFiltrada = resultado;
    }

    /**
     * Versión compatible con <p:ajax listener="...">.
     */
    public void aplicarFiltro(AjaxBehaviorEvent event) {
        aplicarFiltro();
    }

    // ====== GETTERS / SETTERS ======

    public List<Usuario> getLista() {
        return lista;
    }

    public void setLista(List<Usuario> lista) {
        this.lista = lista;
    }

    public List<Usuario> getListaFiltrada() {
        return listaFiltrada;
    }

    public void setListaFiltrada(List<Usuario> listaFiltrada) {
        this.listaFiltrada = listaFiltrada;
    }

    public String getFiltro() {
        return filtro;
    }

    public void setFiltro(String filtro) {
        this.filtro = filtro;
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
