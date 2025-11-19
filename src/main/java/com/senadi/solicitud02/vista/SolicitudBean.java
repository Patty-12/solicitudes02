package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;

import com.senadi.solicitud02.controlador.AplicacionControlador;
import com.senadi.solicitud02.controlador.PermisoAplicacionControlador;
import com.senadi.solicitud02.controlador.SolicitudControlador;
import com.senadi.solicitud02.controlador.UsuarioControlador;
import com.senadi.solicitud02.controlador.AccesoUsuarioControlador;

import com.senadi.solicitud02.controlador.impl.AplicacionControladorImpl;
import com.senadi.solicitud02.controlador.impl.PermisoAplicacionControladorImpl;
import com.senadi.solicitud02.controlador.impl.SolicitudControladorImpl;
import com.senadi.solicitud02.controlador.impl.UsuarioControladorImpl;
import com.senadi.solicitud02.controlador.impl.AccesoUsuarioControladorImpl;

import com.senadi.solicitud02.modelo.entidades.Solicitud;
import com.senadi.solicitud02.modelo.entidades.Usuario;
import com.senadi.solicitud02.modelo.entidades.Aplicacion;
import com.senadi.solicitud02.modelo.entidades.PermisoAplicacion;
import com.senadi.solicitud02.modelo.entidades.AccesoUsuario;

