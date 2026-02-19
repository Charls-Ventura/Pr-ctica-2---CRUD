package edu.pucmm.icc352.models;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean admin;

    public Usuario() {} // requerido por Hibernate

    public Usuario(String username, String nombre, String password, boolean admin) {
        this.username = username;
        this.nombre = nombre;
        this.password = password;
        this.admin = admin;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getNombre() { return nombre; }
    public String getPassword() { return password; }
    public boolean isAdmin() { return admin; }

    public void setUsername(String username) { this.username = username; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPassword(String password) { this.password = password; }
    public void setAdmin(boolean admin) { this.admin = admin; }
}
