package edu.pucmm.icc352.models;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private double precio;

    @Column(length = 1000)
    private String descripcion;

    @Lob
    @Column(name = "imagen", columnDefinition = "BLOB")
    private byte[] imagen;

    @Column(name = "imagen_mime", length = 100)
    private String imagenMimeType;

    public Producto() {}

    public Producto(String nombre, double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    public Producto(String nombre, double precio, String descripcion, byte[] imagen, String imagenMimeType) {
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.imagenMimeType = imagenMimeType;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public String getDescripcion() { return descripcion; }
    public byte[] getImagen() { return imagen; }
    public String getImagenMimeType() { return imagenMimeType; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setImagen(byte[] imagen) { this.imagen = imagen; }
    public void setImagenMimeType(String imagenMimeType) { this.imagenMimeType = imagenMimeType; }

    @Transient
    public boolean tieneImagen() {
        return imagen != null && imagen.length > 0;
    }
}