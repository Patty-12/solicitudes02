package com.senadi.solicitud02.modelo.dao;

import java.util.List;
import com.senadi.solicitud02.modelo.entidades.AccesoUsuario;

public interface AccesoUsuarioDao {

    void crear(AccesoUsuario a);
    AccesoUsuario actualizar(AccesoUsuario a);
    void eliminar(Long id);
    AccesoUsuario buscarPorId(Long id);
    List<AccesoUsuario> listarTodos();

    List<AccesoUsuario> listarPorSolicitud(Long idSolicitud);
}
