package com.senadi.solicitud02.controlador;

import java.util.List;
import com.senadi.solicitud02.modelo.entidades.AccesoUsuario;

public interface AccesoUsuarioControlador {

    void crear(AccesoUsuario a);
    AccesoUsuario actualizar(AccesoUsuario a);
    void eliminar(Long id);
    AccesoUsuario buscarPorId(Long id);
    List<AccesoUsuario> listarTodos();

    // Nuevo: usado por SolicitudBean
    List<AccesoUsuario> listarPorSolicitud(Long idSolicitud);
}
