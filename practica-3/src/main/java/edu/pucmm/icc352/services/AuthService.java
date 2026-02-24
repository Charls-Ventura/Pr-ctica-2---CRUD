package edu.pucmm.icc352.services;

import edu.pucmm.icc352.models.Usuario;
import edu.pucmm.icc352.persistence.JpaUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

public class AuthService {

    private static final String ENV_JDBC_URL = "JDBC_DATABASE_URL";

    public Optional<Usuario> login(String username, String password) {
        if (username == null || password == null) return Optional.empty();

        Optional<Usuario> result = JpaUtil.tx(session ->
                session.createQuery(
                                "from Usuario u where u.username = :u and u.password = :p",
                                Usuario.class
                        )
                        .setParameter("u", username)
                        .setParameter("p", password)
                        .uniqueResultOptional()
        );

        // Requisito 9: registrar login en BD externa (si fue exitoso)
        result.ifPresent(u -> logLogin(u.getUsername()));

        return result;
    }

    // Requisito 4: buscar por username para auto-login con cookie
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

    private void logLogin(String username) {
        String jdbcUrl = System.getenv(ENV_JDBC_URL);
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            // No está configurada la variable de ambiente: no romper el login.
            return;
        }

        // Asegúrate de crear esta tabla una vez en CockroachDB:
        // CREATE TABLE IF NOT EXISTS login_audit (
        //   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        //   username STRING NOT NULL,
        //   login_at TIMESTAMPTZ NOT NULL
        // );

        String sql = "INSERT INTO login_audit (username, login_at) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setTimestamp(2, Timestamp.from(Instant.now()));
            ps.executeUpdate();

        } catch (Exception e) {
            // No tires la app por esto. Si quieres, imprime para depurar:
            System.err.println("No se pudo registrar login en CockroachDB: " + e.getMessage());
        }
    }
}