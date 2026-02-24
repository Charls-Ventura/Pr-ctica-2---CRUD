package edu.pucmm.icc352.services;

import edu.pucmm.icc352.models.Producto;
import edu.pucmm.icc352.persistence.JpaUtil;
import edu.pucmm.icc352.util.Validations;

import java.util.List;
import java.util.Optional;

public class ProductoService {

    // =========================
    // Paginación (lo usa App.java)
    // =========================

    public long contar() {
        return JpaUtil.tx(session ->
                session.createQuery("select count(p) from Producto p", Long.class)
                        .getSingleResult()
        );
    }

    public List<Producto> listarPagina(int page, int size) {
        final int safePage = Math.max(0, page);
        final int safeSize = Math.max(1, size);

        return JpaUtil.tx(session ->
                session.createQuery("from Producto p order by p.id", Producto.class)
                        .setFirstResult(safePage * safeSize)
                        .setMaxResults(safeSize)
                        .getResultList()
        );
    }

    // =========================
    // Imagen (lo usa App.java /producto/{id}/img)
    // =========================

    public record Imagen(byte[] bytes, String mimeType) {}

    public Optional<Imagen> getImagen(int id) {
        return JpaUtil.tx(session -> {
            Producto p = session.get(Producto.class, id);
            if (p == null) return Optional.empty();

            byte[] img = p.getImagen();
            if (img == null || img.length == 0) return Optional.empty();

            String mime = p.getImagenMimeType();
            if (mime == null || mime.isBlank()) mime = "image/jpeg";

            return Optional.of(new Imagen(img, mime));
        });
    }

    // =========================
    // CRUD básico (por si lo necesitas)
    // =========================

    public List<Producto> listar() {
        return JpaUtil.tx(session ->
                session.createQuery("from Producto p order by p.id", Producto.class)
                        .getResultList()
        );
    }

    public Optional<Producto> buscarPorId(int id) {
        return JpaUtil.tx(session -> Optional.ofNullable(session.get(Producto.class, id)));
    }

    // =========================
    // CRUD con 1 imagen + descripción (lo usa AdminController)
    // =========================

    public Producto crear(String nombre,
                          double precio,
                          String descripcion,
                          byte[] imagen,
                          String mimeType) {


        if (Validations.isBlank(nombre)) throw new IllegalArgumentException("Nombre requerido");
        if (!Validations.isPositiveDouble(precio)) throw new IllegalArgumentException("Precio inválido");
        if (Validations.isBlank(descripcion)) throw new IllegalArgumentException("Descripción requerida");
        if (imagen == null || imagen.length == 0) throw new IllegalArgumentException("Debe subir una imagen");
        if (Validations.isBlank(mimeType)) throw new IllegalArgumentException("MimeType inválido");

        return JpaUtil.tx(session -> {
            Producto p = new Producto(
                    nombre.trim(),
                    precio,
                    descripcion.trim(),
                    imagen,
                    mimeType
            );
            session.persist(p);
            return p;
        });
    }

    /**
     * Editar: imagen opcional.
     * - Si no mandan imagen nueva => se queda la anterior.
     */
    public boolean editar(int id,
                          String nombre,
                          double precio,
                          String descripcion,
                          byte[] nuevaImagen,
                          String nuevoMimeType) {

        if (Validations.isBlank(nombre)) return false;
        if (!Validations.isPositiveDouble(precio)) return false;
        if (Validations.isBlank(descripcion)) return false;

        if (precio <= 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo ni cero");
        }

        return JpaUtil.tx(session -> {
            Producto p = session.get(Producto.class, id);
            if (p == null) return false;

            p.setNombre(nombre.trim());
            p.setPrecio(precio);
            p.setDescripcion(descripcion.trim());

            // si viene imagen nueva, la actualizamos
            if (nuevaImagen != null && nuevaImagen.length > 0) {
                p.setImagen(nuevaImagen);
                if (nuevoMimeType != null && !nuevoMimeType.isBlank()) {
                    p.setImagenMimeType(nuevoMimeType);
                }
            }

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