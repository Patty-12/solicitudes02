package com.senadi.solicitud02.vista;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import com.senadi.solicitud02.controlador.SolicitudControlador;
import com.senadi.solicitud02.controlador.AccesoUsuarioControlador;
import com.senadi.solicitud02.controlador.impl.SolicitudControladorImpl;
import com.senadi.solicitud02.controlador.impl.AccesoUsuarioControladorImpl;
import com.senadi.solicitud02.modelo.entidades.AccesoUsuario;
import com.senadi.solicitud02.modelo.entidades.Firma;
import com.senadi.solicitud02.modelo.entidades.PermisoAplicacion;
import com.senadi.solicitud02.modelo.entidades.Solicitud;
import com.senadi.solicitud02.modelo.entidades.Usuario;
import com.senadi.solicitud02.servicio.NotificacionService;

@ManagedBean(name = "solicitudDetalleBean")
@ViewScoped
public class SolicitudDetalleBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;                 // id de la solicitud (viewParam)
    private Solicitud solicitud;

    private SolicitudControlador solCtrl = new SolicitudControladorImpl();
    private AccesoUsuarioControlador accesoCtrl = new AccesoUsuarioControladorImpl();

    // Accesos a permisos
    private List<AccesoUsuario> accesos;

    // Usuario logueado
    private Usuario usuarioActual;

    // ÚNICO campo de subida: todos los roles usan esto
    private UploadedFile archivoFirmado;

    // Formateadores
    private DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter fmtFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ===================== CICLO DE VIDA =====================

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

    // Verificar sesión
    public void verificarSesion(ComponentSystemEvent event) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (usuarioActual == null) {
            try {
                String ctxPath = fc.getExternalContext().getRequestContextPath();
                fc.getExternalContext().redirect(ctxPath + "/index.xhtml");
                fc.responseComplete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Cargar solicitud + accesos
    public void cargar() {
        if (id == null) {
            String param = FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getRequestParameterMap()
                    .get("id");
            if (param != null && !param.isEmpty()) {
                try {
                    id = Long.parseLong(param);
                } catch (NumberFormatException e) {
                    id = null;
                }
            }
        }

        if (id != null) {
            solicitud = solCtrl.buscarPorId(id);

            if (solicitud != null && solicitud.getId() != null) {
                accesos = accesoCtrl.listarPorSolicitud(solicitud.getId());
            } else {
                accesos = null;
            }
        }
    }

    // =====================================================
    //  ESTADOS
    // =====================================================

    public boolean isEstadoCreada() {
        return solicitud != null && "CREADA".equalsIgnoreCase(solicitud.getEstado());
    }

    public boolean isEstadoPendienteDirector() {
        return solicitud != null && "PENDIENTE DIRECTOR".equalsIgnoreCase(solicitud.getEstado());
    }

    public boolean isEstadoPendienteDirectorTic() {
        return solicitud != null && "PENDIENTE DIRECTOR TIC".equalsIgnoreCase(solicitud.getEstado());
    }

    public boolean isEstadoPendienteOficialSeguridad() {
        return solicitud != null && "PENDIENTE OFICIAL SEGURIDAD".equalsIgnoreCase(solicitud.getEstado());
    }

    public boolean isEstadoPendienteAplicacionAccesos() {
        return solicitud != null && "PENDIENTE RESPONSABLE ACCESOS".equalsIgnoreCase(solicitud.getEstado());
    }

    public boolean isEstadoPermisosAplicados() {
        return solicitud != null && "APLICADO PERMISOS".equalsIgnoreCase(solicitud.getEstado());
    }

    // =====================================================
    //  ROLES (según CARGO)
    // =====================================================

    public boolean isSolicitanteActual() {
        if (solicitud == null || solicitud.getUsuario() == null || usuarioActual == null) {
            return false;
        }
        return solicitud.getUsuario().getId().equals(usuarioActual.getId());
    }

    public boolean isDirectorActual() {
        if (usuarioActual == null || usuarioActual.getCargo() == null) return false;
        return "Director".equalsIgnoreCase(usuarioActual.getCargo().trim());
    }

    public boolean isDirectorTicActual() {
        if (usuarioActual == null || usuarioActual.getCargo() == null) return false;
        return "Director TIC".equalsIgnoreCase(usuarioActual.getCargo().trim());
    }

    /**
     * Oficial de Seguridad actual.
     *
     * HOY: el mismo Director TIC actúa también como Oficial de Seguridad,
     * por eso se permite ambos cargos.
     *
     * A FUTURO: si se designa un Oficial distinto, dejar solo:
     *   return "Oficial Seguridad".equalsIgnoreCase(cargo);
     */
    public boolean isOficialSeguridadActual() {
        if (usuarioActual == null || usuarioActual.getCargo() == null) return false;
        String cargo = usuarioActual.getCargo().trim();
        return "Oficial Seguridad".equalsIgnoreCase(cargo)
                || "Director TIC".equalsIgnoreCase(cargo); // <-- ajustar en el futuro si se separan roles
    }

    public boolean isResponsableAccesosActual() {
        if (usuarioActual == null || usuarioActual.getCargo() == null) return false;
        return "Responsable Accesos".equalsIgnoreCase(usuarioActual.getCargo().trim());
    }

    // =====================================================
    //  FLAGS PARA LA VISTA
    // =====================================================

    public boolean isPuedeEnviarADirector() {
        return isSolicitanteActual() && isEstadoCreada();
    }

    public boolean isMostrarAccionesSolicitante() {
        return isPuedeEnviarADirector();
    }

    public boolean isDirectorPuedeFirmar() {
        return isDirectorActual() && isEstadoPendienteDirector();
    }

    public boolean isDirectorTicPuedeFirmar() {
        return isDirectorTicActual() && isEstadoPendienteDirectorTic();
    }

    public boolean isOficialPuedeFirmar() {
        return isOficialSeguridadActual() && isEstadoPendienteOficialSeguridad();
    }

    public boolean isResponsablePuedeAplicarPermisos() {
        return isResponsableAccesosActual() && isEstadoPendienteAplicacionAccesos();
    }

    public boolean isPuedeDescargarImprimirPdf() {
        if (solicitud == null) return false;

        if (isSolicitanteActual() && isEstadoCreada()) return true;
        if (isDirectorActual() && isEstadoPendienteDirector()) return true;
        if (isDirectorTicActual() && isEstadoPendienteDirectorTic()) return true;
        if (isOficialSeguridadActual() && isEstadoPendienteOficialSeguridad()) return true;
        if (isResponsableAccesosActual() && isEstadoPendienteAplicacionAccesos()) return true;

        return false;
    }

    // =====================================================
    //  MANEJO DE ARCHIVOS /firmas (UN SOLO PDF FIRMADO)
    // =====================================================

    private Path getDirectorioFirmas() throws IOException {
        String basePath = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRealPath("/firmas");
        if (basePath == null) {
            throw new IOException("No se pudo determinar la ruta de almacenamiento (/firmas).");
        }
        Path dir = Paths.get(basePath);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        return dir;
    }

    /** Nombre fijo: formulario_accesos_00000001_firmado.pdf */
    private String buildNombreArchivoFirmado() {
        if (solicitud == null || solicitud.getId() == null) {
            return null;
        }
        return String.format("formulario_accesos_%08d_firmado.pdf", solicitud.getId());
    }

    private void guardarArchivo(UploadedFile file, String nombreArchivo) throws IOException {
        if (file == null || file.getFileName() == null || file.getFileName().isEmpty()) {
            throw new IOException("Debe seleccionar un archivo PDF firmado.");
        }

        try (InputStream in = file.getInputStream()) {
            Path dir = getDirectorioFirmas();
            Path target = dir.resolve(nombreArchivo);
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // =====================================================
    //  SUBIR PDF FIRMADO (se usa desde el listener del fileUpload)
    // =====================================================

    public void subirFirmadoListener(FileUploadEvent event) {
        this.archivoFirmado = event.getFile();
        subirFirmado();
    }

    public void subirFirmado() {
        if (solicitud == null || solicitud.getId() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Solicitud no cargada",
                    "No se pudo identificar la solicitud.");
            return;
        }

        if (archivoFirmado == null) {
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Archivo requerido",
                    "Debe seleccionar un archivo PDF firmado.");
            return;
        }

        // Validar que el rol/estado actual pueda subir
        if (!((isSolicitanteActual() && isEstadoCreada())
                || isDirectorPuedeFirmar()
                || isDirectorTicPuedeFirmar()
                || isOficialPuedeFirmar()
                || isResponsablePuedeAplicarPermisos())) {

            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "No tiene permitido subir un PDF firmado en este estado.");
            return;
        }

        try {
            // Guardar siempre con el mismo nombre (última versión firmada)
            String nombre = buildNombreArchivoFirmado();
            guardarArchivo(archivoFirmado, nombre);

            String ctx = FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getRequestContextPath();
            String url = ctx + "/firmas/" + nombre;

            // Actualizamos la URL en la entidad Solicitud
            solicitud.setUrlPdfFirmado(url);
            solicitud = solCtrl.actualizar(solicitud); // merge y refresco

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Archivo cargado",
                    "Se ha almacenado correctamente el PDF firmado.");

        } catch (IOException e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al subir archivo",
                    e.getMessage());
        }
    }

    // =====================================================
    //  FLUJO DE ESTADOS / FIRMAS
    // =====================================================

    public void enviarAlDirector() {
        if (!isSolicitanteActual()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "Sólo el solicitante puede enviar la solicitud al Director.");
            return;
        }

        if (!isEstadoCreada()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "La solicitud ya no está en estado CREADA.");
            return;
        }

        // Verificar que ya exista PDF firmado por el solicitante
        if (solicitud.getUrlPdfFirmado() == null || solicitud.getUrlPdfFirmado().trim().isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "PDF pendiente",
                    "Antes de enviar al Director debe subir el PDF firmado por el solicitante.");
            return;
        }

        if (solicitud == null || solicitud.getId() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Solicitud no cargada",
                    "No se pudo identificar la solicitud.");
            return;
        }

        try {
            String estadoAnterior = solicitud.getEstado();
            solicitud.setEstado("PENDIENTE DIRECTOR");
            solicitud = solCtrl.actualizar(solicitud);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Solicitud enviada",
                    "La solicitud ha sido enviada al Director para su revisión.");

            String correoDirector = "dlopez@senadi.com";
            NotificacionService.notificarPendienteDirector(solicitud, correoDirector);
            NotificacionService.notificarSolicitanteEnvio(solicitud);
            NotificacionService.notificarCambioEstado(solicitud, estadoAnterior);

        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al cambiar estado",
                    e.getMessage());
        }
    }

    public void aprobarComoDirector() {
        if (!isDirectorPuedeFirmar()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "Sólo un Director puede aprobar una solicitud en estado PENDIENTE DIRECTOR.");
            return;
        }

        try {
            String estadoAnterior = solicitud.getEstado();

            Firma f = new Firma();
            f.setSolicitud(solicitud);
            f.setDescripcion("Aprobada por Director: "
                    + usuarioActual.getNombre() + " " + usuarioActual.getApellido());
            f.setFechaFirma(LocalDateTime.now());
            solicitud.getFirmas().add(f);

            solicitud.setEstado("PENDIENTE DIRECTOR TIC");
            solicitud = solCtrl.actualizar(solicitud);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Solicitud aprobada",
                    "La solicitud ha sido aprobada por el Director y enviada al Director TIC.");

            NotificacionService.notificarCambioEstado(solicitud, estadoAnterior);

        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al aprobar solicitud",
                    e.getMessage());
        }
    }

    public void rechazarComoDirector() {
        if (!isDirectorPuedeFirmar()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "Sólo un Director puede rechazar una solicitud en estado PENDIENTE DIRECTOR.");
            return;
        }

        try {
            String estadoAnterior = solicitud.getEstado();
            solicitud.setEstado("RECHAZADA");
            solicitud = solCtrl.actualizar(solicitud);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Solicitud rechazada",
                    "La solicitud ha sido rechazada por el Director.");

            NotificacionService.notificarCambioEstado(solicitud, estadoAnterior);

        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al rechazar solicitud", e.getMessage());
        }
    }

    public void aprobarComoDirectorTic() {
        if (!isDirectorTicPuedeFirmar()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "Sólo el Director TIC puede aprobar una solicitud en estado PENDIENTE DIRECTOR TIC.");
            return;
        }

        try {
            String estadoAnterior = solicitud.getEstado();

            Firma f = new Firma();
            f.setSolicitud(solicitud);
            f.setDescripcion("Validada técnicamente por Director TIC: "
                    + usuarioActual.getNombre() + " " + usuarioActual.getApellido());
            f.setFechaFirma(LocalDateTime.now());
            solicitud.getFirmas().add(f);

            solicitud.setEstado("PENDIENTE OFICIAL SEGURIDAD");
            solicitud = solCtrl.actualizar(solicitud);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Solicitud aprobada por Director TIC",
                    "La solicitud ha sido enviada al Oficial de Seguridad para su revisión.");

            NotificacionService.notificarCambioEstado(solicitud, estadoAnterior);

        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al aprobar como Director TIC", e.getMessage());
        }
    }

    public void rechazarComoDirectorTic() {
        if (!isDirectorTicPuedeFirmar()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "Sólo el Director TIC puede rechazar una solicitud en estado PENDIENTE DIRECTOR TIC.");
            return;
        }

        try {
            String estadoAnterior = solicitud.getEstado();
            solicitud.setEstado("RECHAZADA");
            solicitud = solCtrl.actualizar(solicitud);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Solicitud rechazada por Director TIC",
                    "La solicitud ha sido rechazada en la validación técnica.");

            NotificacionService.notificarCambioEstado(solicitud, estadoAnterior);

        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al rechazar como Director TIC", e.getMessage());
        }
    }

    public void aprobarComoOficial() {
        if (!isOficialPuedeFirmar()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "Sólo el Oficial de Seguridad puede aprobar una solicitud en estado PENDIENTE OFICIAL SEGURIDAD.");
            return;
        }

        try {
            String estadoAnterior = solicitud.getEstado();

            Firma f = new Firma();
            f.setSolicitud(solicitud);
            f.setDescripcion("Aprobada por Oficial de Seguridad: "
                    + usuarioActual.getNombre() + " " + usuarioActual.getApellido());
            f.setFechaFirma(LocalDateTime.now());
            solicitud.getFirmas().add(f);

            solicitud.setEstado("PENDIENTE RESPONSABLE ACCESOS");
            solicitud = solCtrl.actualizar(solicitud);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Solicitud aprobada por Oficial de Seguridad",
                    "La solicitud ha sido enviada al Responsable de Accesos.");

            NotificacionService.notificarCambioEstado(solicitud, estadoAnterior);

        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al aprobar como Oficial de Seguridad", e.getMessage());
        }
    }

    public void rechazarComoOficial() {
        if (!isOficialPuedeFirmar()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "Sólo el Oficial de Seguridad puede rechazar una solicitud en estado PENDIENTE OFICIAL SEGURIDAD.");
            return;
        }

        try {
            String estadoAnterior = solicitud.getEstado();
            solicitud.setEstado("RECHAZADA");
            solicitud = solCtrl.actualizar(solicitud);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Solicitud rechazada por Oficial de Seguridad",
                    "La solicitud ha sido rechazada por Seguridad de la Información.");

            NotificacionService.notificarCambioEstado(solicitud, estadoAnterior);

        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al rechazar como Oficial de Seguridad", e.getMessage());
        }
    }

    /**
     * Responsable de Accesos marca los permisos como aplicados.
     * Deja el estado final en "APLICADO PERMISOS", igual que la vista
     * de "Permisos por aplicar", para que siempre salga en Reportes.
     */
    public void marcarPermisosAplicados() {
        if (!isResponsablePuedeAplicarPermisos()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "Sólo el Responsable de Accesos puede marcar los permisos como aplicados.");
            return;
        }

        try {
            if (solicitud == null || solicitud.getId() == null) {
                addMessage(FacesMessage.SEVERITY_ERROR,
                        "Solicitud no cargada",
                        "No se pudo identificar la solicitud.");
                return;
            }

            // Refrescar desde BD
            Solicitud sDb = solCtrl.buscarPorId(solicitud.getId());
            if (sDb == null) {
                addMessage(FacesMessage.SEVERITY_ERROR,
                        "Solicitud no encontrada",
                        "La solicitud ya no existe en la base de datos.");
                return;
            }

            String estadoAnterior = (sDb.getEstado() != null)
                    ? sDb.getEstado().toUpperCase()
                    : "";

            // Sólo permitir si viene de PENDIENTE RESPONSABLE ACCESOS o APROBADA
            if (!"PENDIENTE RESPONSABLE ACCESOS".equals(estadoAnterior)
                    && !"APROBADA".equals(estadoAnterior)) {

                addMessage(FacesMessage.SEVERITY_WARN,
                        "No permitido",
                        "Sólo se pueden marcar como aplicados los permisos de solicitudes aprobadas y pendientes de aplicación.");
                return;
            }

            // Registrar firma
            Firma f = new Firma();
            f.setSolicitud(sDb);
            f.setDescripcion("Permisos aplicados en sistemas reales por Responsable de Accesos: "
                    + usuarioActual.getNombre() + " " + usuarioActual.getApellido());
            f.setFechaFirma(LocalDateTime.now());
            sDb.getFirmas().add(f);

            // Estado final unificado
            sDb.setEstado("APLICADO PERMISOS");

            // Guardar y sincronizar con el bean
            sDb = solCtrl.actualizar(sDb);
            this.solicitud = sDb;

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Permisos aplicados",
                    "La solicitud ha sido marcada como APLICADO PERMISOS.");

            NotificacionService.notificarCambioEstado(sDb, estadoAnterior);

        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al marcar permisos aplicados", e.getMessage());
        }
    }

    // =====================================================
    //  UTILIDADES
    // =====================================================

    private void addMessage(FacesMessage.Severity sev, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(sev, resumen, detalle));
    }

    public String getFechaSolicitudFormateada() {
        if (solicitud != null && solicitud.getFechaCreacion() != null) {
            return solicitud.getFechaCreacion().format(fmtFecha);
        }
        return "";
    }

    public String formatearFechaHora(LocalDateTime dt) {
        return (dt != null) ? dt.format(fmtFechaHora) : "";
    }

    public Usuario getServidorSolicitante() {
        return (solicitud != null) ? solicitud.getUsuario() : null;
    }

    public Usuario getServidorAutoriza() {
        return (solicitud != null) ? solicitud.getJefeAutoriza() : null;
    }

    public List<AccesoUsuario> getAccesos() {
        if (accesos == null && solicitud != null && solicitud.getId() != null) {
            accesos = accesoCtrl.listarPorSolicitud(solicitud.getId());
        }
        return accesos;
    }

    public boolean tienePermiso(PermisoAplicacion permiso) {
        List<AccesoUsuario> lista = getAccesos();
        if (lista == null || permiso == null) return false;
        for (AccesoUsuario au : lista) {
            if (au.getPermiso() != null && au.getPermiso().getId().equals(permiso.getId())) {
                return true;
            }
        }
        return false;
    }

    public List<Firma> getFirmasOrdenadas() {
        if (solicitud == null || solicitud.getFirmas() == null) {
            return new ArrayList<>();
        }
        List<Firma> lista = new ArrayList<>(solicitud.getFirmas());
        lista.sort(Comparator.comparing(Firma::getFechaFirma));
        return lista;
    }

    // ====== URL para la vista de PDF en el iframe ======

    /**
     * URL que usará el iframe para vista previa.
     * Siempre muestra la última versión firmada.
     */
    public String getUrlPdfActual() {
        return (solicitud != null) ? solicitud.getUrlPdfFirmado() : null;
    }

    // ================= GETTERS / SETTERS =================

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public UploadedFile getArchivoFirmado() {
        return archivoFirmado;
    }
    public void setArchivoFirmado(UploadedFile archivoFirmado) {
        this.archivoFirmado = archivoFirmado;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    // Expuestos para EL
    public boolean isMostrarAccionesSolicitantePublic() {
        return isMostrarAccionesSolicitante();
    }

    public boolean isDirectorPuedeFirmarPublic() {
        return isDirectorPuedeFirmar();
    }

    public boolean isDirectorTicPuedeFirmarPublic() {
        return isDirectorTicPuedeFirmar();
    }

    public boolean isOficialPuedeFirmarPublic() {
        return isOficialPuedeFirmar();
    }

    public boolean isResponsablePuedeAplicarPermisosPublic() {
        return isResponsablePuedeAplicarPermisos();
    }

    public boolean isPuedeDescargarImprimirPdfPublic() {
        return isPuedeDescargarImprimirPdf();
    }
}
