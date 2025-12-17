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

    private final SolicitudControlador solCtrl = new SolicitudControladorImpl();

    // =============================
    // CONTEXTO
    // =============================
    private Usuario usuarioActual;

    // =============================
    // LISTAS
    // =============================
    private List<Solicitud> listaBase = new ArrayList<>();
    private List<Solicitud> lista = new ArrayList<>();

    // =============================
    // UI
    // =============================
    private String tituloSeccion = "Reportes";

    // =============================
    // MODOS (ROL)
    // =============================
    private boolean modoAdmin;
    private boolean modoDirector;
    private boolean modoDirectorTic;
    private boolean modoResponsableAccesos;
    private boolean modoOficialSeguridad;

    // =============================
    // FILTROS
    // =============================
    private String nombreFiltro;
    private Date fechaDesde;
    private Date fechaHasta;

    // =============================
    // PARAM URL
    // =============================
    private String tipoReporte;

    // =====================================================
    // CICLO DE VIDA
    // =====================================================

    @PostConstruct
    public void init() {
        usuarioActual = obtenerUsuarioLogueado();
        prepararPantalla();
    }

    public void prepararPantalla() {

        modoAdmin = false;
        modoDirector = false;
        modoDirectorTic = false;
        modoResponsableAccesos = false;
        modoOficialSeguridad = false;

        listaBase.clear();
        lista.clear();

        nombreFiltro = null;
        fechaDesde = null;
        fechaHasta = null;

        if (usuarioActual == null) {
            tituloSeccion = "Reportes";
            return;
        }

        String cargo = cargoActual();
        String tipo = (tipoReporte != null) ? tipoReporte.trim().toLowerCase() : "";

        if ("tic".equals(tipo)) {
            modoDirectorTic = true;
            tituloSeccion = "Flujo por Dirección TIC";
        } else if ("oficial".equals(tipo)) {
            modoOficialSeguridad = true;
            tituloSeccion = "Flujo Oficial de Seguridad";
        } else {
            if (esAdministrador(cargo)) {
                modoAdmin = true;
                tituloSeccion = "Todas las solicitudes (Administrador)";
            } else if (esDirector(cargo)) {
                modoDirector = true;
                tituloSeccion = "Solicitudes firmadas por Dirección";
            } else if (esDirectorTic(cargo)) {
                modoDirectorTic = true;
                tituloSeccion = "Flujo por Dirección TIC";
            } else if (esOficialSeguridad(cargo)) {
                modoOficialSeguridad = true;
                tituloSeccion = "Flujo Oficial de Seguridad";
            } else if (esResponsableAccesos(cargo)) {
                modoResponsableAccesos = true;
                tituloSeccion = "Responsable de Accesos";
            }
        }

        cargarBasePorRol();
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
        return (usuarioActual != null && usuarioActual.getCargo() != null)
                ? usuarioActual.getCargo().toLowerCase()
                : "";
    }

    private boolean esAdministrador(String c) { return c.contains("administrador"); }
    private boolean esDirector(String c) { return c.contains("director") && !c.contains("tic"); }
    private boolean esDirectorTic(String c) { return c.contains("director") && c.contains("tic"); }
    private boolean esResponsableAccesos(String c) { return c.contains("responsable") && c.contains("accesos"); }
    private boolean esOficialSeguridad(String c) { return c.contains("oficial") && c.contains("seguridad"); }

    // =====================================================
    // CARGA BASE POR ROL (SIN Firma.usuario)
    // =====================================================

    private void cargarBasePorRol() {

        try {
            List<Solicitud> todas = solCtrl.listarTodos();
            if (todas == null) return;

            for (Solicitud s : todas) {
                if (s == null || s.getEstado() == null) continue;

                String est = s.getEstado().toUpperCase();

                // Excluir anuladas
                if ("ANULADA".equals(est)) continue;

                // ADMIN
                if (modoAdmin) {
                    listaBase.add(s);
                    continue;
                }

                // DIRECTOR
                if (modoDirector) {
                    if (!"PENDIENTE DIRECTOR".equals(est)) {
                        listaBase.add(s);
                    }
                    continue;
                }

                // DIRECTOR TIC
                if (modoDirectorTic) {
                    if (est.contains("TIC")
                            || est.contains("OFICIAL")
                            || est.contains("RESPONSABLE")
                            || est.contains("APLICADO")) {
                        listaBase.add(s);
                    }
                    continue;
                }

                // OFICIAL SEGURIDAD
                if (modoOficialSeguridad) {
                    if (est.contains("OFICIAL")
                            || est.contains("RESPONSABLE")
                            || est.contains("APLICADO")) {
                        listaBase.add(s);
                    }
                    continue;
                }

                // RESPONSABLE DE ACCESOS
                if (modoResponsableAccesos) {
                    if ("APLICADO PERMISOS".equals(est)
                            || "PERMISOS APLICADOS".equals(est)) {
                        listaBase.add(s);
                    }
                }
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al cargar reportes", e.getMessage()));
        }
    }

    // =====================================================
    // FILTROS
    // =====================================================

    public void buscar() {
        lista = new ArrayList<>();

        String texto = (nombreFiltro != null) ? nombreFiltro.trim().toLowerCase() : "";
        LocalDate desde = (fechaDesde != null)
                ? fechaDesde.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : null;
        LocalDate hasta = (fechaHasta != null)
                ? fechaHasta.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : null;

        for (Solicitud s : listaBase) {
            if (s == null) continue;

            if (!texto.isEmpty()) {
                String nc = (s.getUsuario() != null)
                        ? (s.getUsuario().getNombre() + " " + s.getUsuario().getApellido()).toLowerCase()
                        : "";
                if (!nc.contains(texto)) continue;
            }

            if (s.getFechaCreacion() != null && (desde != null || hasta != null)) {
                LocalDate f = s.getFechaCreacion().toLocalDate();
                if (desde != null && f.isBefore(desde)) continue;
                if (hasta != null && f.isAfter(hasta)) continue;
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
    // VISUALIZACIÓN PDF (POR ESTADO + ROL)
    // =====================================================

    public boolean puedeVerPdf(Solicitud s) {

        if (s == null || s.getEstado() == null || usuarioActual == null) {
            return false;
        }

        String est = s.getEstado().toUpperCase();

        if (modoAdmin) return true;

        if (modoDirector) {
            return !"PENDIENTE DIRECTOR".equals(est);
        }

        if (modoDirectorTic) {
            return est.contains("TIC")
                    || est.contains("OFICIAL")
                    || est.contains("RESPONSABLE")
                    || est.contains("APLICADO");
        }

        if (modoOficialSeguridad) {
            return est.contains("OFICIAL")
                    || est.contains("RESPONSABLE")
                    || est.contains("APLICADO");
        }

        if (modoResponsableAccesos) {
            return est.contains("APLICADO");
        }

        return false;
    }

    // =====================================================
    // UTILIDADES PARA LA VISTA
    // =====================================================

    public String descripcionEstado(String estado) {
        if (estado == null) return "";
        String e = estado.toUpperCase();

        if ("RECHAZADA".equals(e)) return "Rechazada";
        if ("APLICADO PERMISOS".equals(e) || "PERMISOS APLICADOS".equals(e))
            return "Permisos aplicados";

        return "Firmada y aprobada";
    }

    // =====================================================
    // GETTERS / SETTERS (JSF)
    // =====================================================

    public List<Solicitud> getLista() { return lista; }
    public String getTituloSeccion() { return tituloSeccion; }

    public String getNombreFiltro() { return nombreFiltro; }
    public void setNombreFiltro(String v) { nombreFiltro = v; }

    public Date getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(Date v) { fechaDesde = v; }

    public Date getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(Date v) { fechaHasta = v; }

    public String getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(String v) { tipoReporte = v; }

    public boolean isModoAdmin() { return modoAdmin; }
    public boolean isModoDirector() { return modoDirector; }
    public boolean isModoDirectorTic() { return modoDirectorTic; }
    public boolean isModoResponsableAccesos() { return modoResponsableAccesos; }
    public boolean isModoOficialSeguridad() { return modoOficialSeguridad; }

    public boolean getModoAdmin() { return modoAdmin; }
    public boolean getModoDirector() { return modoDirector; }
    public boolean getModoDirectorTic() { return modoDirectorTic; }
    public boolean getModoResponsableAccesos() { return modoResponsableAccesos; }
    public boolean getModoOficialSeguridad() { return modoOficialSeguridad; }
}
