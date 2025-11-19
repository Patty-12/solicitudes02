package com.senadi.solicitud02.vista;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;

import com.senadi.solicitud02.controlador.UsuarioControlador;
import com.senadi.solicitud02.controlador.impl.UsuarioControladorImpl;
import com.senadi.solicitud02.modelo.entidades.Usuario;

@ManagedBean(name = "loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String correo;
    private String password;
    private Usuario usuario;

    private final UsuarioControlador usuarioCtrl = new UsuarioControladorImpl();

    // ---------- Login ----------
    public String login() {
        usuario = usuarioCtrl.autenticar(correo, password);

        if (usuario != null) {
            // Guardar también en el SessionMap para otros beans (SolicitudBean usa "usuarioLogueado")
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getSessionMap().put("usuarioLogueado", usuario);

            // Ir al home
            return "home?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Credenciales inválidas", "Verifica correo y contraseña"));
            return null;
        }
    }

    // ---------- Verificar sesión antes de renderizar vistas protegidas ----------
    // Firma compatible con <f:event type="preRenderView" ...>
    public void verificarSesion(ComponentSystemEvent event) {
        FacesContext fc = FacesContext.getCurrentInstance();

        if (usuario == null) {
            try {
                String ctxPath = fc.getExternalContext().getRequestContextPath();
                fc.getExternalContext().redirect(ctxPath + "/index.xhtml");
                fc.responseComplete(); // Evita que JSF siga renderizando
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ---------- Logout ----------
    public String logout() {
        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            // invalidar sesión
            fc.getExternalContext().invalidateSession();

            // redirigir manualmente al login
            String ctxPath = fc.getExternalContext().getRequestContextPath();
            fc.getExternalContext().redirect(ctxPath + "/index.xhtml");

            return null; // ya hicimos redirect
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ---------- Getters / Setters ----------
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Usuario getUsuario() { return usuario; }
}
