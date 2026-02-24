package edu.pucmm.icc352.models;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String texto;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    public Comentario() {}

    public Comentario(String texto, Producto producto) {
        this.texto = texto;
        this.producto = producto;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTexto() { return texto; }
    public Instant getCreatedAt() { return createdAt; }
    public Producto getProducto() { return producto; }
}