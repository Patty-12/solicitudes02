package com.senadi.solicitud02.modelo.dao.impl;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import com.senadi.solicitud02.modelo.dao.SolicitudDao;
import com.senadi.solicitud02.modelo.entidades.Solicitud;
import com.senadi.solicitud02.modelo.util.JPAUtil;

public class SolicitudDaoImpl implements SolicitudDao {

    @Override
    public void crear(Solicitud s) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(s);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Solicitud actualizar(Solicitud s) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Solicitud merged = em.merge(s);
            tx.commit();
            return merged;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void eliminar(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Solicitud ref = em.find(Solicitud.class, id);
            if (ref != null) {
                em.remove(ref);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Solicitud buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Solicitud.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Solicitud> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                        "SELECT s FROM Solicitud s ORDER BY s.id",
                        Solicitud.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Solicitud> buscarPorUsuario(Long idUsuario) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Solicitud> q = em.createQuery(
                    "SELECT s FROM Solicitud s " +
                    "WHERE s.usuario.id = :u " +
                    "ORDER BY s.fechaCreacion",
                    Solicitud.class);
            q.setParameter("u", idUsuario);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Solicitud> buscarPorEstado(String estado) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Solicitud> q = em.createQuery(
                    "SELECT s FROM Solicitud s " +
                    "WHERE s.estado = :e " +
                    "ORDER BY s.fechaCreacion",
                    Solicitud.class);
            q.setParameter("e", estado);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Solicitud> buscarPorUsuarioYEstado(Long idUsuario, String estado) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Solicitud> q = em.createQuery(
                    "SELECT s FROM Solicitud s " +
                    "WHERE s.usuario.id = :u AND s.estado = :e " +
                    "ORDER BY s.fechaCreacion",
                    Solicitud.class);
            q.setParameter("u", idUsuario);
            q.setParameter("e", estado);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Solicitud> buscarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Solicitud> q = em.createQuery(
                    "SELECT s FROM Solicitud s " +
                    "WHERE s.fechaCreacion BETWEEN :desde AND :hasta " +
                    "ORDER BY s.fechaCreacion",
                    Solicitud.class);
            q.setParameter("desde", desde);
            q.setParameter("hasta", hasta);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Solicitud> buscarPorUsuarioYFechas(Long idUsuario, LocalDateTime desde, LocalDateTime hasta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Solicitud> q = em.createQuery(
                    "SELECT s FROM Solicitud s " +
                    "WHERE s.usuario.id = :u " +
                    "AND s.fechaCreacion BETWEEN :desde AND :hasta " +
                    "ORDER BY s.fechaCreacion",
                    Solicitud.class);
            q.setParameter("u", idUsuario);
            q.setParameter("desde", desde);
            q.setParameter("hasta", hasta);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Solicitud> buscarPorEstadoYFechas(String estado, LocalDateTime desde, LocalDateTime hasta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Solicitud> q = em.createQuery(
                    "SELECT s FROM Solicitud s " +
                    "WHERE s.estado = :e " +
                    "AND s.fechaCreacion BETWEEN :desde AND :hasta " +
                    "ORDER BY s.fechaCreacion",
                    Solicitud.class);
            q.setParameter("e", estado);
            q.setParameter("desde", desde);
            q.setParameter("hasta", hasta);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Solicitud> buscarPorUsuarioEstadoYFechas(Long idUsuario, String estado,
                                                         LocalDateTime desde, LocalDateTime hasta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Solicitud> q = em.createQuery(
                    "SELECT s FROM Solicitud s " +
                    "WHERE s.usuario.id = :u " +
                    "AND s.estado = :e " +
                    "AND s.fechaCreacion BETWEEN :desde AND :hasta " +
                    "ORDER BY s.fechaCreacion",
                    Solicitud.class);
            q.setParameter("u", idUsuario);
            q.setParameter("e", estado);
            q.setParameter("desde", desde);
            q.setParameter("hasta", hasta);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

}
