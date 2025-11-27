package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
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

    /** Solicitudes que requieren acción (firma / aplicación de accesos) */
    private List<Solicitud> listaPendientes = new ArrayList<>();

    /** Solicitudes ya finalizadas (PERMISOS APLICADOS) para Responsable de Accesos */
    private List<Solicitud> listaAprobadas = new ArrayList<>();

    private final SolicitudControlador solCtrl = new SolicitudControladorImpl();

    @PostConstruct
    public void init() {
        cargarPendientes();
        cargarAprobadas();
    }

    /**
     * Obtiene el usuario logueado desde loginBean.
     */
    private Usuario obtenerUsuarioActual() {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            LoginBean lb = fc.getApplication()
                             .evaluateExpressionGet(fc, "#{loginBean}", LoginBean.class);
            return (lb != null) ? lb.getUsuario() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Carga las solicitudes pendientes según el rol/cargo del usuario.
     * - Director            -> PENDIENTE DIRECTOR
     * - Director TIC        -> PENDIENTE DIRECTOR TIC
     * - Oficial Seguridad   -> PENDIENTE OFICIAL SEGURIDAD
     * - Responsable Accesos -> PENDIENTE APLICACIÓN ACCESOS
     */
    public void cargarPendientes() {
        Usuario u = obtenerUsuarioActual();
        if (u == null || u.getCargo() == null) {
            listaPendientes = new ArrayList<>();
            return;
        }

        String cargo = u.getCargo().trim();
        String cargoLower = cargo.toLowerCase();

        String estadoBuscado = null;

        if ("director".equalsIgnoreCase(cargo)) {
            estadoBuscado = "PENDIENTE DIRECTOR";
        } else if (cargoLower.contains("director tic")) {
            estadoBuscado = "PENDIENTE DIRECTOR TIC";
        } else if (cargoLower.contains("oficial seguridad")) {
            estadoBuscado = "PENDIENTE OFICIAL SEGURIDAD";
        } else if (cargoLower.contains("responsable accesos")) {
            // Debe coincidir con el estado que se usa en SolicitudDetalleBean
            estadoBuscado = "PENDIENTE APLICACIÓN ACCESOS";
        }

        if (estadoBuscado != null) {
            listaPendientes = solCtrl.buscarPorEstado(estadoBuscado);
            if (listaPendientes != null && !listaPendientes.isEmpty()) {
                listaPendientes.sort(
                    Comparator.comparing(Solicitud::getFechaCreacion).reversed()
                );
            }
        } else {
            listaPendientes = new ArrayList<>();
        }
    }

    /**
     * Carga las solicitudes que ya están en estado PERMISOS APLICADOS,
     * visibles solo para el Responsable de Accesos como histórico.
     */
    public void cargarAprobadas() {
        Usuario u = obtenerUsuarioActual();
        if (u == null || u.getCargo() == null) {
            listaAprobadas = new ArrayList<>();
            return;
        }

        String cargoLower = u.getCargo().trim().toLowerCase();

        if (cargoLower.contains("responsable accesos")) {
            listaAprobadas = solCtrl.buscarPorEstado("PERMISOS APLICADOS");
            if (listaAprobadas != null && !listaAprobadas.isEmpty()) {
                listaAprobadas.sort(
                    Comparator.comparing(Solicitud::getFechaCreacion).reversed()
                );
            }
        } else {
            listaAprobadas = new ArrayList<>();
        }
    }

    // ===== Getters / Setters =====

    public List<Solicitud> getListaPendientes() {
        return listaPendientes;
    }

    public void setListaPendientes(List<Solicitud> listaPendientes) {
        this.listaPendientes = listaPendientes;
    }

    public List<Solicitud> getListaAprobadas() {
        return listaAprobadas;
    }

    public void setListaAprobadas(List<Solicitud> listaAprobadas) {
        this.listaAprobadas = listaAprobadas;
    }
}
