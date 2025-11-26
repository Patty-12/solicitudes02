package com.senadi.solicitud02.modelo.dao.impl;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import com.senadi.solicitud02.modelo.dao.UsuarioDao;
import com.senadi.solicitud02.modelo.entidades.Usuario;
import com.senadi.solicitud02.modelo.util.JPAUtil;

public class UsuarioDaoImpl implements UsuarioDao {

    @Override
    public void crear(Usuario u) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(u);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally { em.close(); }
    }

    @Override
    public Usuario actualizar(Usuario u) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Usuario merged = em.merge(u);
            tx.commit();
            return merged;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally { em.close(); }
    }

    @Override
    public void eliminar(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Usuario ref = em.find(Usuario.class, id);
            if (ref != null) em.remove(ref);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally { em.close(); }
    }

    @Override
    public Usuario buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try { 
            return em.find(Usuario.class, id); 
        } finally { 
            em.close(); 
        }
    }

    @Override
    public Usuario buscarPorCorreo(String correo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.correo = :c", Usuario.class);
            q.setParameter("c", correo);
            List<Usuario> r = q.getResultList();
            return r.isEmpty() ? null : r.get(0);
        } finally { em.close(); }
    }

    @Override
    public List<Usuario> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u ORDER BY u.id", Usuario.class)
                     .getResultList();
        } finally { em.close(); }
    }
    
    @Override
    public List<Usuario> buscarPorNombre(String nombre) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.nombre = :n", Usuario.class);
            q.setParameter("n", nombre);
            return q.getResultList();
        } finally { em.close(); }
    }

    @Override
    public List<Usuario> buscarPorApellido(String apellido) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.apellido = :a", Usuario.class);
            q.setParameter("a", apellido);
            return q.getResultList();
        } finally { em.close(); }
    }

    @Override
    public List<Usuario> buscarPorCargo(String cargo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.cargo = :c", Usuario.class);
            q.setParameter("c", cargo);
            return q.getResultList();
        } finally { em.close(); }
    }

    @Override
    public List<Usuario> buscarPorNombreYApellido(String nombre, String apellido) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.nombre = :n AND u.apellido = :a", Usuario.class);
            q.setParameter("n", nombre);
            q.setParameter("a", apellido);
            return q.getResultList();
        } finally { em.close(); }
    }

    @Override
    public List<Usuario> buscarPorNombreOCorreo(String texto) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.nombre LIKE :t OR u.correo LIKE :t", Usuario.class);
            q.setParameter("t", "%" + texto + "%");
            return q.getResultList();
        } finally { em.close(); }
    }

    @Override
    public Usuario autenticar(String correo, String password) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            System.out.println(">>> Intentando autenticar: " + correo + " / " + password);

            Usuario porCorreo = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.correo = :c", Usuario.class)
                    .setParameter("c", correo)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (porCorreo == null) {
                System.out.println(">>> No se encontró ningún usuario con ese correo");
                return null;
            }

            System.out.println(">>> Usuario encontrado: " + porCorreo.getNombre()
                    + " passBD=" + porCorreo.getPassword());

            if (porCorreo.getPassword() != null &&
                porCorreo.getPassword().equals(password)) {
                return porCorreo;
            }

            System.out.println(">>> La contraseña no coincide");
            return null;
        } finally {
            em.close();
        }
    }

    // ======================================================
    //   MÉTODOS NUEVOS PARA DIRECTORES / FORMULARIO
    // ======================================================

    @Override
    public Usuario buscarPorCorreoYCargo(String correo, String cargo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u " +
                "WHERE LOWER(u.correo) = :correo " +
                "AND LOWER(u.cargo) = LOWER(:cargo)", Usuario.class);
            q.setParameter("correo", correo.toLowerCase());
            q.setParameter("cargo", cargo);
            List<Usuario> r = q.getResultList();
            return r.isEmpty() ? null : r.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public Usuario buscarPorNombreYCargo(String nombre, String cargo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u " +
                "WHERE LOWER(u.nombre) = :nombre " +
                "AND LOWER(u.cargo) = LOWER(:cargo)", Usuario.class);
            q.setParameter("nombre", nombre.toLowerCase());
            q.setParameter("cargo", cargo);
            List<Usuario> r = q.getResultList();
            return r.isEmpty() ? null : r.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public Usuario buscarPorCedulaYCargo(String cedula, String cargo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u " +
                "WHERE u.cedula = :cedula " +
                "AND LOWER(u.cargo) = LOWER(:cargo)", Usuario.class);
            q.setParameter("cedula", cedula);
            q.setParameter("cargo", cargo);
            List<Usuario> r = q.getResultList();
            return r.isEmpty() ? null : r.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Usuario> buscarDirectoresPorNombreLike(String patronNombre) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT u FROM Usuario u " +
                          "WHERE LOWER(CONCAT(u.nombre, ' ', u.apellido)) LIKE :patron " +
                          "AND LOWER(u.cargo) = LOWER(:cargo) " +
                          "ORDER BY u.nombre, u.apellido";
            TypedQuery<Usuario> q = em.createQuery(jpql, Usuario.class)
                    .setParameter("patron", "%" + patronNombre.toLowerCase() + "%")
                    .setParameter("cargo", "Director");
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
