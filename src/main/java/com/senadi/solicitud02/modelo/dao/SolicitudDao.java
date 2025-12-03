package com.senadi.solicitud02.modelo.dao;

import java.time.LocalDateTime;
import java.util.List;
import com.senadi.solicitud02.modelo.entidades.Solicitud;

public interface SolicitudDao {

    void crear(Solicitud s);
    Solicitud actualizar(Solicitud s);
    void eliminar(Long id);
    Solicitud buscarPorId(Long id);

    List<Solicitud> listarTodos();

    List<Solicitud> buscarPorUsuario(Long idUsuario);
    List<Solicitud> buscarPorEstado(String estado);
    List<Solicitud> buscarPorUsuarioYEstado(Long idUsuario, String estado);

    List<Solicitud> buscarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta);
    List<Solicitud> buscarPorUsuarioYFechas(Long idUsuario, LocalDateTime desde, LocalDateTime hasta);
    List<Solicitud> buscarPorEstadoYFechas(String estado, LocalDateTime desde, LocalDateTime hasta);
    List<Solicitud> buscarPorUsuarioEstadoYFechas(Long idUsuario, String estado, LocalDateTime desde, LocalDateTime hasta);

}
