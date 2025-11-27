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

    // Lista de accesos para la vista/PDF
    private List<AccesoUsuario> accesos;

    // Usuario logueado
    private Usuario usuarioActual;

    // Archivo PDF firmado (se reutiliza para todos los roles)
    private UploadedFile archivoFirmado;

    // Formateadores
    private DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter fmtFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
    //  HELPERS DE ESTADO
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
        return solicitud != null && "PERMISOS APLICADOS".equalsIgnoreCase(solicitud.getEstado());
    }

    // =====================================================
    //  HELPERS DE ROL (según CARGO)
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

    public boolean isOficialSeguridadActual() {
        if (usuarioActual == null || usuarioActual.getCargo() == null) return false;
        return "Oficial Seguridad".equalsIgnoreCase(usuarioActual.getCargo().trim());
    }

    public boolean isResponsableAccesosActual() {
        if (usuarioActual == null || usuarioActual.getCargo() == null) return false;
        return "Responsable Accesos".equalsIgnoreCase(usuarioActual.getCargo().trim());
    }

    // =====================================================
    //  FLAGS PARA LA VISTA (rendered)
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
    //  SUBIDA DE PDF FIRMADO (DISTINTOS ACTORES)
    // =====================================================

    private Path getFirmasBasePath() throws IOException {
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

    private void guardarArchivoFirmado(String nombreArchivo) throws IOException {
        if (solicitud == null || solicitud.getId() == null) {
            throw new IOException("Solicitud no cargada, no se puede guardar el archivo.");
        }
        if (archivoFirmado == null || archivoFirmado.getFileName() == null
                || archivoFirmado.getFileName().isEmpty()) {
            throw new IOException("Debe seleccionar un archivo PDF firmado.");
        }

        try (InputStream in = archivoFirmado.getInputStream()) {
            Path dir = getFirmasBasePath();
            Path target = dir.resolve(nombreArchivo);
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // --- Solicitante (ya existía, pero ahora usa el helper) ---
    public void subirFirmado() {
        if (!isSolicitanteActual()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "Sólo el solicitante puede subir el archivo firmado.");
            return;
        }

        if (!isEstadoCreada()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "Sólo se puede cargar el archivo mientras la solicitud está en estado CREADA.");
            return;
        }

        try {
            String fileName = "solicitud_" + solicitud.getId() + "_firmada_usuario.pdf";
            guardarArchivoFirmado(fileName);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Archivo cargado",
                    "El archivo firmado del solicitante se ha guardado correctamente.");

        } catch (IOException e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al subir archivo",
                    e.getMessage());
        }
    }

    // --- Director ---
    public void subirFirmadoDirector() {
        if (!isDirectorPuedeFirmar()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "Sólo el Director puede subir el PDF firmado en este estado.");
            return;
        }

        try {
            String fileName = "solicitud_" + solicitud.getId() + "_firmada_director.pdf";
            guardarArchivoFirmado(fileName);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Archivo cargado",
                    "El archivo firmado por el Director se ha guardado correctamente.");

        } catch (IOException e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al subir archivo del Director",
                    e.getMessage());
        }
    }

    // --- Director TIC ---
    public void subirFirmadoDirectorTic() {
        if (!isDirectorTicPuedeFirmar()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "Sólo el Director TIC puede subir el PDF firmado en este estado.");
            return;
        }

        try {
            String fileName = "solicitud_" + solicitud.getId() + "_firmada_director_tic.pdf";
            guardarArchivoFirmado(fileName);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Archivo cargado",
                    "El archivo firmado por el Director TIC se ha guardado correctamente.");

        } catch (IOException e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al subir archivo del Director TIC",
                    e.getMessage());
        }
    }

    // --- Oficial de Seguridad ---
    public void subirFirmadoOficial() {
        if (!isOficialPuedeFirmar()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "Sólo el Oficial de Seguridad puede subir el PDF firmado en este estado.");
            return;
        }

        try {
            String fileName = "solicitud_" + solicitud.getId() + "_firmada_oficial_seguridad.pdf";
            guardarArchivoFirmado(fileName);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Archivo cargado",
                    "El archivo firmado por el Oficial de Seguridad se ha guardado correctamente.");

        } catch (IOException e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al subir archivo del Oficial de Seguridad",
                    e.getMessage());
        }
    }

    // --- Responsable de Accesos ---
    public void subirFirmadoResponsable() {
        if (!isResponsablePuedeAplicarPermisos()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "Sólo el Responsable de Accesos puede subir el PDF firmado en este estado.");
            return;
        }

        try {
            String fileName = "solicitud_" + solicitud.getId() + "_firmada_responsable_accesos.pdf";
            guardarArchivoFirmado(fileName);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Archivo cargado",
                    "El archivo firmado por el Responsable de Accesos se ha guardado correctamente.");

        } catch (IOException e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al subir archivo del Responsable de Accesos",
                    e.getMessage());
        }
    }

    // =====================================================
    //  DIRECTOR / DIRECTOR TIC / OFICIAL / RESPONSABLE
    //  (firma lógica en BD: NO se toca flujo original)
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

        if (solicitud == null || solicitud.getId() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Solicitud no cargada",
                    "No se pudo identificar la solicitud.");
            return;
        }

        try {
            String estadoAnterior = solicitud.getEstado();
            solicitud.setEstado("PENDIENTE DIRECTOR");
            solCtrl.actualizar(solicitud);

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
            solCtrl.actualizar(solicitud);

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
            solCtrl.actualizar(solicitud);

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
            solCtrl.actualizar(solicitud);

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
            solCtrl.actualizar(solicitud);

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
            solCtrl.actualizar(solicitud);

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
            solCtrl.actualizar(solicitud);

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

    public void marcarPermisosAplicados() {
        if (!isResponsablePuedeAplicarPermisos()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Acción no permitida",
                    "Sólo el Responsable de Accesos puede marcar los permisos como aplicados.");
            return;
        }

        try {
            String estadoAnterior = solicitud.getEstado();

            Firma f = new Firma();
            f.setSolicitud(solicitud);
            f.setDescripcion("Permisos aplicados en sistemas reales por Responsable de Accesos: "
                    + usuarioActual.getNombre() + " " + usuarioActual.getApellido());
            f.setFechaFirma(LocalDateTime.now());
            solicitud.getFirmas().add(f);

            solicitud.setEstado("PERMISOS APLICADOS");
            solCtrl.actualizar(solicitud);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Permisos aplicados",
                    "La solicitud ha sido marcada como PERMISOS APLICADOS.");

            NotificacionService.notificarCambioEstado(solicitud, estadoAnterior);

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

    // --- Helpers para saber si existen PDFs firmados (para mostrar enlaces) ---

    private boolean existeArchivoPdf(String nombreArchivo) {
        try {
            Path dir = getFirmasBasePath();
            Path target = dir.resolve(nombreArchivo);
            return Files.exists(target);
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isPdfSolicitanteSubido() {
        if (solicitud == null || solicitud.getId() == null) return false;
        String fileName = "solicitud_" + solicitud.getId() + "_firmada_usuario.pdf";
        return existeArchivoPdf(fileName);
    }

    public boolean isPdfDirectorSubido() {
        if (solicitud == null || solicitud.getId() == null) return false;
        String fileName = "solicitud_" + solicitud.getId() + "_firmada_director.pdf";
        return existeArchivoPdf(fileName);
    }

    public boolean isPdfDirectorTicSubido() {
        if (solicitud == null || solicitud.getId() == null) return false;
        String fileName = "solicitud_" + solicitud.getId() + "_firmada_director_tic.pdf";
        return existeArchivoPdf(fileName);
    }

    public boolean isPdfOficialSubido() {
        if (solicitud == null || solicitud.getId() == null) return false;
        String fileName = "solicitud_" + solicitud.getId() + "_firmada_oficial_seguridad.pdf";
        return existeArchivoPdf(fileName);
    }

    public boolean isPdfResponsableSubido() {
        if (solicitud == null || solicitud.getId() == null) return false;
        String fileName = "solicitud_" + solicitud.getId() + "_firmada_responsable_accesos.pdf";
        return existeArchivoPdf(fileName);
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
