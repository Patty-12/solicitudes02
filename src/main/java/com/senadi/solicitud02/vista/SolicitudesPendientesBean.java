package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.senadi.solicitud02.controlador.SolicitudControlador;
import com.senadi.solicitud02.controlador.impl.SolicitudControladorImpl;
import com.senadi.solicitud02.modelo.entidades.Solicitud;
import com.senadi.solicitud02.modelo.entidades.Usuario;

@ManagedBean(name = "solicitudesPendientesBean")
@ViewScoped
public class SolicitudesPendientesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // Pendientes para firmas (director, director TIC, oficial, responsable accesos)
    private List<Solicitud> listaPendientes;

    // Pendientes específicos para Responsable de Accesos (vista pendientesAccesos.xhtml)
    private List<Solicitud> listaPendientesAccesos;

    // Para marcar como atendida una solicitud desde pendientesAccesos.xhtml
    private Solicitud solicitudSeleccionada;

    private SolicitudControlador solCtrl = new SolicitudControladorImpl();
    private Usuario usuarioActual;

    // =======================
    // NOTIFICACIONES (badge)
    // =======================
    private Integer pendientesFirmaCountCache;
    private Integer pendientesPorAplicarCountCache;

    @PostConstruct
    public void init() {
        usuarioActual = obtenerUsuarioLogueado();
        listaPendientes = new ArrayList<Solicitud>();
        listaPendientesAccesos = new ArrayList<Solicitud>();

        // cache inicial (evita que salga null en el menú)
        pendientesFirmaCountCache = null;
        pendientesPorAplicarCountCache = null;
    }

    // ======================================================
    // UTILIDADES
    // ======================================================

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

    private boolean esDirector(String cargo) {
        // Director de área (financiero, administrativo, etc.), no Director TIC
        return cargo.contains("director") && !cargo.contains("tic");
    }

    private boolean esDirectorTic(String cargo) {
        return cargo.contains("director") && cargo.contains("tic");
    }

    private boolean esOficialSeguridad(String cargo) {
        return cargo.contains("oficial") && cargo.contains("seguridad");
    }

    private boolean esResponsableAccesos(String cargo) {
        return cargo.contains("responsable") && cargo.contains("accesos");
    }

    private List<Solicitud> safeBuscarPorEstado(String estado) {
        try {
            List<Solicitud> l = solCtrl.buscarPorEstado(estado);
            return (l != null) ? l : new ArrayList<Solicitud>();
        } catch (Exception e) {
            return new ArrayList<Solicitud>();
        }
    }

    private List<Solicitud> deduplicarPorId(List<Solicitud> input) {
        Map<Long, Solicitud> mapa = new LinkedHashMap<Long, Solicitud>();
        if (input != null) {
            for (Solicitud s : input) {
                if (s != null && s.getId() != null) {
                    mapa.put(s.getId(), s);
                }
            }
        }
        return new ArrayList<Solicitud>(mapa.values());
    }

    // ======================================================
    // CONTADORES PARA NOTIFICACIONES (MENÚ)
    // ======================================================

    /**
     * Contador para badge en "Solicitudes Pendientes" (pendientes.xhtml)
     * según el cargo del usuario actual.
     */
    public int getPendientesFirmaCount() {
        if (pendientesFirmaCountCache == null) {
            pendientesFirmaCountCache = calcularPendientesFirmaCount();
        }
        return pendientesFirmaCountCache;
    }

    /**
     * Contador para badge en "Solicitudes por aplicar" (pendientesAccesos.xhtml)
     * para Responsable de Accesos.
     */
    public int getPendientesPorAplicarCount() {
        if (pendientesPorAplicarCountCache == null) {
            pendientesPorAplicarCountCache = calcularPendientesPorAplicarCount();
        }
        return pendientesPorAplicarCountCache;
    }

    /**
     * Recalcula ambos contadores (útil después de actualizar estado).
     */
    public void refrescarContadores() {
        pendientesFirmaCountCache = calcularPendientesFirmaCount();
        pendientesPorAplicarCountCache = calcularPendientesPorAplicarCount();
    }

    private int calcularPendientesFirmaCount() {
        if (usuarioActual == null) return 0;

        String cargo = cargoActual();
        List<Solicitud> acumulado = new ArrayList<Solicitud>();

        // Director de área
        if (esDirector(cargo)) {
            acumulado.addAll(safeBuscarPorEstado("PENDIENTE DIRECTOR"));
        }

        // Director TIC: ve Director TIC + Oficial Seguridad
        if (esDirectorTic(cargo)) {
            acumulado.addAll(safeBuscarPorEstado("PENDIENTE DIRECTOR TIC"));
            acumulado.addAll(safeBuscarPorEstado("PENDIENTE OFICIAL SEGURIDAD"));
        }
        // Oficial Seguridad separado
        else if (esOficialSeguridad(cargo)) {
            acumulado.addAll(safeBuscarPorEstado("PENDIENTE OFICIAL SEGURIDAD"));
        }

        // Responsable de accesos (si también usa esta vista genérica)
        if (esResponsableAccesos(cargo)) {
            acumulado.addAll(safeBuscarPorEstado("PENDIENTE RESPONSABLE ACCESOS"));
        }

        return deduplicarPorId(acumulado).size();
    }

    private int calcularPendientesPorAplicarCount() {
        if (usuarioActual == null) return 0;

        String cargo = cargoActual();
        if (!esResponsableAccesos(cargo)) return 0;

        // pendientes por aplicar permisos
        return safeBuscarPorEstado("PENDIENTE RESPONSABLE ACCESOS").size();
    }

    // ======================================================
    // PENDIENTES DE FIRMA (pendientes.xhtml)
    // ======================================================

    /**
     * Carga la lista de solicitudes pendientes según el rol/cargo del usuario actual.
     * Se invoca desde f:event preRenderView en pendientes.xhtml.
     */
    public void cargarPendientes() {
        try {
            listaPendientes = new ArrayList<Solicitud>();

            if (usuarioActual == null) {
                return;
            }

            String cargo = cargoActual();
            List<Solicitud> acumulado = new ArrayList<Solicitud>();

            // Director de área
            if (esDirector(cargo)) {
                acumulado.addAll(safeBuscarPorEstado("PENDIENTE DIRECTOR"));
            }

            // Director TIC
            if (esDirectorTic(cargo)) {
                acumulado.addAll(safeBuscarPorEstado("PENDIENTE DIRECTOR TIC"));
                acumulado.addAll(safeBuscarPorEstado("PENDIENTE OFICIAL SEGURIDAD"));
            }
            // Oficial Seguridad separado
            else if (esOficialSeguridad(cargo)) {
                acumulado.addAll(safeBuscarPorEstado("PENDIENTE OFICIAL SEGURIDAD"));
            }

            // Responsable Accesos también puede usar esta vista genérica
            if (esResponsableAccesos(cargo)) {
                acumulado.addAll(safeBuscarPorEstado("PENDIENTE RESPONSABLE ACCESOS"));
            }

            listaPendientes = deduplicarPorId(acumulado);

            // actualizar badge
            pendientesFirmaCountCache = listaPendientes.size();

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al cargar pendientes", e.getMessage()));
            listaPendientes = new ArrayList<Solicitud>();
            pendientesFirmaCountCache = 0;
        }
    }

    // ======================================================
    // PENDIENTES PARA RESPONSABLE DE ACCESOS (pendientesAccesos.xhtml)
    // ======================================================

    /**
     * Carga las solicitudes que debe ver el Responsable de Accesos
     * en la vista /Solicitud/pendientesAccesos.xhtml.
     */
    public void cargarPendientesAccesos() {
        try {
            listaPendientesAccesos = new ArrayList<Solicitud>();

            if (usuarioActual == null) {
                return;
            }

            String cargo = cargoActual();
            if (!esResponsableAccesos(cargo)) {
                return;
            }

            List<Solicitud> pendientes = safeBuscarPorEstado("PENDIENTE RESPONSABLE ACCESOS");
            listaPendientesAccesos.addAll(pendientes);

            // actualizar badge
            pendientesPorAplicarCountCache = listaPendientesAccesos.size();

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al cargar permisos por aplicar", e.getMessage()));
            listaPendientesAccesos = new ArrayList<Solicitud>();
            pendientesPorAplicarCountCache = 0;
        }
    }

    /**
     * Marca una solicitud como "atendida" por el Responsable de Accesos.
     * Estado final: "APLICADO PERMISOS".
     */
    public void marcarComoAtendido() {
        if (solicitudSeleccionada == null || solicitudSeleccionada.getId() == null) {
            return;
        }

        try {
            Solicitud s = solCtrl.buscarPorId(solicitudSeleccionada.getId());
            if (s == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Solicitud no encontrada",
                                "ID: " + solicitudSeleccionada.getId()));
                return;
            }

            String estadoActual = (s.getEstado() != null) ? s.getEstado().toUpperCase() : "";

            if (!"PENDIENTE RESPONSABLE ACCESOS".equals(estadoActual)
                    && !"APROBADA".equals(estadoActual)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "No permitido",
                                "Sólo puede marcar como atendidas solicitudes aprobadas y pendientes de aplicación de permisos."));
                return;
            }

            s.setEstado("APLICADO PERMISOS");
            solCtrl.actualizar(s);

            // Recargar listas
            cargarPendientesAccesos();
            cargarPendientes();

            // Recalcular notificaciones
            refrescarContadores();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Permisos aplicados",
                            "Se ha marcado la solicitud como atendida por el Responsable de Accesos."));

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al marcar como atendida", e.getMessage()));
        }
    }

    // ================= GETTERS / SETTERS =================

    public List<Solicitud> getListaPendientes() {
        return listaPendientes;
    }

    public List<Solicitud> getListaPendientesAccesos() {
        return listaPendientesAccesos;
    }

    public Solicitud getSolicitudSeleccionada() {
        return solicitudSeleccionada;
    }

    public void setSolicitudSeleccionada(Solicitud solicitudSeleccionada) {
        this.solicitudSeleccionada = solicitudSeleccionada;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
}
