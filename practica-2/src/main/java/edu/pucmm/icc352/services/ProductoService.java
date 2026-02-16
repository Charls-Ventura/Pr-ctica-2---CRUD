package edu.pucmm.icc352.services;

import edu.pucmm.icc352.models.Producto;
import edu.pucmm.icc352.store.DataStore;
import edu.pucmm.icc352.util.Validations;

import java.util.List;
import java.util.Optional;

public class ProductoService {

    public List<Producto> listar() {
        return DataStore.PRODUCTOS;
    }

    public Optional<Producto> buscarPorId(int id) {
        return DataStore.findProducto(id);
    }

    public Producto crear(String nombre, double precio) {
        if (Validations.isBlank(nombre)) throw new IllegalArgumentException("Nombre requerido");
        if (!Validations.isPositiveDouble(precio)) throw new IllegalArgumentException("Precio inválido");

        Producto p = new Producto(DataStore.nextProductoId(), nombre.trim(), precio);
        DataStore.PRODUCTOS.add(p);
        return p;
    }

    public boolean editar(int id, String nombre, double precio) {
        Optional<Producto> op = buscarPorId(id);
        if (op.isEmpty()) return false;

        if (Validations.isBlank(nombre)) return false;
        if (!Validations.isPositiveDouble(precio)) return false;

        Producto p = op.get();
        p.setNombre(nombre.trim());
        p.setPrecio(precio);
        return true;
    }

    public boolean eliminar(int id) {
        return DataStore.PRODUCTOS.removeIf(p -> p.getId() == id);
    }
}
