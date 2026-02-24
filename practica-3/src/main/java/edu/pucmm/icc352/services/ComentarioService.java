package edu.pucmm.icc352.services;

import edu.pucmm.icc352.models.Comentario;
import edu.pucmm.icc352.models.Producto;
import edu.pucmm.icc352.persistence.JpaUtil;

import java.util.List;
import java.util.Optional;

public class ComentarioService {

    public List<Comentario> listarPorProducto(int productoId) {
        return JpaUtil.tx(session ->
                session.createQuery(
                                "from Comentario c where c.producto.id = :id order by c.createdAt desc",
                                Comentario.class
                        )
                        .setParameter("id", productoId)
                        .getResultList()
        );
    }

    public void agregar(String texto, Producto producto) {
        if (texto == null || texto.isBlank()) return;

        JpaUtil.tx(session -> {
            Comentario c = new Comentario(texto.trim(), producto);
            session.persist(c);
            return null;
        });
    }

    public void eliminar(Long id) {
        JpaUtil.tx(session -> {
            Comentario c = session.get(Comentario.class, id);
            if (c != null) session.remove(c);
            return null;
        });
    }
}