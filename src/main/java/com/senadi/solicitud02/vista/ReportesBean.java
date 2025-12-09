package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.senadi.solicitud02.controlador.SolicitudControlador;
import com.senadi.solicitud02.controlador.impl.SolicitudControladorImpl;
import com.senadi.solicitud02.modelo.entidades.Solicitud;
import com.senadi.solicitud02.modelo.entidades.Usuario;

@ManagedBean(name = "reportesBean")
@ViewScoped
public class ReportesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private SolicitudControlador solCtrl = new SolicitudControladorImpl();

    // Usuario actual (logueado)
    private Usuario usuarioActual;

    // Lista base según el rol y lista filtrada que se muestra en la tabla
    private List<Solicitud> listaBase;
    private List<Solicitud> lista;

    // Título dinámico de la sección
    private String tituloSeccion;

    // Modos según rol (se usan en rendered="#{reportesBean.modoX}")
    private boolean modoAdmin;
    private boolean modoDirector;
    private boolean modoDirectorTic;
    private boolean modoResponsableAccesos;
    private boolean modoOficialSeguridad;

    // Filtros comunes
    private String nombreFiltro;  // nombre / apellido del solicitante
    private Date fechaDesde;      // rango de fechas (desde)
    private Date fechaHasta;      // rango de fechas (hasta)

    // Parámetro de URL para forzar perspectiva (tic / oficial)
    private String tipoReporte;

    // =====================================================
    // CICLO DE VIDA
    // =====================================================

    @PostConstruct
    public void init() {
        usuarioActual = obtenerUsuarioLogueado();
        prepararPantalla();
    }

    /**
     * Inicializa la pantalla de reportes según el rol del usuario
     * y/o el parámetro tipoReporte (tic / oficial).
     */
    public void prepararPantalla() {
        // Reset de modos y listas
        modoAdmin = false;
        modoDirector = false;
        modoDirectorTic = false;
        modoResponsableAccesos = false;
        modoOficialSeguridad = false;

        tituloSeccion = "Reportes";
        listaBase = new ArrayList<>();
        lista = new ArrayList<>();

        // Reset filtros
        nombreFiltro = null;
        fechaDesde = null;
        fechaHasta = null;

        if (usuarioActual == null) {
            tituloSeccion = "Reportes (sin usuario)";
            return;
        }

        String cargo = cargoActual();
        String tipo = (tipoReporte != null) ? tipoReporte.trim().toLowerCase() : "";

        // 1) Si viene parámetro en la URL, manda él
        if ("tic".equals(tipo)) {
            modoDirectorTic = true;
            tituloSeccion = "Flujo por Dirección TIC";
        } else if ("oficial".equals(tipo)) {
            modoOficialSeguridad = true;
            tituloSeccion = "Flujo Oficial de Seguridad";
        } else {
            // 2) Si no hay parámetro, se usa el cargo
            if (esAdministrador(cargo)) {
                modoAdmin = true;
                tituloSeccion = "Todas las solicitudes (Administrador)";
            } else if (esDirector(cargo)) {
                modoDirector = true;
                tituloSeccion = "Solicitudes que usted firmó y aprobó";
            } else if (esDirectorTic(cargo)) {
                modoDirectorTic = true;
                tituloSeccion = "Flujo por Dirección TIC";
            } else if (esResponsableAccesos(cargo)) {
                modoResponsableAccesos = true;
                tituloSeccion = "Etapa Responsable de Accesos";
            } else if (esOficialSeguridad(cargo)) {
                modoOficialSeguridad = true;
                tituloSeccion = "Flujo Oficial de Seguridad";
            } else {
                tituloSeccion = "Reportes";
            }
        }

        // Cargar la lista base según el modo activo
        cargarBasePorRol();

        // Inicialmente, mostrar la lista completa sin filtros
        lista = new ArrayList<>(listaBase);
    }

    // =====================================================
    // UTILIDADES
    // =====================================================

    private Usuario obtenerUsuarioLogueado() {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            LoginBean lb = fc.getApplication()
                             .evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
            return (lb != null) ? lb.getUsuario() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String cargoActual() {
        if (usuarioActual == null || usuarioActual.getCargo() == null) return "";
        return usuarioActual.getCargo().trim().toLowerCase();
    }

    private boolean esAdministrador(String cargo) {
        return cargo.contains("administrador");
    }

    private boolean esDirector(String cargo) {
        return cargo.contains("director") && !cargo.contains("tic");
    }

    private boolean esDirectorTic(String cargo) {
        return cargo.contains("director") && cargo.contains("tic");
    }

    private boolean esResponsableAccesos(String cargo) {
        return cargo.contains("responsable") && cargo.contains("accesos");
    }

    private boolean esOficialSeguridad(String cargo) {
        return cargo.contains("oficial") && cargo.contains("seguridad");
    }

    // =====================================================
    // CARGA BASE POR ROL
    // =====================================================

    private void cargarBasePorRol() {
        listaBase = new ArrayList<>();

        try {
            List<Solicitud> todas = solCtrl.listarTodos();
            if (todas == null) return;

            for (Solicitud s : todas) {
                if (s == null) continue;
                String est = (s.getEstado() != null) ? s.getEstado().toUpperCase() : "";

                // Excluir ANULADAS para todos
                if ("ANULADA".equals(est)) {
                    continue;
                }

                // =========================
                // ADMINISTRADOR
                // =========================
                if (modoAdmin) {
                    listaBase.add(s);
                    continue;
                }

                // =========================
                // DIRECTOR DE ÁREA
                // =========================
                if (modoDirector) {
                    if (s.getJefeAutoriza() == null
                            || s.getJefeAutoriza().getId() == null
                            || usuarioActual == null
                            || usuarioActual.getId() == null) {
                        continue;
                    }

                    if (!usuarioActual.getId().equals(s.getJefeAutoriza().getId())) {
                        continue;
                    }

                    // No mostrar las que aún están en PENDIENTE DIRECTOR
                    if ("PENDIENTE DIRECTOR".equals(est)) {
                        continue;
                    }

                    listaBase.add(s);
                    continue;
                }

                // =========================
                // DIRECTOR TIC
                // =========================
                if (modoDirectorTic) {
                    if ("PENDIENTE DIRECTOR TIC".equals(est)
                            || "PENDIENTE OFICIAL SEGURIDAD".equals(est)
                            || "PENDIENTE RESPONSABLE ACCESOS".equals(est)
                            || "APROBADA".equals(est)
                            || "RECHAZADA".equals(est)) {
                        listaBase.add(s);
                    }
                    continue;
                }

                // =========================
                // OFICIAL DE SEGURIDAD
                // =========================
                if (modoOficialSeguridad) {
                    if ("PENDIENTE OFICIAL SEGURIDAD".equals(est)
                            || "PENDIENTE RESPONSABLE ACCESOS".equals(est)
                            || "APROBADA".equals(est)
                            || "RECHAZADA".equals(est)) {
                        listaBase.add(s);
                    }
                    continue;
                }

                // =========================
                // RESPONSABLE DE ACCESOS
                // =========================
                if (modoResponsableAccesos) {
                    // En reportes SOLO ver lo que ya fue aplicado
                    if ("APLICADO PERMISOS".equals(est)) {
                        listaBase.add(s);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al cargar reportes",
                            e.getMessage()));
        }
    }

    // =====================================================
    // FILTROS (BUSCAR / LIMPIAR)
    // =====================================================

    public void buscar() {
        lista = new ArrayList<>();

        if (listaBase == null) {
            return;
        }

        String texto = (nombreFiltro != null) ? nombreFiltro.trim().toLowerCase() : "";
        LocalDate desde = null;
        LocalDate hasta = null;

        if (fechaDesde != null) {
            desde = fechaDesde.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
        if (fechaHasta != null) {
            hasta = fechaHasta.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        for (Solicitud s : listaBase) {
            if (s == null) continue;

            // ---- Filtro por nombre / apellido del solicitante ----
            if (!texto.isEmpty()) {
                String nombreCompleto = "";
                if (s.getUsuario() != null) {
                    String n = (s.getUsuario().getNombre() != null)
                            ? s.getUsuario().getNombre()
                            : "";
                    String a = (s.getUsuario().getApellido() != null)
                            ? s.getUsuario().getApellido()
                            : "";
                    nombreCompleto = (n + " " + a).trim().toLowerCase();
                }
                if (!nombreCompleto.contains(texto)) {
                    continue;
                }
            }

            // ---- Filtro por rango de fechas (usamos fechaCreacion) ----
            if (s.getFechaCreacion() != null && (desde != null || hasta != null)) {
                LocalDate fechaSolicitud = s.getFechaCreacion().toLocalDate();

                if (desde != null && fechaSolicitud.isBefore(desde)) {
                    continue;
                }
                if (hasta != null && fechaSolicitud.isAfter(hasta)) {
                    continue;
                }
            }

            lista.add(s);
        }
    }

    public void limpiar() {
        nombreFiltro = null;
        fechaDesde = null;
        fechaHasta = null;
        lista = new ArrayList<>(listaBase);
    }

    // =====================================================
    // DESCRIPCIÓN AMIGABLE DE ESTADO (para la tabla)
    // =====================================================

    /**
     * Devuelve una descripción amigable de estado para TODOS los roles:
     * - "Rechazada"  cuando el estado real es RECHAZADA.
     * - "Firmada y aprobada"   para cualquier otro estado distinto de ANULADA.
     */
    public String descripcionEstado(String estado) {
        if (estado == null) {
            return "";
        }

        String est = estado.trim().toUpperCase();

        if ("RECHAZADA".equals(est)) {
            return "Rechazada";
        }

        return "Firmada y aprobada";
    }

    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public List<Solicitud> getLista() {
        return lista;
    }

    public void setLista(List<Solicitud> lista) {
        this.lista = lista;
    }

    public String getTituloSeccion() {
        return tituloSeccion;
    }

    public void setTituloSeccion(String tituloSeccion) {
        this.tituloSeccion = tituloSeccion;
    }

    public String getNombreFiltro() {
        return nombreFiltro;
    }

    public void setNombreFiltro(String nombreFiltro) {
        this.nombreFiltro = nombreFiltro;
    }

    public Date getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public Date getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public String getTipoReporte() {
        return tipoReporte;
    }

    public void setTipoReporte(String tipoReporte) {
        this.tipoReporte = tipoReporte;
    }

    // --------- Getters booleanos (para JSF) ---------

    public boolean isModoAdmin() {
        return modoAdmin;
    }

    public boolean isModoDirector() {
        return modoDirector;
    }

    public boolean isModoDirectorTic() {
        return modoDirectorTic;
    }

    public boolean isModoResponsableAccesos() {
        return modoResponsableAccesos;
    }

    public boolean isModoOficialSeguridad() {
        return modoOficialSeguridad;
    }

    // También con getX para EL
    public boolean getModoAdmin() { return isModoAdmin(); }
    public boolean getModoDirector() { return isModoDirector(); }
    public boolean getModoDirectorTic() { return isModoDirectorTic(); }
    public boolean getModoResponsableAccesos() { return isModoResponsableAccesos(); }
    public boolean getModoOficialSeguridad() { return isModoOficialSeguridad(); }
}
