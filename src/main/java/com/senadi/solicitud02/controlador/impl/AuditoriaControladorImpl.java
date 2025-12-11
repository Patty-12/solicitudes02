package com.senadi.solicitud02.controlador.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.senadi.solicitud02.controlador.AuditoriaControlador;
import com.senadi.solicitud02.modelo.dao.AuditoriaDao;
import com.senadi.solicitud02.modelo.dao.impl.AuditoriaDaoImpl;
import com.senadi.solicitud02.modelo.entidades.Auditoria;
import com.senadi.solicitud02.modelo.entidades.Usuario;

public class AuditoriaControladorImpl implements AuditoriaControlador {

    private AuditoriaDao auditoriaDao = new AuditoriaDaoImpl();

    @Override
    public void crear(Auditoria a) {
        auditoriaDao.crear(a);
    }

    @Override
    public Auditoria actualizar(Auditoria a) {
        return auditoriaDao.actualizar(a);
    }

    @Override
    public void eliminar(Long id) {
        auditoriaDao.eliminar(id);
    }

    @Override
    public Auditoria buscarPorId(Long id) {
        return auditoriaDao.buscarPorId(id);
    }

    @Override
    public List<Auditoria> listarTodos() {
        return auditoriaDao.listarTodos();
    }

    @Override
    public List<Auditoria> buscarPorUsuario(Long idUsuario) {
        return auditoriaDao.buscarPorUsuario(idUsuario);
    }

    @Override
    public List<Auditoria> buscarPorAccion(String accion) {
        return auditoriaDao.buscarPorAccion(accion);
    }

    @Override
    public List<Auditoria> buscarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        return auditoriaDao.buscarPorRangoFechas(desde, hasta);
    }

    @Override
    public List<Auditoria> buscarPorUsuarioYAccion(Long idUsuario, String accion) {
        return auditoriaDao.buscarPorUsuarioYAccion(idUsuario, accion);
    }

    @Override
    public List<Auditoria> buscarPorUsuarioYFechas(Long idUsuario, LocalDateTime desde, LocalDateTime hasta) {
        return auditoriaDao.buscarPorUsuarioYFechas(idUsuario, desde, hasta);
    }

    @Override
    public List<Auditoria> buscarPorAccionYFechas(String accion, LocalDateTime desde, LocalDateTime hasta) {
        return auditoriaDao.buscarPorAccionYFechas(accion, desde, hasta);
    }

    @Override
    public List<Auditoria> buscarPorUsuarioAccionYFechas(Long idUsuario, String accion,
                                                         LocalDateTime desde, LocalDateTime hasta) {
        return auditoriaDao.buscarPorUsuarioAccionYFechas(idUsuario, accion, desde, hasta);
    }

    @Override
    public void registrarEvento(Usuario usuario,
                                String accion,
                                String entidad,
                                Long entidadId,
                                String detalle) {

        if (usuario == null) {
            // si llega nulo, no registramos nada para evitar incoherencias
            return;
        }

        Auditoria a = new Auditoria();
        a.setUsuario(usuario);
        a.setAccion(accion);
        a.setEntidad(entidad);
        a.setEntidadId(entidadId);
        a.setDetalle(detalle);
        a.setFechaEvento(LocalDateTime.now());

        auditoriaDao.crear(a);
    }
}
