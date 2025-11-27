package com.senadi.solicitud02.modelo.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import com.senadi.solicitud02.modelo.dao.AccesoUsuarioDao;
import com.senadi.solicitud02.modelo.entidades.AccesoUsuario;
import com.senadi.solicitud02.modelo.util.JPAUtil;

public class AccesoUsuarioDaoImpl implements AccesoUsuarioDao {

    @Override
    public void crear(AccesoUsuario a) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(a);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public AccesoUsuario actualizar(AccesoUsuario a) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            AccesoUsuario merged = em.merge(a);
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
            AccesoUsuario ref = em.find(AccesoUsuario.class, id);
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
    public AccesoUsuario buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(AccesoUsuario.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<AccesoUsuario> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM AccesoUsuario a ORDER BY a.id",
                    AccesoUsuario.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<AccesoUsuario> listarPorSolicitud(Long idSolicitud) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<AccesoUsuario> q = em.createQuery(
                "SELECT a FROM AccesoUsuario a " +
                "JOIN FETCH a.permiso p " +
                "JOIN FETCH p.aplicacion " +
                "WHERE a.solicitud.id = :idSol",
                AccesoUsuario.class);
            q.setParameter("idSol", idSolicitud);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
