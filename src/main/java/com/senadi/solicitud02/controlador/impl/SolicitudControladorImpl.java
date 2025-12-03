package com.senadi.solicitud02.controlador.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.senadi.solicitud02.controlador.SolicitudControlador;
import com.senadi.solicitud02.modelo.dao.SolicitudDao;
import com.senadi.solicitud02.modelo.dao.impl.SolicitudDaoImpl;
import com.senadi.solicitud02.modelo.entidades.Solicitud;

public class SolicitudControladorImpl implements SolicitudControlador {

    private final SolicitudDao solicitudDao = new SolicitudDaoImpl();

    @Override
    public void crear(Solicitud s) {
        solicitudDao.crear(s);
    }

    @Override
    public Solicitud actualizar(Solicitud s) {
        return solicitudDao.actualizar(s);
    }

    @Override
    public void eliminar(Long id) {
        solicitudDao.eliminar(id);
    }

    @Override
    public Solicitud buscarPorId(Long id) {
        return solicitudDao.buscarPorId(id);
    }

    @Override
    public List<Solicitud> listarTodos() {
        return solicitudDao.listarTodos();
    }

    @Override
    public List<Solicitud> buscarPorUsuario(Long idUsuario) {
        return solicitudDao.buscarPorUsuario(idUsuario);
    }

    @Override
    public List<Solicitud> buscarPorEstado(String estado) {
        return solicitudDao.buscarPorEstado(estado);
    }

    @Override
    public List<Solicitud> buscarPorUsuarioYEstado(Long idUsuario, String estado) {
        return solicitudDao.buscarPorUsuarioYEstado(idUsuario, estado);
    }

    @Override
    public List<Solicitud> buscarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        return solicitudDao.buscarPorRangoFechas(desde, hasta);
    }

    @Override
    public List<Solicitud> buscarPorUsuarioYFechas(Long idUsuario, LocalDateTime desde, LocalDateTime hasta) {
        return solicitudDao.buscarPorUsuarioYFechas(idUsuario, desde, hasta);
    }

    @Override
    public List<Solicitud> buscarPorEstadoYFechas(String estado, LocalDateTime desde, LocalDateTime hasta) {
        return solicitudDao.buscarPorEstadoYFechas(estado, desde, hasta);
    }

    @Override
    public List<Solicitud> buscarPorUsuarioEstadoYFechas(Long idUsuario, String estado,
                                                         LocalDateTime desde, LocalDateTime hasta) {
        return solicitudDao.buscarPorUsuarioEstadoYFechas(idUsuario, estado, desde, hasta);
    }

}
