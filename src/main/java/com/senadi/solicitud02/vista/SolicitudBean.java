package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.senadi.solicitud02.controlador.*;
import com.senadi.solicitud02.controlador.impl.*;
import com.senadi.solicitud02.modelo.entidades.*;

/**
 * Bean de gestión de Solicitudes.
 */
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
    private Solicitud formulario;
    private Solicitud solicitudSeleccionada;

    // Filtro
    private String estadoFiltro = "TODAS";

    // Para edición
    private Long idSolicitud;

    // Datos de Director (jefe)
    private String correoJefe;
    private Usuario jefe;

    // Aplicaciones / permisos
    private List<Aplicacion> aplicaciones;
    private Map<Long, Boolean> permisosSeleccionados = new HashMap<>();

    @PostConstruct
    public void init() {
        formulario = new Solicitud();
        cargarAplicacionesYPermisos();
        aplicarFiltro(); // carga inicial según usuario logueado
    }

    // ==========================
    //   UTILIDADES
    // ==========================

    private Usuario obtenerUsuarioLogueado() {
        FacesContext fc = FacesContext.getCurrentInstance();
        LoginBean lb = fc.getApplication()
                         .evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
        if (lb != null) {
            return lb.getUsuario();
        }
        return null;
    }

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

    private void marcarPermisosDeSolicitud(Solicitud s) {
        cargarAplicacionesYPermisos();
        if (s != null && s.getAccesos() != null) {
            for (AccesoUsuario au : s.getAccesos()) {
                if (au.getPermiso() != null) {
                    permisosSeleccionados.put(au.getPermiso().getId(), true);
                }
            }
        }
    }

    private void limpiarAccesosDeSolicitud(Solicitud s) {
        if (s != null && s.getAccesos() != null) {
            for (AccesoUsuario au : new ArrayList<>(s.getAccesos())) {
                try {
                    accesoCtrl.eliminar(au.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            s.getAccesos().clear();
        }
    }

    // ==========================
    //   LISTADO / FILTRO
    // ==========================

    public void aplicarFiltro() {
        try {
            Usuario u = obtenerUsuarioLogueado();
            if (u == null) {
                lista = Collections.emptyList();
                return;
            }

            if (estadoFiltro == null || "TODAS".equals(estadoFiltro)) {
                // Todas las solicitudes del usuario, pero ocultando ANULADAS en la vista normal
                lista = solCtrl.buscarPorUsuario(u.getId());
                if (lista != null) {
                    lista.removeIf(s -> "ANULADA".equalsIgnoreCase(s.getEstado()));
                }
            } else {
                // Filtro específico por estado (si el usuario selecciona ANULADA, también las verá)
                lista = solCtrl.buscarPorUsuarioYEstado(u.getId(), estadoFiltro);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al aplicar filtro", e.getMessage()));
        }
    }

    public void limpiarFiltro() {
        estadoFiltro = "TODAS";
        aplicarFiltro();
    }

    // ==========================
    //   NUEVA SOLICITUD
    // ==========================

    public void prepararNuevo() {
        formulario = new Solicitud();

        Usuario usuarioLogueado = obtenerUsuarioLogueado();
        if (usuarioLogueado != null) {
            formulario.setUsuario(usuarioLogueado);
        } else {
            formulario.setUsuario(new Usuario());
        }

        formulario.setEstado("CREADA");
        formulario.setFechaCreacion(LocalDateTime.now());

        correoJefe = null;
        jefe = null;

        cargarAplicacionesYPermisos();
    }

    // ==========================
    //   CARGAR PARA EDICIÓN
    // ==========================

    public void cargarSolicitud() {
        if (idSolicitud == null) {
            return;
        }

        Solicitud s = solCtrl.buscarPorId(idSolicitud);
        if (s == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Solicitud no encontrada", "ID: " + idSolicitud));
            return;
        }

        // Regla: sólo se permite editar solicitudes en estado CREADA
        if (!"CREADA".equalsIgnoreCase(s.getEstado())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Edición no permitida",
                            "Sólo puede editar solicitudes en estado CREADA."));
            return;
        }

        formulario = s;
        marcarPermisosDeSolicitud(formulario);
    }

    // ==========================
    //   DIRECTOR (JEFE)
    // ==========================

    public String llenarDatosJefe() {
        if (correoJefe != null && !correoJefe.isEmpty()) {
            jefe = usuarioCtrl.buscarPorCorreoYCargo(correoJefe, "Director");
            if (jefe == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Director no encontrado",
                                "Verifica el correo y que tenga cargo 'Director'"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Director encontrado",
                                jefe.getNombre() + " " + jefe.getApellido()));
            }
        }
        return null;
    }

    // ==========================
    //   GUARDAR / ELIMINAR (ANULAR)
    // ==========================

    public String guardar() {
        try {
            Usuario usuarioLogueado = obtenerUsuarioLogueado();
            if (usuarioLogueado != null && formulario.getUsuario() == null) {
                formulario.setUsuario(usuarioLogueado);
            }

            boolean nueva = (formulario.getId() == null);

            // Regla de negocio: sólo se guarda/actualiza si está CREADA o es nueva
            if (!nueva && !"CREADA".equalsIgnoreCase(formulario.getEstado())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "No permitido",
                                "Sólo puede modificar solicitudes en estado CREADA."));
                return null;
            }

            if (nueva) {
                formulario.setEstado("CREADA");
                formulario.setFechaCreacion(LocalDateTime.now());
                solCtrl.crear(formulario);
            } else {
                // limpiar accesos previos para re-generarlos
                limpiarAccesosDeSolicitud(formulario);
                solCtrl.actualizar(formulario);
            }

            // Crear accesos según los permisos marcados
            for (Map.Entry<Long, Boolean> entry : permisosSeleccionados.entrySet()) {
                if (Boolean.TRUE.equals(entry.getValue())) {
                    PermisoAplicacion permiso = permisoCtrl.buscarPorId(entry.getKey());
                    if (permiso != null) {
                        AccesoUsuario au = new AccesoUsuario();
                        au.setSolicitud(formulario);
                        au.setPermiso(permiso);
                        au.setFechaCarga(LocalDateTime.now());
                        accesoCtrl.crear(au);
                    }
                }
            }

            aplicarFiltro();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Solicitud guardada",
                            "La solicitud se ha guardado correctamente."));

            // Volver al listado
            return "/Solicitud/index?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al guardar solicitud", e.getMessage()));
            return null;
        }
    }

    /**
     * "Eliminar" ahora significa ANULAR la solicitud (soft delete).
     * No se borra de la base, sólo cambia el estado a ANULADA y se oculta de la vista normal.
     */
    public void eliminar() {
        if (solicitudSeleccionada != null && solicitudSeleccionada.getId() != null) {
            try {
                Solicitud s = solCtrl.buscarPorId(solicitudSeleccionada.getId());
                if (s == null) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Solicitud no encontrada",
                                    "ID: " + solicitudSeleccionada.getId()));
                    return;
                }

                // Regla: sólo se puede anular cuando está CREADA
                if (!"CREADA".equalsIgnoreCase(s.getEstado())) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "No permitido",
                                    "Sólo puede anular solicitudes en estado CREADA."));
                    return;
                }

                s.setEstado("ANULADA");
                solCtrl.actualizar(s);

                aplicarFiltro();

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Solicitud anulada",
                                "La solicitud ha sido anulada y ya no aparecerá en su listado principal."));
            } catch (Exception e) {
                e.printStackTrace();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error al anular solicitud", e.getMessage()));
            }
        }
    }

    // ==========================
    //   GETTERS / SETTERS
    // ==========================

    public List<Solicitud> getLista() {
        return lista;
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

    public String getEstadoFiltro() {
        return estadoFiltro;
    }

    public void setEstadoFiltro(String estadoFiltro) {
        this.estadoFiltro = estadoFiltro;
    }

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
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

    public Map<Long, Boolean> getPermisosSeleccionados() {
        return permisosSeleccionados;
    }
}
