package edu.pucmm.icc352.models;

import jakarta.persistence.*;

@Entity
@Table(name = "producto_imagenes")
public class ProductoImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false, columnDefinition = "CLOB")
    private String base64;

    @Column(nullable = false)
    private String mimeType; // image/png, image/jpeg

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    public ProductoImagen() {}

    public ProductoImagen(String base64, String mimeType, Producto producto) {
        this.base64 = base64;
        this.mimeType = mimeType;
        this.producto = producto;
    }

    public Long getId() { return id; }
    public String getBase64() { return base64; }
    public String getMimeType() { return mimeType; }
    public Producto getProducto() { return producto; }
}