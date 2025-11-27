package com.senadi.solicitud02.controlador;

import java.util.List;

import com.senadi.solicitud02.modelo.entidades.Usuario;

public interface UsuarioControlador {
    
    void crear(Usuario u);
    Usuario actualizar(Usuario u);
    void eliminar(Long id);
    Usuario buscarPorId(Long id);

    // Búsqueda tradicional por correo (se mantiene por compatibilidad)
    Usuario buscarPorCorreo(String correo);

    // NUEVO: búsqueda por username (para login / flujos internos)
    Usuario buscarPorUsername(String username);

    List<Usuario> listarTodos();
    List<Usuario> buscarPorNombre(String nombre);
    List<Usuario> buscarPorApellido(String apellido);
    List<Usuario> buscarPorCargo(String cargo);
    List<Usuario> buscarPorNombreYApellido(String nombre, String apellido);

    // Ampliado: ahora también puede encontrar por username
    List<Usuario> buscarPorNombreOCorreo(String texto);
    
    Usuario buscarPorCorreoYCargo(String correo, String cargo);

    // AHORA: autenticación por username + password
    Usuario autenticar(String username, String password);

    // === MÉTODOS PARA EL FORMULARIO DE SOLICITUD ===

    Usuario buscarPorNombreYCargo(String nombre, String cargo);

    Usuario buscarPorCedulaYCargo(String cedula, String cargo);

    List<Usuario> buscarDirectoresPorNombreLike(String patronNombre);
    
    // === MÉTODOS DE APOYO PARA ROLES / PERMISOS ===
    boolean tieneRol(Long idUsuario, String nombreRol);

    List<String> obtenerNombresRoles(Long idUsuario);

}
