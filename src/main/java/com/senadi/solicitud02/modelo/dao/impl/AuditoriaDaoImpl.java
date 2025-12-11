package com.senadi.solicitud02.modelo.dao.impl;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.senadi.solicitud02.modelo.dao.AuditoriaDao;
import com.senadi.solicitud02.modelo.entidades.Auditoria;
import com.senadi.solicitud02.modelo.util.JPAUtil;

public class AuditoriaDaoImpl implements AuditoriaDao {

    @Override
    public void crear(Auditoria a) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(a);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public Auditoria actualizar(Auditoria a) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Auditoria merged = em.merge(a);
            em.getTransaction().commit();
            return merged;
        } finally {
            em.close();
        }
    }

    @Override
    public void eliminar(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Auditoria a = em.find(Auditoria.class, id);
            if (a != null) {
                em.remove(a);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public Auditoria buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Auditoria.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Auditoria> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Auditoria> q = em.createQuery(
                "SELECT a FROM Auditoria a ORDER BY a.fechaEvento DESC",
                Auditoria.class
            );
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Auditoria> buscarPorUsuario(Long idUsuario) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Auditoria> q = em.createQuery(
                "SELECT a FROM Auditoria a " +
                "WHERE a.usuario.id = :idUsuario " +
                "ORDER BY a.fechaEvento DESC",
                Auditoria.class
            );
            q.setParameter("idUsuario", idUsuario);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Auditoria> buscarPorAccion(String accion) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Auditoria> q = em.createQuery(
                "SELECT a FROM Auditoria a " +
                "WHERE a.accion = :accion " +
                "ORDER BY a.fechaEvento DESC",
                Auditoria.class
            );
            q.setParameter("accion", accion);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Auditoria> buscarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Auditoria> q = em.createQuery(
                "SELECT a FROM Auditoria a " +
                "WHERE a.fechaEvento BETWEEN :desde AND :hasta " +
                "ORDER BY a.fechaEvento DESC",
                Auditoria.class
            );
            q.setParameter("desde", desde);
            q.setParameter("hasta", hasta);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Auditoria> buscarPorUsuarioYAccion(Long idUsuario, String accion) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Auditoria> q = em.createQuery(
                "SELECT a FROM Auditoria a " +
                "WHERE a.usuario.id = :idUsuario AND a.accion = :accion " +
                "ORDER BY a.fechaEvento DESC",
                Auditoria.class
            );
            q.setParameter("idUsuario", idUsuario);
            q.setParameter("accion", accion);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Auditoria> buscarPorUsuarioYFechas(Long idUsuario, LocalDateTime desde, LocalDateTime hasta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Auditoria> q = em.createQuery(
                "SELECT a FROM Auditoria a " +
                "WHERE a.usuario.id = :idUsuario " +
                "AND a.fechaEvento BETWEEN :desde AND :hasta " +
                "ORDER BY a.fechaEvento DESC",
                Auditoria.class
            );
            q.setParameter("idUsuario", idUsuario);
            q.setParameter("desde", desde);
            q.setParameter("hasta", hasta);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Auditoria> buscarPorAccionYFechas(String accion, LocalDateTime desde, LocalDateTime hasta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Auditoria> q = em.createQuery(
                "SELECT a FROM Auditoria a " +
                "WHERE a.accion = :accion " +
                "AND a.fechaEvento BETWEEN :desde AND :hasta " +
                "ORDER BY a.fechaEvento DESC",
                Auditoria.class
            );
            q.setParameter("accion", accion);
            q.setParameter("desde", desde);
            q.setParameter("hasta", hasta);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Auditoria> buscarPorUsuarioAccionYFechas(Long idUsuario, String accion,
                                                         LocalDateTime desde, LocalDateTime hasta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Auditoria> q = em.createQuery(
                "SELECT a FROM Auditoria a " +
                "WHERE a.usuario.id = :idUsuario " +
                "AND a.accion = :accion " +
                "AND a.fechaEvento BETWEEN :desde AND :hasta " +
                "ORDER BY a.fechaEvento DESC",
                Auditoria.class
            );
            q.setParameter("idUsuario", idUsuario);
            q.setParameter("accion", accion);
            q.setParameter("desde", desde);
            q.setParameter("hasta", hasta);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
