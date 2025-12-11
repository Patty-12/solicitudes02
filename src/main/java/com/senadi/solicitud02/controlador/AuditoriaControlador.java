package com.senadi.solicitud02.controlador;

import java.time.LocalDateTime;
import java.util.List;

import com.senadi.solicitud02.modelo.entidades.Auditoria;
import com.senadi.solicitud02.modelo.entidades.Usuario;

public interface AuditoriaControlador {

    void crear(Auditoria a);
    Auditoria actualizar(Auditoria a);
    void eliminar(Long id);
    Auditoria buscarPorId(Long id);
    List<Auditoria> listarTodos();

    List<Auditoria> buscarPorUsuario(Long idUsuario);
    List<Auditoria> buscarPorAccion(String accion);
    List<Auditoria> buscarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta);
    List<Auditoria> buscarPorUsuarioYAccion(Long idUsuario, String accion);
    List<Auditoria> buscarPorUsuarioYFechas(Long idUsuario, LocalDateTime desde, LocalDateTime hasta);
    List<Auditoria> buscarPorAccionYFechas(String accion, LocalDateTime desde, LocalDateTime hasta);
    List<Auditoria> buscarPorUsuarioAccionYFechas(Long idUsuario, String accion,
                                                  LocalDateTime desde, LocalDateTime hasta);

    // Helper institucional para registrar eventos
    void registrarEvento(Usuario usuario,
                         String accion,
                         String entidad,
                         Long entidadId,
                         String detalle);
}
