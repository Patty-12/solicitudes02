package com.senadi.solicitud02.modelo.dao;

import java.util.List;

import com.senadi.solicitud02.modelo.entidades.Usuario;

public interface UsuarioDao {
    
    void crear(Usuario u);
    Usuario actualizar(Usuario u);
    void eliminar(Long id);
    Usuario buscarPorId(Long id);
    Usuario buscarPorCorreo(String correo);
    List<Usuario> listarTodos();
    List<Usuario> buscarPorNombre(String nombre);
    List<Usuario> buscarPorApellido(String apellido);
    List<Usuario> buscarPorCargo(String cargo);
    List<Usuario> buscarPorNombreYApellido(String nombre, String apellido);
    List<Usuario> buscarPorNombreOCorreo(String texto); // búsqueda parcial
    
    Usuario autenticar(String correo, String password);

    // === MÉTODOS NUEVOS PARA DIRECTORES / FORMULARIO ===

    /**
     * Busca un usuario por correo y cargo exactos.
     */
    Usuario buscarPorCorreoYCargo(String correo, String cargo);

    /**
     * Busca un usuario por nombre y cargo exactos.
     */
    Usuario buscarPorNombreYCargo(String nombre, String cargo);

    /**
     * Busca un usuario por cédula y cargo exactos.
     * IMPORTANTE: requiere que la entidad Usuario tenga el campo 'cedula'.
     */
    Usuario buscarPorCedulaYCargo(String cedula, String cargo);

    /**
     * Busca una lista de directores cuyo nombre completo coincida parcialmente
     * con el patrón proporcionado (para autocompletado).
     */
    List<Usuario> buscarDirectoresPorNombreLike(String patronNombre);
}
