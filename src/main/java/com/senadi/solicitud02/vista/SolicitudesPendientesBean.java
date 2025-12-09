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

    @PostConstruct
    public void init() {
        usuarioActual = obtenerUsuarioLogueado();
        listaPendientes = new ArrayList<Solicitud>();
        listaPendientesAccesos = new ArrayList<Solicitud>();
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

            // Director de área: ve las PENDIENTE DIRECTOR
            if (esDirector(cargo)) {
                listaPendientes.addAll(solCtrl.buscarPorEstado("PENDIENTE DIRECTOR"));
            }

            // Director TIC: ve PENDIENTE DIRECTOR TIC y también PENDIENTE OFICIAL SEGURIDAD
            // (porque en tu flujo el Director TIC actúa también como Oficial)
            if (esDirectorTic(cargo)) {
                listaPendientes.addAll(solCtrl.buscarPorEstado("PENDIENTE DIRECTOR TIC"));
                listaPendientes.addAll(solCtrl.buscarPorEstado("PENDIENTE OFICIAL SEGURIDAD"));
            }
            // Si existe un usuario con cargo Oficial Seguridad separado
            else if (esOficialSeguridad(cargo)) {
                listaPendientes.addAll(solCtrl.buscarPorEstado("PENDIENTE OFICIAL SEGURIDAD"));
            }

            // Responsable de Accesos también puede utilizar esta vista genérica
            if (esResponsableAccesos(cargo)) {
                listaPendientes.addAll(solCtrl.buscarPorEstado("PENDIENTE RESPONSABLE ACCESOS"));
            }

            // Eliminar duplicados manteniendo orden
            Map<Long, Solicitud> mapa = new LinkedHashMap<Long, Solicitud>();
            for (Solicitud s : listaPendientes) {
                if (s.getId() != null) {
                    mapa.put(s.getId(), s);
                }
            }
            listaPendientes = new ArrayList<Solicitud>(mapa.values());

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al cargar pendientes", e.getMessage()));
            listaPendientes = new ArrayList<Solicitud>();
        }
    }

    // ======================================================
    // PENDIENTES PARA RESPONSABLE DE ACCESOS (pendientesAccesos.xhtml)
    // ======================================================

    /**
     * Carga las solicitudes que debe ver el Responsable de Accesos
     * en la vista /Solicitud/pendientesAccesos.xhtml.
     *
     * Diseño actual:
     *  - Solo se muestran solicitudes en estado "PENDIENTE RESPONSABLE ACCESOS".
     *  - Cuando se marcan como atendidas, cambian a "APLICADO PERMISOS"
     *    y dejan de aparecer aquí (solo quedan visibles en Reportes).
     */
    public void cargarPendientesAccesos() {
        try {
            listaPendientesAccesos = new ArrayList<Solicitud>();

            if (usuarioActual == null) {
                return;
            }

            String cargo = cargoActual();
            if (!esResponsableAccesos(cargo)) {
                // Seguridad básica: si no es responsable de accesos, no se carga nada.
                return;
            }

            // Aquí asumimos que el flujo pone en este estado las solicitudes
            // que ya están aprobadas y esperan la aplicación de permisos.
            List<Solicitud> pendientes = solCtrl.buscarPorEstado("PENDIENTE RESPONSABLE ACCESOS");
            if (pendientes != null) {
                listaPendientesAccesos.addAll(pendientes);
            }

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al cargar permisos por aplicar", e.getMessage()));
            listaPendientesAccesos = new ArrayList<Solicitud>();
        }
    }

    /**
     * Marca una solicitud como "atendida" por el Responsable de Accesos.
     * Implementación: actualizar el estado a "APLICADO PERMISOS".
     *
     * IMPORTANTE: en la base de datos, el campo "estado" debe aceptar este valor.
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

            // Permitimos marcar como atendida si viene desde estado pendiente de accesos
            // o, opcionalmente, desde "APROBADA" (ajusta según tu flujo real).
            if (!"PENDIENTE RESPONSABLE ACCESOS".equals(estadoActual)
                    && !"APROBADA".equals(estadoActual)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "No permitido",
                                "Sólo puede marcar como atendidas solicitudes aprobadas y pendientes de aplicación de permisos."));
                return;
            }

            // Estado final sugerido para cuando se aplicaron los permisos
            s.setEstado("APLICADO PERMISOS");
            solCtrl.actualizar(s);

            // Recargar listas
            cargarPendientesAccesos();
            cargarPendientes(); // opcional, por si usas la vista genérica también

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
