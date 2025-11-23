package com.senadi.solicitud02.vista;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.file.UploadedFile;

import com.senadi.solicitud02.controlador.SolicitudControlador;
import com.senadi.solicitud02.controlador.UsuarioControlador;
import com.senadi.solicitud02.controlador.impl.SolicitudControladorImpl;
import com.senadi.solicitud02.controlador.impl.UsuarioControladorImpl;
import com.senadi.solicitud02.modelo.entidades.AccesoUsuario;
import com.senadi.solicitud02.modelo.entidades.PermisoAplicacion;
import com.senadi.solicitud02.modelo.entidades.Solicitud;
import com.senadi.solicitud02.modelo.entidades.Usuario;

@ManagedBean(name = "solicitudDetalleBean")
@ViewScoped
public class SolicitudDetalleBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // ------------------------------
    //        VARIABLES PRINCIPALES
    // ------------------------------

    private Long id;
    private Solicitud solicitud;

    private SolicitudControlador solCtrl = new SolicitudControladorImpl();
    private UsuarioControlador usuarioCtrl = new UsuarioControladorImpl();

    private Usuario usuarioActual;

    // Archivos de firma
    private UploadedFile archivoFirmado;            // solicitante
    private UploadedFile archivoFirmadoDirector;    // director
    private UploadedFile archivoFirmadoDTIC;        // director TIC
    private UploadedFile archivoFirmadoOficial;     // oficial seguridad

    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ------------------------------
    //             INIT
    // ------------------------------

    @PostConstruct
    public void init() {
        usuarioActual = obtenerUsuarioLogueado();
        if (id != null) {
            cargar();
        }
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

    // ------------------------------
    //            CARGA
    // ------------------------------

    public void cargar() {
        if (id == null) {
            String param = FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getRequestParameterMap()
                    .get("id");

            if (param != null && !param.isEmpty()) {
                try {
                    id = Long.parseLong(param);
                } catch (Exception e) {
                    id = null;
                }
            }
        }

        if (id != null) {
            solicitud = solCtrl.buscarPorId(id);
        }
    }

    // ========================================================
    //                ROLES Y PERMISOS
    // ========================================================

    public boolean isSolicitanteActual() {
        if (solicitud == null || solicitud.getUsuario() == null || usuarioActual == null) return false;
        return solicitud.getUsuario().getId().equals(usuarioActual.getId());
    }

    public boolean isDirectorArea() {
        return usuarioActual != null && "Director".equalsIgnoreCase(usuarioActual.getCargo());
    }

    public boolean isDirectorTIC() {
        return usuarioActual != null && "Director TIC".equalsIgnoreCase(usuarioActual.getCargo());
    }

    public boolean isOficialSeguridad() {
        return usuarioActual != null && "Oficial Seguridad".equalsIgnoreCase(usuarioActual.getCargo());
    }

    // ========================================================
    //          ESTADOS DEL FLUJO DE FIRMA
    // ========================================================

    public boolean isEstadoPendienteDirector() {
        return solicitud != null && "PENDIENTE DIRECTOR".equalsIgnoreCase(solicitud.getEstado());
    }

    public boolean isEstadoPendienteDTIC() {
        return solicitud != null && "PENDIENTE DIRECTOR TIC".equalsIgnoreCase(solicitud.getEstado());
    }

    public boolean isEstadoPendienteOficial() {
        return solicitud != null && "PENDIENTE OFICIAL".equalsIgnoreCase(solicitud.getEstado());
    }

    public boolean isPuedeEnviarADirector() {
        return isSolicitanteActual() && solicitud != null
                && "CREADA".equalsIgnoreCase(solicitud.getEstado());
    }

    // ========================================================
    //             LOGICA DE SUBIDA DE PDF
    // ========================================================

    private boolean guardarArchivo(UploadedFile file, String nombreDestino) {

        if (file == null || file.getFileName() == null || file.getFileName().isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Archivo requerido", "Debe seleccionar un archivo PDF.");
            return false;
        }

        try (InputStream in = file.getInputStream()) {

            String basePath = FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getRealPath("/firmas");

            if (basePath == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Error de ruta", "No se pudo determinar ruta base.");
                return false;
            }

            Path dir = Paths.get(basePath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            Path destino = dir.resolve(nombreDestino);
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);

            return true;

        } catch (IOException ex) {
            ex.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar archivo", ex.getMessage());
            return false;
        }
    }

    // ================================
    //          1. SOLICITANTE
    // ================================

    public void subirFirmado() {

        if (!isSolicitanteActual()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Acción no permitida", "No es el solicitante.");
            return;
        }

        boolean ok = guardarArchivo(
                archivoFirmado,
                "solicitud_" + solicitud.getId() + "_firmada_solicitante.pdf"
        );

        if (ok) {
            addMessage(FacesMessage.SEVERITY_INFO, "Archivo cargado", "Se cargó PDF del solicitante.");
        }
    }

    public void enviarAlDirector() {
        if (!isSolicitanteActual()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Acción no permitida", "No es solicitante.");
            return;
        }

        solicitud.setEstado("PENDIENTE DIRECTOR");
        solCtrl.actualizar(solicitud);

        addMessage(FacesMessage.SEVERITY_INFO, "Enviado", "La solicitud fue enviada al Director.");
    }

    // ================================
    //          2. DIRECTOR
    // ================================

    public void subirFirmadoDirector() {
        if (!isDirectorArea() || !isEstadoPendienteDirector()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Acción no permitida", "No corresponde");
            return;
        }

        boolean ok = guardarArchivo(
                archivoFirmadoDirector,
                "solicitud_" + solicitud.getId() + "_firmada_director.pdf"
        );

        if (ok) {
            addMessage(FacesMessage.SEVERITY_INFO, "PDF cargado", "Firma del Director registrada.");
        }
    }

    public void directorAprueba() {
        if (!isDirectorArea() || !isEstadoPendienteDirector()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Acción no permitida", "No corresponde.");
            return;
        }

        solicitud.setEstado("PENDIENTE DIRECTOR TIC");
        solCtrl.actualizar(solicitud);

        addMessage(FacesMessage.SEVERITY_INFO, "Aprobado por Director", "Ahora el Director TIC debe revisar.");
    }

    public void directorRechaza() {
        if (!isDirectorArea() || !isEstadoPendienteDirector()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Acción no permitida", "No corresponde.");
            return;
        }

        solicitud.setEstado("RECHAZADA");
        solCtrl.actualizar(solicitud);

        addMessage(FacesMessage.SEVERITY_INFO, "Solicitud rechazada", "El Director rechazó la solicitud.");
    }

    // ================================
    //          3. DIRECTOR TIC
    // ================================

    public void subirFirmadoDTIC() {
        if (!isDirectorTIC() || !isEstadoPendienteDTIC()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Acción no permitida", "No corresponde.");
            return;
        }

        boolean ok = guardarArchivo(
                archivoFirmadoDTIC,
                "solicitud_" + solicitud.getId() + "_firmada_dtic.pdf"
        );

        if (ok) {
            addMessage(FacesMessage.SEVERITY_INFO, "PDF cargado", "Firma del Director TIC registrada.");
        }
    }

    public void dticApruebaSinOficial() {
        if (!isDirectorTIC() || !isEstadoPendienteDTIC()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Acción no permitida", "No corresponde.");
            return;
        }

        solicitud.setEstado("APROBADA");
        solCtrl.actualizar(solicitud);

        addMessage(FacesMessage.SEVERITY_INFO, "Aprobada", "El Director TIC aprobó definitivamente la solicitud.");
    }

    public void dticEnviaOficial() {
        if (!isDirectorTIC() || !isEstadoPendienteDTIC()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Acción no permitida", "No corresponde.");
            return;
        }

        solicitud.setEstado("PENDIENTE OFICIAL");
        solCtrl.actualizar(solicitud);

        addMessage(FacesMessage.SEVERITY_INFO, "Enviado a Oficial", "El Oficial de Seguridad debe firmar.");
    }

    // ================================
    //          4. OFICIAL
    // ================================

    public void subirFirmadoOficial() {
        if (!isOficialSeguridad() || !isEstadoPendienteOficial()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Acción no permitida", "No corresponde.");
            return;
        }

        boolean ok = guardarArchivo(
                archivoFirmadoOficial,
                "solicitud_" + solicitud.getId() + "_firmada_oficial.pdf"
        );

        if (ok) {
            addMessage(FacesMessage.SEVERITY_INFO, "PDF cargado", "Firma del Oficial registrada.");
        }
    }

    public void oficialAprueba() {
        if (!isOficialSeguridad() || !isEstadoPendienteOficial()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Acción no permitida", "No corresponde.");
            return;
        }

        solicitud.setEstado("APROBADA");
        solCtrl.actualizar(solicitud);

        addMessage(FacesMessage.SEVERITY_INFO, "Solicitud aprobada", "El Oficial aprobó la solicitud.");
    }

    // ------------------------------
    // UTILIDAD PARA MENSAJES
    // ------------------------------

    private void addMessage(FacesMessage.Severity sev, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(sev, resumen, detalle));
    }

    // ------------------------------
    // GETTERS y SETTERS
    // ------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Solicitud getSolicitud() { return solicitud; }

    public UploadedFile getArchivoFirmado() { return archivoFirmado; }
    public void setArchivoFirmado(UploadedFile archivoFirmado) { this.archivoFirmado = archivoFirmado; }

    public UploadedFile getArchivoFirmadoDirector() { return archivoFirmadoDirector; }
    public void setArchivoFirmadoDirector(UploadedFile archivoFirmadoDirector) { this.archivoFirmadoDirector = archivoFirmadoDirector; }

    public UploadedFile getArchivoFirmadoDTIC() { return archivoFirmadoDTIC; }
    public void setArchivoFirmadoDTIC(UploadedFile archivoFirmadoDTIC) { this.archivoFirmadoDTIC = archivoFirmadoDTIC; }

    public UploadedFile getArchivoFirmadoOficial() { return archivoFirmadoOficial; }
    public void setArchivoFirmadoOficial(UploadedFile archivoFirmadoOficial) { this.archivoFirmadoOficial = archivoFirmadoOficial; }

    public Usuario getUsuarioActual() { return usuarioActual; }

    public String getFechaSolicitudFormateada() {
        if (solicitud != null && solicitud.getFechaCreacion() != null) {
            return solicitud.getFechaCreacion().format(fmt);
        }
        return "";
    }

    public Usuario getServidorSolicitante() {
        return (solicitud != null) ? solicitud.getUsuario() : null;
    }

    public List<AccesoUsuario> getAccesos() {
        return (solicitud != null) ? solicitud.getAccesos() : null;
    }

    public boolean tienePermiso(PermisoAplicacion p) {
        if (solicitud == null || solicitud.getAccesos() == null) return false;

        return solicitud.getAccesos().stream().anyMatch(
                au -> au.getPermiso() != null &&
                      au.getPermiso().getId().equals(p.getId())
        );
    }
}
