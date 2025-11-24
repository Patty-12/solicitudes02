package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.util.ArrayList;
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

    private List<Solicitud> listaPendientes = new ArrayList<>();

    private final SolicitudControlador solCtrl = new SolicitudControladorImpl();

    @PostConstruct
    public void init() {
        cargarPendientes();
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
     * - Director           -> PENDIENTE DIRECTOR
     * - Director TIC       -> PENDIENTE DIRECTOR TIC
     * - Oficial Seguridad  -> PENDIENTE OFICIAL SEGURIDAD
     * - Responsable Accesos-> PENDIENTE RESPONSABLE ACCESOS
     */
    public void cargarPendientes() {
        Usuario u = obtenerUsuarioActual();
        if (u == null || u.getCargo() == null) {
            listaPendientes = new ArrayList<>();
            return;
        }

        String cargo = u.getCargo().trim();

        // Normalizamos a minúsculas para comparar con contains cuando haga falta
        String cargoLower = cargo.toLowerCase();

        String estadoBuscado = null;

        if ("Director".equalsIgnoreCase(cargo)) {
            estadoBuscado = "PENDIENTE DIRECTOR";
        } else if (cargoLower.contains("director tic")) {
            estadoBuscado = "PENDIENTE DIRECTOR TIC";
        } else if (cargoLower.contains("oficial seguridad")) {
            estadoBuscado = "PENDIENTE OFICIAL SEGURIDAD";
        } else if (cargoLower.contains("responsable accesos")) {
            estadoBuscado = "PENDIENTE RESPONSABLE ACCESOS";
        }

        if (estadoBuscado != null) {
            listaPendientes = solCtrl.buscarPorEstado(estadoBuscado);
        } else {
            // Para otros cargos (Usuario, Administrador, etc.) no mostramos nada
            listaPendientes = new ArrayList<>();
        }
    }

    // ===== Getter / Setter =====

    public List<Solicitud> getListaPendientes() {
        return listaPendientes;
    }

    public void setListaPendientes(List<Solicitud> listaPendientes) {
        this.listaPendientes = listaPendientes;
    }
}
