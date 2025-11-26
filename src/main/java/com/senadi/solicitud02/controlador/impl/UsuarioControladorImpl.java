package com.senadi.solicitud02.controlador.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import com.senadi.solicitud02.controlador.UsuarioControlador;
import com.senadi.solicitud02.modelo.entidades.Usuario;

public class UsuarioControladorImpl implements UsuarioControlador {

    private EntityManagerFactory emf;
    private EntityManager em;

    public UsuarioControladorImpl() {
        // Inicializar EntityManagerFactory y EntityManager con tu persistence unit
        emf = Persistence.createEntityManagerFactory("solicitud02PU");
        em = emf.createEntityManager();
    }

    @Override
    public void crear(Usuario u) {
        try {
            em.getTransaction().begin();
            em.persist(u);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        }
    }

    @Override
    public Usuario actualizar(Usuario u) {
        try {
            em.getTransaction().begin();
            Usuario actualizado = em.merge(u);
            em.getTransaction().commit();
            return actualizado;
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void eliminar(Long id) {
        try {
            em.getTransaction().begin();
            Usuario u = buscarPorId(id);
            if (u != null) {
                em.remove(u);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        }
    }

    @Override
    public Usuario buscarPorId(Long id) {
        return em.find(Usuario.class, id);
    }

    @Override
    public Usuario buscarPorCorreo(String correo) {
        try {
            return em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.correo = :correo", Usuario.class)
                     .setParameter("correo", correo)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Usuario buscarPorCorreoYCargo(String correo, String cargo) {
        try {
            return em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.correo = :correo AND u.cargo = :cargo",
                    Usuario.class)
                     .setParameter("correo", correo)
                     .setParameter("cargo", cargo)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Usuario> listarTodos() {
        return em.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList();
    }

    @Override
    public List<Usuario> buscarPorNombre(String nombre) {
        return em.createQuery(
                "SELECT u FROM Usuario u WHERE u.nombre = :nombre", Usuario.class)
                 .setParameter("nombre", nombre)
                 .getResultList();
    }

    @Override
    public List<Usuario> buscarPorApellido(String apellido) {
        return em.createQuery(
                "SELECT u FROM Usuario u WHERE u.apellido = :apellido", Usuario.class)
                 .setParameter("apellido", apellido)
                 .getResultList();
    }

    @Override
    public List<Usuario> buscarPorCargo(String cargo) {
        return em.createQuery(
                "SELECT u FROM Usuario u WHERE u.cargo = :cargo", Usuario.class)
                 .setParameter("cargo", cargo)
                 .getResultList();
    }

    @Override
    public List<Usuario> buscarPorNombreYApellido(String nombre, String apellido) {
        return em.createQuery(
                "SELECT u FROM Usuario u WHERE u.nombre = :nombre AND u.apellido = :apellido",
                Usuario.class)
                 .setParameter("nombre", nombre)
                 .setParameter("apellido", apellido)
                 .getResultList();
    }

    @Override
    public List<Usuario> buscarPorNombreOCorreo(String texto) {
        return em.createQuery(
                "SELECT u FROM Usuario u WHERE u.nombre LIKE :texto OR u.correo LIKE :texto",
                Usuario.class)
                 .setParameter("texto", "%" + texto + "%")
                 .getResultList();
    }

    @Override
    public Usuario autenticar(String correo, String password) {
        try {
            return em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.correo = :correo AND u.password = :password",
                    Usuario.class)
                     .setParameter("correo", correo)
                     .setParameter("password", password)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    // ======================================================
    //   MÉTODOS NUEVOS PARA DIRECTORES / AUTOCOMPLETE
    // ======================================================

    @Override
    public Usuario buscarPorNombreYCargo(String nombre, String cargo) {
        try {
            return em.createQuery(
                    "SELECT u FROM Usuario u " +
                    "WHERE LOWER(u.nombre) = :nombre AND LOWER(u.cargo) = LOWER(:cargo)",
                    Usuario.class)
                     .setParameter("nombre", nombre.toLowerCase())
                     .setParameter("cargo", cargo)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Usuario buscarPorCedulaYCargo(String cedula, String cargo) {
        try {
            return em.createQuery(
                    "SELECT u FROM Usuario u " +
                    "WHERE u.cedula = :cedula AND LOWER(u.cargo) = LOWER(:cargo)",
                    Usuario.class)
                     .setParameter("cedula", cedula)
                     .setParameter("cargo", cargo)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Usuario> buscarDirectoresPorNombreLike(String patronNombre) {
        return em.createQuery(
                "SELECT u FROM Usuario u " +
                "WHERE LOWER(CONCAT(u.nombre, ' ', u.apellido)) LIKE :patron " +
                "AND LOWER(u.cargo) = LOWER(:cargo) " +
                "ORDER BY u.nombre, u.apellido",
                Usuario.class)
                 .setParameter("patron", "%" + patronNombre.toLowerCase() + "%")
                 .setParameter("cargo", "Director")
                 .getResultList();
    }
}
