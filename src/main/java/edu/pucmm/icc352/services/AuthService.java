package edu.pucmm.icc352.services;

import edu.pucmm.icc352.models.Usuario;
import edu.pucmm.icc352.store.DataStore;

import java.util.Optional;

public class AuthService {

    public Optional<Usuario> login(String username, String password) {
        if (username == null || password == null) return Optional.empty();

        return DataStore.USUARIOS.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst();
    }

    public boolean isAdmin(Usuario u) {
        return u != null && u.isAdmin();
    }
}
