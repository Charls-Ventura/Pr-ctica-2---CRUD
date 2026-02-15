package edu.pucmm.icc352.services;

import edu.pucmm.icc352.models.Compra;
import edu.pucmm.icc352.models.ItemCarrito;
import edu.pucmm.icc352.store.DataStore;
import edu.pucmm.icc352.util.Validations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CompraService {

    public List<Compra> listarCompras() {
        return DataStore.COMPRAS;
    }

    public Compra procesarCompra(String nombreCliente, List<ItemCarrito> carrito) {
        if (Validations.isBlank(nombreCliente)) throw new IllegalArgumentException("Nombre del cliente requerido");
        if (carrito == null || carrito.isEmpty()) throw new IllegalArgumentException("Carrito vacío");

        // Copia del carrito para que no se "rompa" si luego lo limpias
        List<ItemCarrito> copia = new ArrayList<>();
        for (ItemCarrito i : carrito) {
            copia.add(new ItemCarrito(i.getProductoId(), i.getNombre(), i.getPrecio(), i.getCantidad()));
        }

        Compra compra = new Compra(DataStore.nextCompraId(), new Date(), nombreCliente.trim(), copia);
        DataStore.COMPRAS.add(compra);
        return compra;
    }
}
