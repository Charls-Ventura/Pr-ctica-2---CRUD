package edu.pucmm.icc352.models;

public class ItemCarrito {
    private int productoId;
    private String nombre;
    private double precio;
    private int cantidad;

    public ItemCarrito(int productoId, String nombre, double precio, int cantidad) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    public int getProductoId() { return productoId; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }

    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getSubtotal() {
        return precio * cantidad;
    }
}
