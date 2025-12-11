package com.senadi.solicitud02.vista;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.senadi.solicitud02.controlador.PermisoAplicacionControlador;
import com.senadi.solicitud02.controlador.AplicacionControlador;
import com.senadi.solicitud02.controlador.impl.PermisoAplicacionControladorImpl;
import com.senadi.solicitud02.controlador.impl.AplicacionControladorImpl;
import com.senadi.solicitud02.modelo.entidades.PermisoAplicacion;
import com.senadi.solicitud02.modelo.entidades.Aplicacion;

@ManagedBean(name = "permisoAplicacionBean")
@ViewScoped
public class PermisoAplicacionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private PermisoAplicacionControlador permisoCtrl = new PermisoAplicacionControladorImpl();
    private AplicacionControlador aplicacionCtrl = new AplicacionControladorImpl();

    // Listado principal (toda la tabla)
    private List<PermisoAplicacion> lista;

    // Formulario (crear / editar)
    private PermisoAplicacion formulario;

    // Registro seleccionado para eliminar
    private PermisoAplicacion permisoSeleccionado;

    // Soporte para combos
    private List<Aplicacion> aplicaciones;
    private Long idAplicacionSeleccionada;

    // Para edición vía ?id=XX
    private Long idPermisoAplicacion;

    // Filtro global de texto (como en AplicacionBean)
    private String filtroTexto;

    // Filtro por aplicación (cuando venimos desde Gestión de Aplicaciones)
    private Long idAplicacionFiltro;
    private String nombreAplicacionFiltro;

    @PostConstruct
    public void init() {
        formulario = new PermisoAplicacion();
        cargarAplicaciones();
        cargarLista();
        cargarNombreAplicacionFiltro();
    }

    private void cargarAplicaciones() {
        aplicaciones = aplicacionCtrl.listarTodos();
    }

    private void cargarLista() {
        lista = permisoCtrl.listarTodos();
    }

    private void cargarNombreAplicacionFiltro() {
        if (idAplicacionFiltro != null) {
            try {
                Aplicacion app = aplicacionCtrl.buscarPorId(idAplicacionFiltro);
                if (app != null) {
                    nombreAplicacionFiltro = app.getNombre();
                }
            } catch (Exception e) {
                // solo log, no romper la vista
                e.printStackTrace();
            }
        }
    }

    /**
     * Cargar un permiso específico para edición (usado por <f:viewAction> en edit.xhtml).
     */
    public String cargarPermisoAplicacion() {
        if (idPermisoAplicacion != null) {
            PermisoAplicacion p = permisoCtrl.buscarPorId(idPermisoAplicacion);
            if (p != null) {
                permisoSeleccionado = p;
                formulario = p;

                if (p.getAplicacion() != null) {
                    idAplicacionSeleccionada = p.getAplicacion().getId();
                    // si no vino el filtro en el URL, lo ajustamos al de este permiso
                    if (idAplicacionFiltro == null) {
                        idAplicacionFiltro = idAplicacionSeleccionada;
                        nombreAplicacionFiltro = p.getAplicacion().getNombre();
                    }
                }
                cargarAplicaciones();
            } else {
                FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Permiso no encontrado",
                            "El registro solicitado no existe."));
            }
        }
        return null;
    }

    /**
     * Crear o actualizar permiso.
     */
    public String guardar() {
        try {
            // Resolver aplicación para el formulario
            Long idAppParaGuardar = idAplicacionSeleccionada;

            // Si no se seleccionó nada en el combo pero hay filtro de aplicación, usamos ese
            if (idAppParaGuardar == null && idAplicacionFiltro != null) {
                idAppParaGuardar = idAplicacionFiltro;
            }

            if (idAppParaGuardar != null) {
                Aplicacion a = aplicacionCtrl.buscarPorId(idAppParaGuardar);
                formulario.setAplicacion(a);
            } else {
                formulario.setAplicacion(null);
            }

            boolean esNuevo = (formulario.getId() == null);

            if (esNuevo) {
                permisoCtrl.crear(formulario);
            } else {
                permisoCtrl.actualizar(formulario);
            }

            cargarLista();
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Éxito",
                        "Permiso de aplicación guardado correctamente."));

            // Redirigir al listado, manteniendo la aplicación si existe
            if (idAplicacionFiltro != null) {
                return "/PermisoAplicacion/index.xhtml?faces-redirect=true&idAplicacion=" + idAplicacionFiltro;
            } else {
                return "/PermisoAplicacion/index.xhtml?faces-redirect=true";
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error al guardar",
                        e.getMessage()));
        }
        return null; // si hay error, nos quedamos en la vista
    }

    /**
     * Eliminar el permiso seleccionado.
     */
    public String eliminar() {
        if (permisoSeleccionado != null && permisoSeleccionado.getId() != null) {
            try {
                // Verificar si tiene accesos asociados
                if (permisoSeleccionado.getAccesos() != null &&
                    !permisoSeleccionado.getAccesos().isEmpty()) {

                    FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "No se puede eliminar",
                            "El permiso tiene accesos de usuario asociados. " +
                            "Primero elimine o reasigne esos accesos.")
                    );
                    return null;
                }

                // Eliminar si no tiene hijos
                permisoCtrl.eliminar(permisoSeleccionado.getId());
                cargarLista();

                FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        "Eliminado",
                        "El permiso ha sido eliminado correctamente.")
                );

            } catch (Exception e) {
                e.printStackTrace(); // detalle en consola

                String detalle = (e.getCause() != null && e.getCause().getMessage() != null)
                        ? e.getCause().getMessage()
                        : e.getMessage();

                FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Error al eliminar",
                        detalle)
                );
            }
        }
        return null;
    }

    public void cancelar() {
        formulario = new PermisoAplicacion();
        idAplicacionSeleccionada = null;
        cargarAplicaciones();
    }

    // ================= LISTA FILTRADA (texto + app) =================

    public List<PermisoAplicacion> getListaFiltrada() {
        if (lista == null) {
            return null;
        }

        List<PermisoAplicacion> resultado = new ArrayList<>();

        String f = (filtroTexto == null) ? "" : filtroTexto.trim().toLowerCase();
        boolean aplicarTexto = !f.isEmpty();

        for (PermisoAplicacion p : lista) {

            // Filtrar por aplicación si viene desde Gestión de Aplicaciones
            if (idAplicacionFiltro != null) {
                if (p.getAplicacion() == null ||
                    !idAplicacionFiltro.equals(p.getAplicacion().getId())) {
                    continue;
                }
            }

            if (aplicarTexto) {
                String nombreApp = (p.getAplicacion() != null && p.getAplicacion().getNombre() != null)
                        ? p.getAplicacion().getNombre().toLowerCase()
                        : "";
                String nombrePermiso = (p.getNombre() != null)
                        ? p.getNombre().toLowerCase()
                        : "";
                String desc = (p.getDescripcion() != null)
                        ? p.getDescripcion().toLowerCase()
                        : "";

                if (!(nombreApp.contains(f) ||
                      nombrePermiso.contains(f) ||
                      desc.contains(f))) {
                    continue;
                }
            }

            resultado.add(p);
        }

        return resultado;
    }

    // ================= GETTERS / SETTERS =================

    public List<PermisoAplicacion> getLista() {
        return lista;
    }

    public void setLista(List<PermisoAplicacion> lista) {
        this.lista = lista;
    }

    public PermisoAplicacion getFormulario() {
        return formulario;
    }

    public void setFormulario(PermisoAplicacion formulario) {
        this.formulario = formulario;
    }

    public PermisoAplicacion getPermisoSeleccionado() {
        return permisoSeleccionado;
    }

    public void setPermisoSeleccionado(PermisoAplicacion permisoSeleccionado) {
        this.permisoSeleccionado = permisoSeleccionado;
    }

    public List<Aplicacion> getAplicaciones() {
        return aplicaciones;
    }

    public void setAplicaciones(List<Aplicacion> aplicaciones) {
        this.aplicaciones = aplicaciones;
    }

    public Long getIdAplicacionSeleccionada() {
        return idAplicacionSeleccionada;
    }

    public void setIdAplicacionSeleccionada(Long idAplicacionSeleccionada) {
        this.idAplicacionSeleccionada = idAplicacionSeleccionada;
    }

    public Long getIdPermisoAplicacion() {
        return idPermisoAplicacion;
    }

    public void setIdPermisoAplicacion(Long idPermisoAplicacion) {
        this.idPermisoAplicacion = idPermisoAplicacion;
    }

    public String getFiltroTexto() {
        return filtroTexto;
    }

    public void setFiltroTexto(String filtroTexto) {
        this.filtroTexto = filtroTexto;
    }

    public Long getIdAplicacionFiltro() {
        return idAplicacionFiltro;
    }

    public void setIdAplicacionFiltro(Long idAplicacionFiltro) {
        this.idAplicacionFiltro = idAplicacionFiltro;
        cargarNombreAplicacionFiltro();
    }

    public String getNombreAplicacionFiltro() {
        return nombreAplicacionFiltro;
    }

    public void setNombreAplicacionFiltro(String nombreAplicacionFiltro) {
        this.nombreAplicacionFiltro = nombreAplicacionFiltro;
    }
}
