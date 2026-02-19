package edu.pucmm.icc352.models;

import jakarta.persistence.*;

@Entity
@Table(name = "items_compra")
public class ItemCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // snapshot de producto al momento de comprar (para no depender del producto luego)
    @Column(nullable = false)
    private int productoId;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private double precio;

    @Column(nullable = false)
    private int cantidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    public ItemCarrito() {}

    public ItemCarrito(int productoId, String nombre, double precio, int cantidad) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    public Long getId() { return id; }
    public int getProductoId() { return productoId; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }
    public Compra getCompra() { return compra; }

    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setCompra(Compra compra) { this.compra = compra; }

    public double getSubtotal() {
        return precio * cantidad;
    }
}
