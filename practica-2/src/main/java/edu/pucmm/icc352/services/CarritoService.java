package edu.pucmm.icc352.services;

import edu.pucmm.icc352.models.ItemCarrito;
import edu.pucmm.icc352.models.Producto;

import java.util.ArrayList;
import java.util.List;

public class CarritoService {

    public List<ItemCarrito> crearCarritoVacio() {
        return new ArrayList<>();
    }

    public void agregar(List<ItemCarrito> carrito, Producto producto, int cantidad) {
        if (carrito == null) throw new IllegalArgumentException("Carrito nulo");
        if (producto == null) throw new IllegalArgumentException("Producto nulo");
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida");

        ItemCarrito existente = null;
        for (ItemCarrito i : carrito) {
            if (i.getProductoId() == producto.getId()) {
                existente = i;
                break;
            }
        }

        if (existente == null) {
            carrito.add(new ItemCarrito(producto.getId(), producto.getNombre(), producto.getPrecio(), cantidad));
        } else {
            existente.setCantidad(existente.getCantidad() + cantidad);
        }
    }

    public boolean quitar(List<ItemCarrito> carrito, int productoId) {
        if (carrito == null) return false;
        return carrito.removeIf(i -> i.getProductoId() == productoId);
    }

    // restar una cantidad específica
    public void restarCantidad(List<ItemCarrito> carrito, int productoId, int cantidadARestar) {
        if (carrito == null) return;
        if (cantidadARestar <= 0) return;

        ItemCarrito encontrado = null;
        for (ItemCarrito i : carrito) {
            if (i.getProductoId() == productoId) {
                encontrado = i;
                break;
            }
        }
        if (encontrado == null) return;

        int nueva = encontrado.getCantidad() - cantidadARestar;

        if (nueva <= 0) {
            carrito.removeIf(i -> i.getProductoId() == productoId);
        } else {
            encontrado.setCantidad(nueva);
        }
    }

    public void limpiar(List<ItemCarrito> carrito) {
        if (carrito != null) carrito.clear();
    }

    public int contadorItems(List<ItemCarrito> carrito) {
        if (carrito == null) return 0;
        int count = 0;
        for (ItemCarrito i : carrito) count += i.getCantidad();
        return count;
    }

    public double total(List<ItemCarrito> carrito) {
        if (carrito == null) return 0;
        double t = 0;
        for (ItemCarrito i : carrito) t += i.getSubtotal();
        return t;
    }
}
