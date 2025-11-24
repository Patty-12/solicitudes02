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
import com.senadi.solicitud02.modelo.util.LDAP;

@ManagedBean(name = "loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String correo;
    private String password;
    private Usuario usuario;

    private final UsuarioControlador usuarioCtrl = new UsuarioControladorImpl();

    /**
     *  para activar/desactivar autenticación LDAP.
     * Por defecto: false → sólo BD local.
     * Cuando se confirme el LDAP,
     * poner esto en true o controlarlo vía configuración. 
     */
    private boolean usarLDAP = false;

    // ---------- Login ----------
    public String login() {

        FacesContext fc = FacesContext.getCurrentInstance();

        try {
            if (usarLDAP) {
                // =========================
                //   AUTENTICACIÓN VÍA LDAP
                // =========================
                LDAP ldap = new LDAP();

                // Si el usuario escribe el correo completo, tomamos la parte antes de '@'
                String userLDAP = correo;
                if (correo != null && correo.contains("@")) {
                    userLDAP = correo.substring(0, correo.indexOf('@'));
                }

                boolean okLDAP = ldap.validarIngresoLDAPSinrestrinccion(userLDAP, password);

                if (!okLDAP) {
                    fc.addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Credenciales inválidas (LDAP)",
                            "No fue posible autenticar contra el directorio corporativo."
                    ));
                    return null;
                }

                // Si llegó aquí, LDAP autenticó.
                // Ahora buscamos al usuario en nuestra BD de solicitudes.
                // Opción sencilla: buscar por correo exactamente como lo digita.
                usuario = usuarioCtrl.autenticar(correo, password);
                // Si en el futuro la contraseña local no coincide con la de LDAP,
                // aquí puedes cambiar la lógica a un "buscarPorCorreo(correo)".

                if (usuario == null) {
                    fc.addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Usuario no registrado",
                            "Está autenticado en el dominio, pero no existe en el sistema de solicitudes."
                    ));
                    return null;
                }

            } else {
                // =======================================
                //   AUTENTICACIÓN SOLO CONTRA LA BD LOCAL
                // =======================================
                usuario = usuarioCtrl.autenticar(correo, password);

                if (usuario == null) {
                    fc.addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Credenciales inválidas",
                            "Verifica correo y contraseña."
                    ));
                    return null;
                }
            }

            // Guardar también en SessionMap para otros beans (SolicitudBean usa "usuarioLogueado")
            fc.getExternalContext().getSessionMap().put("usuarioLogueado", usuario);

            // Ir al home
            return "home?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            fc.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error en login",
                    e.getMessage()
            ));
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

    public String getCorreo() {
        return correo;
    }
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public boolean isUsarLDAP() {
        return usarLDAP;
    }
    public void setUsarLDAP(boolean usarLDAP) {
        this.usarLDAP = usarLDAP;
    }
}
