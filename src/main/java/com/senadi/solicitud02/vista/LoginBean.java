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

    // AHORA: usuario + contraseña
    private String username;
    private String password;
    private Usuario usuario;

    private final UsuarioControlador usuarioCtrl = new UsuarioControladorImpl();

    /**
     * para activar/desactivar autenticación LDAP.
     * Por defecto: false → sólo BD local.
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

                // En LDAP normalmente se usa el mismo username
                String userLDAP = username;

                boolean okLDAP = ldap.validarIngresoLDAPSinrestrinccion(userLDAP, password);

                if (!okLDAP) {
                    fc.addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Credenciales inválidas (LDAP)",
                            "No fue posible autenticar contra el directorio corporativo."
                    ));
                    return null;
                }

                // Si LDAP autenticó, buscamos usuario en nuestra BD
                usuario = usuarioCtrl.autenticar(username, password);
                // Si quisieras que la BD no valide password cuando viene de LDAP,
                // podrías usar un método tipo: usuarioCtrl.buscarPorUsername(username).

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
                usuario = usuarioCtrl.autenticar(username, password);

                if (usuario == null) {
                    fc.addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Credenciales inválidas",
                            "Verifica usuario y contraseña."
                    ));
                    return null;
                }
            }

            // Guardar también en SessionMap para otros beans
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
    public void verificarSesion(ComponentSystemEvent event) {
        FacesContext fc = FacesContext.getCurrentInstance();

        if (usuario == null) {
            try {
                String ctxPath = fc.getExternalContext().getRequestContextPath();
                fc.getExternalContext().redirect(ctxPath + "/index.xhtml");
                fc.responseComplete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ---------- Logout ----------
    public String logout() {
        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            fc.getExternalContext().invalidateSession();

            String ctxPath = fc.getExternalContext().getRequestContextPath();
            fc.getExternalContext().redirect(ctxPath + "/index.xhtml");

            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ---------- Getters / Setters ----------

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
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
