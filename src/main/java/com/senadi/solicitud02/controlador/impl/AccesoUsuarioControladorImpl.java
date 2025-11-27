package com.senadi.solicitud02.controlador.impl;

import java.util.List;

import com.senadi.solicitud02.controlador.AccesoUsuarioControlador;
import com.senadi.solicitud02.modelo.dao.AccesoUsuarioDao;
import com.senadi.solicitud02.modelo.dao.impl.AccesoUsuarioDaoImpl;
import com.senadi.solicitud02.modelo.entidades.AccesoUsuario;

public class AccesoUsuarioControladorImpl implements AccesoUsuarioControlador {

    private final AccesoUsuarioDao accesoDao = new AccesoUsuarioDaoImpl();

    @Override
    public void crear(AccesoUsuario a) {
        accesoDao.crear(a);
    }

    @Override
    public AccesoUsuario actualizar(AccesoUsuario a) {
        return accesoDao.actualizar(a);
    }

    @Override
    public void eliminar(Long id) {
        accesoDao.eliminar(id);
    }

    @Override
    public AccesoUsuario buscarPorId(Long id) {
        return accesoDao.buscarPorId(id);
    }

    @Override
    public List<AccesoUsuario> listarTodos() {
        return accesoDao.listarTodos();
    }

    @Override
    public List<AccesoUsuario> listarPorSolicitud(Long idSolicitud) {
        return accesoDao.listarPorSolicitud(idSolicitud);
    }
}
