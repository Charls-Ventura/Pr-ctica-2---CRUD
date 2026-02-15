package edu.pucmm.icc352.models;

import java.util.Date;
import java.util.List;

public class Compra {
    private long id;
    private Date fecha;
    private String nombreCliente;
    private List<ItemCarrito> items;

    public Compra(long id, Date fecha, String nombreCliente, List<ItemCarrito> items) {
        this.id = id;
        this.fecha = fecha;
        this.nombreCliente = nombreCliente;
        this.items = items;
    }

    public long getId() { return id; }
    public Date getFecha() { return fecha; }
    public String getNombreCliente() { return nombreCliente; }
    public List<ItemCarrito> getItems() { return items; }

    public double getTotal() {
        double total = 0;
        for (ItemCarrito i : items) total += i.getSubtotal();
        return total;
    }
}
