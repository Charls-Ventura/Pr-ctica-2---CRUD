package edu.pucmm.icc352.services;

import edu.pucmm.icc352.models.Usuario;
import edu.pucmm.icc352.persistence.JpaUtil;

import java.util.Optional;

public class AuthService {

    public Optional<Usuario> login(String username, String password) {
        if (username == null || password == null) return Optional.empty();

        return JpaUtil.tx(session ->
                session.createQuery(
                                "from Usuario u where u.username = :u and u.password = :p",
                                Usuario.class
                        )
                        .setParameter("u", username)
                        .setParameter("p", password)
                        .uniqueResultOptional()
        );
    }

    // equisito 4: buscar por username para auto-login con cookie
    public Optional<Usuario> findByUsername(String username) {
        if (username == null) return Optional.empty();

        return JpaUtil.tx(session ->
                session.createQuery(
                                "from Usuario u where u.username = :u",
                                Usuario.class
                        )
                        .setParameter("u", username)
                        .uniqueResultOptional()
        );
    }

    public boolean isAdmin(Usuario u) {
        return u != null && u.isAdmin();
    }
}
