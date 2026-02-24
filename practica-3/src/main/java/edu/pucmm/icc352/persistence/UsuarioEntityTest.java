package edu.pucmm.icc352.persistence;

import edu.pucmm.icc352.models.Usuario;
import edu.pucmm.icc352.util.H2Server;

public class UsuarioEntityTest {
    public static void main(String[] args) {
        H2Server.start();

        JpaUtil.tx(session -> {
            session.persist(new Usuario("admin", "Administrador", "admin", true));
            return null;
        });

        System.out.println(" Usuario insertado en BD ");
    }
}
