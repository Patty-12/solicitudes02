package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.senadi.solicitud02.controlador.SolicitudControlador;
import com.senadi.solicitud02.controlador.UsuarioControlador;
import com.senadi.solicitud02.controlador.AccesoUsuarioControlador;
import com.senadi.solicitud02.controlador.AplicacionControlador;
import com.senadi.solicitud02.controlador.PermisoAplicacionControlador;
import com.senadi.solicitud02.controlador.impl.SolicitudControladorImpl;
import com.senadi.solicitud02.controlador.impl.UsuarioControladorImpl;
import com.senadi.solicitud02.controlador.impl.AccesoUsuarioControladorImpl;
import com.senadi.solicitud02.controlador.impl.AplicacionControladorImpl;
import com.senadi.solicitud02.controlador.impl.PermisoAplicacionControladorImpl;
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
    private Solicitud formulario;
    private Solicitud solicitudSeleccionada;

    // Filtro
    private String estadoFiltro = "TODAS";

    // Para edición
    private Long idSolicitud;

    // Datos del Director (Jefe inmediato)
    private String nombreJefeBusqueda;      // lo dejamos por compatibilidad (no se usa en la vista nueva)
    private Usuario jefe;                   // director seleccionado (objeto)

    // NUEVO: soporte para combo/autocomplete de directores
    private List<Usuario> directores;       // cache local de directores
    private Long idDirectorSeleccionado;    // id del director elegido en el autocomplete

    // Aplicaciones / permisos
    private List<Aplicacion> aplicaciones;
    private Map<Long, Boolean> permisosSeleccionados = new HashMap<>();

    @PostConstruct
    public void init() {
        formulario = new Solicitud();
        cargarAplicacionesYPermisos();

        // Carga inicial de directores (los que tienen cargo "Director")
        directores = usuarioCtrl.listarDirectores();

        aplicarFiltro();
    }

    // ==========================
    //   UTILIDADES
    // ==========================

    private Usuario obtenerUsuarioLogueado() {
        FacesContext fc = FacesContext.getCurrentInstance();
        LoginBean lb = fc.getApplication()
                         .evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
        return (lb != null) ? lb.getUsuario() : null;
    }

    /** Carga todas las aplicaciones y sus permisos, inicializando el mapa en false. */
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

    /** Marca en el mapa los permisos que ya tiene la solicitud en BD. */
    private void marcarPermisosDeSolicitud(Solicitud s) {
        cargarAplicacionesYPermisos();

        if (s == null || s.getId() == null) {
            return;
        }

        List<AccesoUsuario> accesos = accesoCtrl.listarPorSolicitud(s.getId());
        if (accesos == null) {
            return;
        }

        for (AccesoUsuario au : accesos) {
            if (au.getPermiso() != null && au.getPermiso().getId() != null) {
                permisosSeleccionados.put(au.getPermiso().getId(), true);
            }
        }
    }

    /** Elimina de BD todos los accesos asociados a la solicitud antes de re-grabarlos. */
    private void limpiarAccesosDeSolicitud(Solicitud s) {
        if (s == null || s.getId() == null) {
            return;
        }

        List<AccesoUsuario> accesos = accesoCtrl.listarPorSolicitud(s.getId());
        if (accesos != null) {
            for (AccesoUsuario au : accesos) {
                try {
                    accesoCtrl.eliminar(au.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (s.getAccesos() != null) {
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

            if ("TODAS".equals(estadoFiltro)) {
                lista = solCtrl.buscarPorUsuario(u.getId());
                if (lista != null) {
                    lista.removeIf(s -> "ANULADA".equalsIgnoreCase(s.getEstado()));
                }
            } else {
                lista = solCtrl.buscarPorUsuarioYEstado(u.getId(), estadoFiltro);
            }

            if (lista != null) {
                lista.sort(
                    Comparator.comparing(Solicitud::getFechaCreacion).reversed()
                );
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
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null && fc.isPostback()) {
            return;
        }

        formulario = new Solicitud();

        Usuario usuarioLogueado = obtenerUsuarioLogueado();
        formulario.setUsuario(usuarioLogueado != null ? usuarioLogueado : new Usuario());

        formulario.setEstado("CREADA");
        formulario.setFechaCreacion(LocalDateTime.now());

        nombreJefeBusqueda = null;
        jefe = null;
        idDirectorSeleccionado = null;

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

        if (!"CREADA".equalsIgnoreCase(s.getEstado())) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN,
                                 "Edición no permitida",
                                 "Sólo puede editar solicitudes en estado CREADA."));
            return;
        }

        formulario = s;

        // Director (jefe inmediato) de la solicitud
        jefe = formulario.getJefeAutoriza();
        if (jefe != null) {
            idDirectorSeleccionado = jefe.getId();
        } else {
            idDirectorSeleccionado = null;
        }

        marcarPermisosDeSolicitud(formulario);
    }

    // ==========================
    //   DIRECTOR (JEFE)
    // ==========================

    /**
     * Método antiguo de búsqueda por nombre (ya casi no se usa en la vista,
     * pero se mantiene por compatibilidad).
     */
    public void buscarDirectorPorNombre() {
        if (nombreJefeBusqueda == null || nombreJefeBusqueda.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN,
                                 "Búsqueda vacía",
                                 "Ingrese al menos un nombre para buscar al Director."));
            return;
        }

        String criterio = nombreJefeBusqueda.trim();

        List<Usuario> encontrados = usuarioCtrl.buscarPorNombre(criterio);

        if (encontrados == null || encontrados.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN,
                                 "Director no encontrado",
                                 "No se encontraron usuarios con ese nombre."));
            jefe = null;
            idDirectorSeleccionado = null;
            return;
        }

        Usuario candidatoDirector = null;
        for (Usuario u : encontrados) {
            if (u.getCargo() != null &&
                u.getCargo().toLowerCase().contains("director")) {
                candidatoDirector = u;
                break;
            }
        }

        if (candidatoDirector == null) {
            candidatoDirector = encontrados.get(0);
        }

        jefe = candidatoDirector;
        idDirectorSeleccionado = jefe.getId();

        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO,
                             "Director seleccionado",
                             jefe.getNombre() + " " + jefe.getApellido()));
    }

    // ==========================
    //   AUTOCOMPLETE DIRECTORES
    // ==========================

    /**
     * Devuelve la lista de directores que coinciden con el texto digitado.
     * Se usa en el p:autoComplete del formulario.
     */
    public List<Usuario> completarDirectores(String query) {
        String patron = (query != null) ? query.trim().toLowerCase() : "";
        if (patron.isEmpty()) {
            // Si no escribe nada, se podría mostrar toda la lista (o vacía según prefieras)
            return directores != null ? directores : Collections.emptyList();
        }
        return usuarioCtrl.buscarDirectoresPorNombreLike(patron);
    }

    // ==========================
    //   GUARDAR / ANULAR
    // ==========================

    public String guardar() {
        try {
            Usuario usuarioLogueado = obtenerUsuarioLogueado();
            if (usuarioLogueado != null && formulario.getUsuario() == null) {
                formulario.setUsuario(usuarioLogueado);
            }

            // Resolver director (jefe) a partir del id seleccionado, si aún no está cargado
            if ((jefe == null || jefe.getId() == null) && idDirectorSeleccionado != null) {
                jefe = usuarioCtrl.buscarPorId(idDirectorSeleccionado);
            }

            // Validar que exista director seleccionado
            if (jefe == null || jefe.getId() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                                     "Director requerido",
                                     "Debe seleccionar un Director que autoriza la solicitud."));
                return null;
            }
            formulario.setJefeAutoriza(jefe);

            // Validar que exista al menos un permiso seleccionado
            boolean hayPermisoSeleccionado = permisosSeleccionados.values().stream()
                    .anyMatch(Boolean.TRUE::equals);

            if (!hayPermisoSeleccionado) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                                     "Permisos requeridos",
                                     "Debe seleccionar al menos un permiso de acceso."));
                return null;
            }

            boolean nueva = (formulario.getId() == null);

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
                limpiarAccesosDeSolicitud(formulario);
                solCtrl.actualizar(formulario);
            }

            // Guardar accesos seleccionados
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

            return "/Solicitud/index?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                 "Error al guardar solicitud", e.getMessage()));
            return null;
        }
    }

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
                                     "La solicitud ha sido anulada."));
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

    public String getNombreJefeBusqueda() {
        return nombreJefeBusqueda;
    }

    public void setNombreJefeBusqueda(String nombreJefeBusqueda) {
        this.nombreJefeBusqueda = nombreJefeBusqueda;
    }

    public Usuario getJefe() {
        return jefe;
    }

    public void setJefe(Usuario jefe) {
        this.jefe = jefe;
        if (jefe != null) {
            this.idDirectorSeleccionado = jefe.getId();
        }
    }

    public List<Aplicacion> getAplicaciones() {
        return aplicaciones;
    }

    public Map<Long, Boolean> getPermisosSeleccionados() {
        return permisosSeleccionados;
    }

    public void setPermisosSeleccionados(Map<Long, Boolean> permisosSeleccionados) {
        if (permisosSeleccionados == null) {
            this.permisosSeleccionados = new HashMap<>();
        } else {
            this.permisosSeleccionados.clear();
            this.permisosSeleccionados.putAll(permisosSeleccionados);
        }
    }

    public List<Usuario> getDirectores() {
        return directores;
    }

    public Long getIdDirectorSeleccionado() {
        return idDirectorSeleccionado;
    }

    public void setIdDirectorSeleccionado(Long idDirectorSeleccionado) {
        this.idDirectorSeleccionado = idDirectorSeleccionado;
        // Cada vez que cambia el id, actualizamos el objeto jefe
        if (idDirectorSeleccionado != null) {
            this.jefe = usuarioCtrl.buscarPorId(idDirectorSeleccionado);
        } else {
            this.jefe = null;
        }
    }
}
