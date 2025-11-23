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

    private SolicitudControlador solCtrl = new SolicitudControladorImpl();

    private Usuario usuarioActual;
    private List<Solicitud> listaPendientes;

    @PostConstruct
    public void init() {
        usuarioActual = obtenerUsuarioLogueado();
        cargarPendientes();
    }

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

    /**
     * Carga las solicitudes pendientes según el rol/cargo del usuario:
     *  - Director       -> PENDIENTE DIRECTOR
     *  - Director TIC   -> PENDIENTE DIRECTOR TIC
     *  - Oficial Seg.   -> PENDIENTE OFICIAL
     */
    public void cargarPendientes() {
        listaPendientes = new ArrayList<>();

        if (usuarioActual == null) {
            return;
        }

        String cargo = (usuarioActual.getCargo() != null)
                ? usuarioActual.getCargo().toUpperCase()
                : "";

        String estadoObjetivo = null;

        if (cargo.contains("DIRECTOR TIC")) {
            estadoObjetivo = "PENDIENTE DIRECTOR TIC";
        } else if (cargo.contains("OFICIAL")) {
            estadoObjetivo = "PENDIENTE OFICIAL";
        } else if (cargo.contains("DIRECTOR")) {
            // Director de área
            estadoObjetivo = "PENDIENTE DIRECTOR";
        }

        if (estadoObjetivo == null) {
            return; // no es ninguno de los roles de aprobación
        }

        // Se asume que el controlador tiene un listarTodos();
        // en caso contrario, aquí se ajusta a lo que tengas.
        List<Solicitud> todas = solCtrl.listarTodos();
        if (todas == null) {
            return;
        }

        for (Solicitud s : todas) {
            if (s.getEstado() != null &&
                s.getEstado().equalsIgnoreCase(estadoObjetivo)) {
                listaPendientes.add(s);
            }
        }
    }

    // ================= GETTERS =================

    public List<Solicitud> getListaPendientes() {
        return listaPendientes;
    }
}
