package edu.pucmm.icc352.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "compras")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date fecha;

    @Column(nullable = false)
    private String nombreCliente;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCarrito> items = new ArrayList<>();

    public Compra() {}

    public Compra(Date fecha, String nombreCliente) {
        this.fecha = fecha;
        this.nombreCliente = nombreCliente;
    }

    public Long getId() { return id; }
    public Date getFecha() { return fecha; }
    public String getNombreCliente() { return nombreCliente; }
    public List<ItemCarrito> getItems() { return items; }

    public void setFecha(Date fecha) { this.fecha = fecha; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public void setItems(List<ItemCarrito> items) { this.items = items; }

    public void addItem(ItemCarrito item) {
        items.add(item);
        item.setCompra(this);
    }

    public double getTotal() {
        double total = 0;
        for (ItemCarrito i : items) total += i.getSubtotal();
        return total;
    }
}
