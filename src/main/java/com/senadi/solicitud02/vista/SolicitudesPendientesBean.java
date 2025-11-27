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

    private List<Solicitud> listaPendientes;
    private SolicitudControlador solCtrl = new SolicitudControladorImpl();
    private Usuario usuarioActual;

    @PostConstruct
    public void init() {
        usuarioActual = obtenerUsuarioLogueado();
        listaPendientes = new ArrayList<>();
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

    /**
     * Carga la lista de solicitudes pendientes según el rol/cargo del usuario actual.
     * Se invoca desde f:event preRenderView en pendientes.xhtml.
     */
    public void cargarPendientes() {
        try {
            listaPendientes = new ArrayList<>();

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

            // Responsable de Accesos
            if (esResponsableAccesos(cargo)) {
                listaPendientes.addAll(solCtrl.buscarPorEstado("PENDIENTE RESPONSABLE ACCESOS"));
            }

            // Eliminar duplicados manteniendo orden
            Map<Long, Solicitud> mapa = new LinkedHashMap<>();
            for (Solicitud s : listaPendientes) {
                if (s.getId() != null) {
                    mapa.put(s.getId(), s);
                }
            }
            listaPendientes = new ArrayList<>(mapa.values());

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al cargar pendientes", e.getMessage()));
            listaPendientes = new ArrayList<>();
        }
    }

    // ================= GETTERS / SETTERS =================

    public List<Solicitud> getListaPendientes() {
        return listaPendientes;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
}
