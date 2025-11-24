package com.senadi.solicitud02.servicio;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.senadi.solicitud02.modelo.entidades.Solicitud;
import com.senadi.solicitud02.modelo.entidades.Usuario;

public class NotificacionService {

    // Ajusta estos valores a la cuenta de correo real que usará el sistema
    private static final String SMTP_HOST = "mail.senadi.gob.ec";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_USER = "notificaciones@senadi.gob.ec"; // TODO: reemplazar
    private static final String SMTP_PASS = "CAMBIAR_PASSWORD";             // TODO: reemplazar

    /**
     * Crea y configura la sesión SMTP (Zimbra).
     */
    private static Session crearSesion() {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });
    }

    /**
     * Envía un correo simple (texto plano).
     */
    private static void enviarCorreo(String destinatario, String asunto, String contenido) {
        try {
            Session session = crearSesion();

            Message mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(SMTP_USER));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject(asunto);
            mensaje.setText(contenido);

            Transport.send(mensaje);

            System.out.println("[EMAIL ENVIADO] → " + destinatario);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("[ERROR EMAIL] " + e.getMessage());
        }
    }

    /**
     * Correo de prueba para validar configuración SMTP.
     */
    public static void enviarCorreoPrueba(String destinatario) {
        if (destinatario == null || destinatario.trim().isEmpty()) {
            System.err.println("[EMAIL PRUEBA] destinatario vacío, no se envía.");
            return;
        }

        String asunto = "[SENADI] Prueba de notificación";
        String mensaje = "Este es un correo de prueba generado por el sistema de solicitudes.\n\n"
                       + "Destinatario: " + destinatario + "\n"
                       + "Si ves este mensaje, la configuración SMTP está funcionando.";

        enviarCorreo(destinatario, asunto, mensaje);
    }

    /**
     * Notifica al Director que tiene una solicitud pendiente.
     */
    public static void notificarPendienteDirector(Solicitud solicitud, String correoDirector) {
        if (correoDirector == null || correoDirector.isEmpty() || solicitud == null) {
            return;
        }

        String asunto = "[SENADI] Solicitud pendiente de revisión";
        String mensaje = "Estimado Director,\n\n"
                + "Tiene una solicitud pendiente de revisión.\n\n"
                + "ID de solicitud: " + solicitud.getId() + "\n"
                + "Estado actual: " + solicitud.getEstado() + "\n\n"
                + "Este mensaje fue generado automáticamente.";

        enviarCorreo(correoDirector, asunto, mensaje);
    }

    /**
     * Notifica al solicitante que su solicitud fue enviada al Director.
     */
    public static void notificarSolicitanteEnvio(Solicitud solicitud) {
        if (solicitud == null) {
            return;
        }

        Usuario u = solicitud.getUsuario();
        if (u == null || u.getCorreo() == null || u.getCorreo().isEmpty()) {
            return;
        }

        String asunto = "[SENADI] Su solicitud fue enviada al Director";
        String mensaje = "Estimado(a) " + u.getNombre() + " " + u.getApellido() + ",\n\n"
                + "Su solicitud ha sido enviada al Director para su revisión.\n\n"
                + "ID de solicitud: " + solicitud.getId() + "\n"
                + "Estado actual: " + solicitud.getEstado() + "\n\n"
                + "Este mensaje fue generado automáticamente.";

        enviarCorreo(u.getCorreo(), asunto, mensaje);
    }

    /**
     * Notifica al solicitante que la solicitud cambió de estado
     * (por ejemplo: APROBADA, RECHAZADA, ANULADA).
     */
    public static void notificarCambioEstado(Solicitud solicitud, String estadoAnterior) {
        if (solicitud == null) {
            return;
        }

        Usuario u = solicitud.getUsuario();
        if (u == null || u.getCorreo() == null || u.getCorreo().isEmpty()) {
            return;
        }

        String asunto = "[SENADI] Actualización de estado de solicitud";
        String mensaje = "Estimado(a) " + u.getNombre() + " " + u.getApellido() + ",\n\n"
                + "Su solicitud ha cambiado de estado.\n\n"
                + "ID de solicitud: " + solicitud.getId() + "\n"
                + "Estado anterior: " + estadoAnterior + "\n"
                + "Estado nuevo: " + solicitud.getEstado() + "\n\n"
                + "Este mensaje fue generado automáticamente.";

        enviarCorreo(u.getCorreo(), asunto, mensaje);
    }
}
