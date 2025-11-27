package solicitud02;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import com.senadi.solicitud02.controlador.AplicacionControlador;
import com.senadi.solicitud02.controlador.PermisoAplicacionControlador;
import com.senadi.solicitud02.controlador.AccesoUsuarioControlador;
import com.senadi.solicitud02.controlador.SolicitudControlador;
import com.senadi.solicitud02.controlador.UsuarioControlador;
import com.senadi.solicitud02.controlador.impl.AplicacionControladorImpl;
import com.senadi.solicitud02.controlador.impl.PermisoAplicacionControladorImpl;
import com.senadi.solicitud02.controlador.impl.AccesoUsuarioControladorImpl;
import com.senadi.solicitud02.controlador.impl.SolicitudControladorImpl;
import com.senadi.solicitud02.controlador.impl.UsuarioControladorImpl;
import com.senadi.solicitud02.modelo.entidades.Aplicacion;
import com.senadi.solicitud02.modelo.entidades.PermisoAplicacion;
import com.senadi.solicitud02.modelo.entidades.AccesoUsuario;
import com.senadi.solicitud02.modelo.entidades.Solicitud;
import com.senadi.solicitud02.modelo.entidades.Usuario;

import java.util.List;

public class TestAccesoUsuario {

    private static final UsuarioControlador usuarioCtrl = new UsuarioControladorImpl();
    private static final SolicitudControlador solicitudCtrl = new SolicitudControladorImpl();
    private static final AplicacionControlador appCtrl = new AplicacionControladorImpl();
    private static final PermisoAplicacionControlador permisoCtrl = new PermisoAplicacionControladorImpl();
    private static final AccesoUsuarioControlador accesoCtrl = new AccesoUsuarioControladorImpl();

    private static Usuario usuarioBase;
    private static Solicitud solicitudBase;
    private static Aplicacion appBase;
    private static PermisoAplicacion permisoBase;

    // ================================================================
    // 🔧 INICIALIZACIÓN DEL TEST
    // ================================================================
    @BeforeClass
    public static void inicializarDatos() {

        // Crear usuario base (sin buscarPorCorreo, porque no existe)
        usuarioBase = new Usuario();
        usuarioBase.setNombre("Ana");
        usuarioBase.setApellido("Gómez");
        usuarioBase.setCorreo("acceso.user@example.com");
        usuarioBase.setCargo("Analista");
        usuarioCtrl.crear(usuarioBase);

        // Crear solicitud base
        solicitudBase = new Solicitud();
        solicitudBase.setUsuario(usuarioBase);
        solicitudBase.setEstado("CREADA");
        solicitudCtrl.crear(solicitudBase);

        // Crear aplicación base
        appBase = new Aplicacion();
        appBase.setNombre("SistemaInventario");
        appBase.setDescripcion("Aplicación de inventarios");
        appCtrl.crear(appBase);

        // Crear permiso base
        permisoBase = new PermisoAplicacion();
        permisoBase.setNombre("PRUEBA ACCESO");
        permisoBase.setDescripcion("Permiso para test");
        permisoBase.setAplicacion(appBase);
        permisoCtrl.crear(permisoBase);

        // Crear acceso si no hay
        List<AccesoUsuario> accesos = accesoCtrl.listarPorSolicitud(solicitudBase.getId());
        if (accesos.isEmpty()) {
            AccesoUsuario a = new AccesoUsuario();
            a.setSolicitud(solicitudBase);
            a.setPermiso(permisoBase);
            accesoCtrl.crear(a);
        }
    }

    // ================================================================
    // 🔹 TEST: CREAR ACCESO
    // ================================================================
    @Test
    public void testCrearAccesoUsuario() {
        AccesoUsuario a = new AccesoUsuario();
        a.setSolicitud(solicitudBase);
        a.setPermiso(permisoBase);

        accesoCtrl.crear(a);

        assertNotNull("El acceso debe tener ID después de crearse", a.getId());
        System.out.println("✔ AccesoUsuario creado con ID: " + a.getId());
    }

    // ================================================================
    // 🔹 TEST: LISTAR ACCESOS
    // ================================================================
    @Test
    public void testListarAccesos() {
        List<AccesoUsuario> lista = accesoCtrl.listarTodos();
        assertFalse("Debe existir al menos un acceso", lista.isEmpty());
        System.out.println("📋 Total accesos: " + lista.size());
    }

    // ================================================================
    // 🔹 TEST: BUSCAR POR SOLICITUD
    // ================================================================
    @Test
    public void testBuscarPorSolicitud() {
        List<AccesoUsuario> lista = accesoCtrl.listarPorSolicitud(solicitudBase.getId());
        assertFalse("La solicitud debe tener al menos un acceso", lista.isEmpty());
        System.out.println("🔍 Accesos encontrados: " + lista.size());
    }

    // ================================================================
    // 🔹 TEST: ELIMINAR ACCESO
    // ================================================================
    @Test
    public void testEliminarAcceso() {
        List<AccesoUsuario> lista = accesoCtrl.listarTodos();
        if (!lista.isEmpty()) {
            AccesoUsuario a = lista.get(0);
            accesoCtrl.eliminar(a.getId());

            AccesoUsuario eliminado = accesoCtrl.buscarPorId(a.getId());
            assertNull("El acceso debe ser eliminado", eliminado);

            System.out.println("🗑 AccesoUsuario eliminado correctamente");
        }
    }
}
