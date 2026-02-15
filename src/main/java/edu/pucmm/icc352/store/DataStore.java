package edu.pucmm.icc352.store;

import edu.pucmm.icc352.models.Compra;
import edu.pucmm.icc352.models.Producto;
import edu.pucmm.icc352.models.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataStore {

    public static final List<Usuario> USUARIOS = new ArrayList<>();
    public static final List<Producto> PRODUCTOS = new ArrayList<>();
    public static final List<Compra> COMPRAS = new ArrayList<>();

    private static int productoSeq = 1;
    private static long compraSeq = 1;

    static {
        // Usuario admin por defecto
        USUARIOS.add(new Usuario("admin", "Administrador", "admin", true));

        // Productos de ejemplo
        PRODUCTOS.add(new Producto(nextProductoId(), "RAM 8GB", 1500));
        PRODUCTOS.add(new Producto(nextProductoId(), "Computadora", 5000));
        PRODUCTOS.add(new Producto(nextProductoId(), "Laptop", 6000));
    }

    public static int nextProductoId() { return productoSeq++; }
    public static long nextCompraId() { return compraSeq++; }

    public static Optional<Usuario> findUser(String username) {
        return USUARIOS.stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }

    public static Optional<Producto> findProducto(int id) {
        return PRODUCTOS.stream().filter(p -> p.getId() == id).findFirst();
    }
}
