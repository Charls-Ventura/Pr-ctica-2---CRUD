package edu.pucmm.icc352.services;

import edu.pucmm.icc352.models.Producto;
import edu.pucmm.icc352.persistence.JpaUtil;
import edu.pucmm.icc352.util.Validations;

import java.util.List;
import java.util.Optional;

public class ProductoService {

    public List<Producto> listar() {
        return JpaUtil.tx(session ->
                session.createQuery("from Producto p order by p.id", Producto.class)
                        .getResultList()
        );
    }

    public Optional<Producto> buscarPorId(int id) {
        return JpaUtil.tx(session -> Optional.ofNullable(session.get(Producto.class, id)));
    }

    public Producto crear(String nombre, double precio) {
        if (Validations.isBlank(nombre)) throw new IllegalArgumentException("Nombre requerido");
        if (!Validations.isPositiveDouble(precio)) throw new IllegalArgumentException("Precio inválido");

        return JpaUtil.tx(session -> {
            Producto p = new Producto(nombre.trim(), precio);
            session.persist(p);
            return p;
        });
    }

    public boolean editar(int id, String nombre, double precio) {
        if (Validations.isBlank(nombre)) return false;
        if (!Validations.isPositiveDouble(precio)) return false;

        return JpaUtil.tx(session -> {
            Producto p = session.get(Producto.class, id);
            if (p == null) return false;

            p.setNombre(nombre.trim());
            p.setPrecio(precio);
            return true;
        });
    }

    public boolean eliminar(int id) {
        return JpaUtil.tx(session -> {
            Producto p = session.get(Producto.class, id);
            if (p == null) return false;

            session.remove(p);
            return true;
        });
    }
}
