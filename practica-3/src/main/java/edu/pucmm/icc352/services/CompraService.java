package edu.pucmm.icc352.services;

import edu.pucmm.icc352.models.Compra;
import edu.pucmm.icc352.models.ItemCarrito;
import edu.pucmm.icc352.persistence.JpaUtil;
import edu.pucmm.icc352.util.Validations;

import java.util.Date;
import java.util.List;

public class CompraService {

    public List<Compra> listarCompras() {
        return JpaUtil.tx(session ->
                session.createQuery(
                                "select distinct c from Compra c left join fetch c.items order by c.id desc",
                                Compra.class
                        )
                        .getResultList()
        );
    }

    public Compra procesarCompra(String nombreCliente, List<ItemCarrito> carrito) {
        if (Validations.isBlank(nombreCliente)) throw new IllegalArgumentException("Nombre del cliente requerido");
        if (carrito == null || carrito.isEmpty()) throw new IllegalArgumentException("Carrito vacío");

        return JpaUtil.tx(session -> {
            Compra compra = new Compra(new Date(), nombreCliente.trim());

            for (ItemCarrito i : carrito) {
                ItemCarrito item = new ItemCarrito(
                        i.getProductoId(),
                        i.getNombre(),
                        i.getPrecio(),
                        i.getCantidad()
                );
                compra.addItem(item); // setea compra_id
            }

            session.persist(compra); // guarda compra + items por cascade
            return compra;
        });
    }
}
