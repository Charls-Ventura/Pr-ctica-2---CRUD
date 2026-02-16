package edu.pucmm.icc352.models;

public class Usuario {
    private String username;
    private String nombre;
    private String password;
    private boolean admin;

    public Usuario(String username, String nombre, String password, boolean admin) {
        this.username = username;
        this.nombre = nombre;
        this.password = password;
        this.admin = admin;
    }

    public String getUsername() { return username; }
    public String getNombre() { return nombre; }
    public String getPassword() { return password; }
    public boolean isAdmin() { return admin; }
}