@ManagedBean(name = "solicitudBean")
@ViewScoped
public class SolicitudBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // Controladores
    private SolicitudControlador solCtrl = new SolicitudControladorImpl();
    private UsuarioControlador usuarioCtrl = new UsuarioControladorImpl();
    private AccesoUsuarioControlador accesoCtrl = new AccesoUsuarioControladorImpl();
    private AplicacionControlador aplicacionCtrl = new AplicacionControladorImpl();
    private PermisoAplicacionControlador permisoCtrl = new PermisoAplicacionControladorImpl();

    // Datos de la vista
    private List<Solicitud> lista;
    private Solicitud formulario;              // solicitud en edición/creación
    private Solicitud solicitudSeleccionada;   // para eliminar / selección en tabla

    // Datos del jefe/director que autoriza
    private String correoJefe;
    private Usuario jefe;

    // Aplicaciones y permisos
    private List<Aplicacion> aplicaciones;
    private Map<Long, Boolean> permisosSeleccionados = new HashMap<>();

    // ID que viene por parámetro para editar (?id=XX)
    private Long idSolicitud;

    // ================= CICLO DE VIDA =================

    @PostConstruct
    public void init() {
        listar();
        formulario = new Solicitud();
        cargarAplicacionesYPermisos();
    }

    // ================= LÓGICA INTERNA =================

    private void cargarAplicacionesYPermisos() {
        aplicaciones = aplicacionCtrl.listarTodos();
        permisosSeleccionados.clear();

        if (aplicaciones != null) {
            for (Aplicacion app : aplicaciones) {
                if (app.getPermisos() != null) {
                    for (PermisoAplicacion p : app.getPermisos()) {
                        permisosSeleccionados.put(p.getId(), false);
                    }
                }
            }
        }
    }

    public void listar() {
        lista = solCtrl.listarTodos();
    }

    /**
     * Prepara el bean para una nueva solicitud.
     * Firma compatible con <f:event type="preRenderView" ...>
     */
    public void prepararNuevo(ComponentSystemEvent event) {
        formulario = new Solicitud();

        // Recuperar usuario logueado de sesión
        Usuario usuarioLogueado = (Usuario) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("usuarioLogueado");

        if (usuarioLogueado != null) {
            formulario.setUsuario(usuarioLogueado);
        } else {
            formulario.setUsuario(new Usuario());
        }

        // Estado y fecha por defecto
        formulario.setEstado("CREADA");
        formulario.setFechaCreacion(LocalDateTime.now());

        correoJefe = null;
        jefe = null;
        cargarAplicacionesYPermisos();
    }

    /**
     * Prepara los datos del formulario para edición
     * a partir de la solicitudSeleccionada.
     */
    public void prepararEdicion() {
        if (solicitudSeleccionada != null) {
            formulario = solicitudSeleccionada;

            // Datos del jefe (si aplica)
            jefe = null;
            correoJefe = null;

            if (formulario.getUsuario() != null &&
                "Director".equals(formulario.getUsuario().getCargo())) {
                jefe = formulario.getUsuario();
                correoJefe = jefe.getCorreo();
            }

            // Cargar aplicaciones y marcar permisos de la solicitud
            cargarAplicacionesYPermisos();
            if (formulario.getAccesos() != null) {
                for (AccesoUsuario au : formulario.getAccesos()) {
                    if (au.getPermiso() != null && au.getPermiso().getId() != null) {
                        permisosSeleccionados.put(au.getPermiso().getId(), true);
                    }
                }
            }
        }
    }

    /**
     * Se invoca desde edit.xhtml mediante <f:event>.
     * Firma compatible con <f:event type="preRenderView" ...>
     */
    public void cargarSolicitud(ComponentSystemEvent event) {
        if (idSolicitud != null) {
            Solicitud s = solCtrl.buscarPorId(idSolicitud);
            if (s != null) {
                this.solicitudSeleccionada = s;
                prepararEdicion();
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                         "Solicitud no encontrada",
                                         "El registro solicitado no existe."));
            }
        }
    }

    /**
     * Busca al jefe/director por correo y setea el objeto jefe.
     * Firma compatible con action="#{...}" → retorna String.
     */
    public String llenarDatosJefe() {
        if (correoJefe != null && !correoJefe.isEmpty()) {
            jefe = usuarioCtrl.buscarPorCorreoYCargo(correoJefe, "Director");
            if (jefe == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                         "Jefe no encontrado",
                                         "Verifique el correo institucional del Director."));
            }
        }
        return null; // sin navegación
    }

    /**
     * Crea o actualiza la solicitud y registra accesos según permisosSeleccionados.
     * Firma compatible con action="#{...}" → retorna String.
     */
    public String guardar() {
        try {
            boolean esNueva = (formulario.getId() == null);

            if (esNueva) {
                formulario.setEstado("CREADA");
                formulario.setFechaCreacion(LocalDateTime.now());
                solCtrl.crear(formulario);
            } else {
                solCtrl.actualizar(formulario);
                // Dependiendo de tu lógica, aquí podrías limpiar accesos previos
            }

            // Crear accesos por cada permiso marcado
            for (Map.Entry<Long, Boolean> entry : permisosSeleccionados.entrySet()) {
                if (Boolean.TRUE.equals(entry.getValue())) {
                    PermisoAplicacion permiso = permisoCtrl.buscarPorId(entry.getKey());
                    if (permiso != null) {
                        AccesoUsuario acceso = new AccesoUsuario();
                        acceso.setSolicitud(formulario);
                        acceso.setPermiso(permiso);
                        accesoCtrl.crear(acceso);
                    }
                }
            }

            listar();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                                     "Éxito",
                                     "Solicitud guardada correctamente."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                     "Error al guardar",
                                     e.getMessage()));
        }
        return null; // permanece en la misma vista
    }

    /**
     * Elimina la solicitud seleccionada.
     * Firma compatible con action="#{...}" → retorna String.
     */
    public String eliminar() {
        if (solicitudSeleccionada != null && solicitudSeleccionada.getId() != null) {
            try {
                solCtrl.eliminar(solicitudSeleccionada.getId());
                listar();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                         "Eliminada",
                                         "La solicitud ha sido eliminada."));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                         "Error al eliminar",
                                         e.getMessage()));
            }
        }
        return null;
    }

    /**
     * Limpia el formulario y vuelve a estado inicial.
     */
    public void cancelar() {
        formulario = new Solicitud();
        correoJefe = null;
        jefe = null;
        cargarAplicacionesYPermisos();
    }

    // ================= GETTERS / SETTERS =================

    public List<Solicitud> getLista() {
        return lista;
    }

    public void setLista(List<Solicitud> lista) {
        this.lista = lista;
    }

    public Solicitud getFormulario() {
        return formulario;
    }

    public void setFormulario(Solicitud formulario) {
        this.formulario = formulario;
    }

    public Solicitud getSolicitudSeleccionada() {
        return solicitudSeleccionada;
    }

    public void setSolicitudSeleccionada(Solicitud solicitudSeleccionada) {
        this.solicitudSeleccionada = solicitudSeleccionada;
    }

    public String getCorreoJefe() {
        return correoJefe;
    }

    public void setCorreoJefe(String correoJefe) {
        this.correoJefe = correoJefe;
    }

    public Usuario getJefe() {
        return jefe;
    }

    public void setJefe(Usuario jefe) {
        this.jefe = jefe;
    }

    public List<Aplicacion> getAplicaciones() {
        return aplicaciones;
    }

    public void setAplicaciones(List<Aplicacion> aplicaciones) {
        this.aplicaciones = aplicaciones;
    }

    public Map<Long, Boolean> getPermisosSeleccionados() {
        return permisosSeleccionados;
    }

    public void setPermisosSeleccionados(Map<Long, Boolean> permisosSeleccionados) {
        this.permisosSeleccionados = permisosSeleccionados;
    }

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }
}
