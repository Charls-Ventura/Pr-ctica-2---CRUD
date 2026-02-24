package edu.pucmm.icc352.services;

import edu.pucmm.icc352.models.Usuario;
import edu.pucmm.icc352.persistence.JpaUtil;
import edu.pucmm.icc352.util.Validations;

import java.util.List;
import java.util.Optional;

public class UsuarioService {

    public List<Usuario> listar() {
        return JpaUtil.tx(session ->
                session.createQuery("from Usuario u order by u.id", Usuario.class)
                        .getResultList()
        );
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return JpaUtil.tx(session -> Optional.ofNullable(session.get(Usuario.class, id)));
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        if (Validations.isBlank(username)) return Optional.empty();

        return JpaUtil.tx(session ->
                session.createQuery("from Usuario u where u.username = :u", Usuario.class)
                        .setParameter("u", username.trim())
                        .uniqueResultOptional()
        );
    }

    public Usuario crear(String username, String nombre, String password, boolean admin) {
        if (Validations.isBlank(username)) throw new IllegalArgumentException("Username requerido");
        if (Validations.isBlank(nombre)) throw new IllegalArgumentException("Nombre requerido");
        if (Validations.isBlank(password)) throw new IllegalArgumentException("Password requerido");

        // Validar username único
        if (buscarPorUsername(username).isPresent()) {
            throw new IllegalArgumentException("Ese username ya existe");
        }

        return JpaUtil.tx(session -> {
            Usuario u = new Usuario(username.trim(), nombre.trim(), password, admin);
            session.persist(u);
            return u;
        });
    }

    public boolean editar(Long id, String username, String nombre, String password, boolean admin) {
        if (id == null) return false;
        if (Validations.isBlank(username)) return false;
        if (Validations.isBlank(nombre)) return false;

        return JpaUtil.tx(session -> {
            Usuario u = session.get(Usuario.class, id);
            if (u == null) return false;

            // Si cambió el username, validar que no choque con otro
            String newUser = username.trim();
            if (!newUser.equals(u.getUsername())) {
                var existe = session.createQuery("from Usuario x where x.username = :u", Usuario.class)
                        .setParameter("u", newUser)
                        .uniqueResultOptional();
                if (existe.isPresent()) return false;
            }

            u.setUsername(newUser);
            u.setNombre(nombre.trim());
            u.setAdmin(admin);

            // Password opcional al editar (si viene vacío no cambiarla)
            if (password != null && !password.isBlank()) {
                u.setPassword(password);
            }

            return true;
        });
    }

    public boolean eliminar(Long id) {
        if (id == null) return false;

        return JpaUtil.tx(session -> {
            Usuario u = session.get(Usuario.class, id);
            if (u == null) return false;

            session.remove(u);
            return true;
        });
    }
}