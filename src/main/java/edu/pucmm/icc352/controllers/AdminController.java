package edu.pucmm.icc352.controllers;

import edu.pucmm.icc352.models.ItemCarrito;
import edu.pucmm.icc352.models.Producto;
import edu.pucmm.icc352.models.Usuario;
import edu.pucmm.icc352.services.SistemaService;
import io.javalin.Javalin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminController {

    private final SistemaService sistema;

    public AdminController(SistemaService sistema) {
        this.sistema = sistema;
    }

    public void register(Javalin app) {
        protegerAdmin(app);
        panelAdmin(app);
        crudProductos(app);
        ventas(app);
    }

    private void protegerAdmin(Javalin app) {
        // Protege /admin (exacto)
        app.before("/admin", ctx -> {
            Usuario user = ctx.sessionAttribute("user");
            if (user == null) { ctx.redirect("/login"); return; }
            if (!sistema.auth.isAdmin(user)) { ctx.status(403).result("Acceso denegado"); return; }
        });

        // Protege /admin/*
        app.before("/admin/*", ctx -> {
            Usuario user = ctx.sessionAttribute("user");
            if (user == null) { ctx.redirect("/login"); return; }
            if (!sistema.auth.isAdmin(user)) { ctx.status(403).result("Acceso denegado"); return; }
        });
    }

    private void panelAdmin(Javalin app) {
        app.get("/admin", ctx -> {
            Map<String, Object> model = baseModel(ctx);
            model.put("mensaje", "Panel administrador");
            ctx.render("templates/admin.html", model);
        });
    }

    private void crudProductos(Javalin app) {

        // LISTAR
        app.get("/admin/productos", ctx -> {
            Map<String, Object> model = baseModel(ctx);
            model.put("productos", sistema.productos.listar());
            ctx.render("templates/admin_productos.html", model);
        });

        // FORM NUEVO
        app.get("/admin/productos/new", ctx -> {
            Map<String, Object> model = baseModel(ctx);
            model.put("titulo", "Nuevo Producto");
            model.put("action", "/admin/productos");
            model.put("btn", "Crear");
            model.put("nombre", "");
            model.put("precio", "");
            ctx.render("templates/admin_productos_form.html", model);
        });

        // CREAR
        app.post("/admin/productos", ctx -> {
            String nombre = ctx.formParam("nombre");
            String precioStr = ctx.formParam("precio");

            try {
                double precio = Double.parseDouble(precioStr);
                sistema.productos.crear(nombre, precio);
                ctx.redirect("/admin/productos");
            } catch (Exception e) {
                Map<String, Object> model = baseModel(ctx);
                model.put("titulo", "Nuevo Producto");
                model.put("action", "/admin/productos");
                model.put("btn", "Crear");
                model.put("nombre", nombre == null ? "" : nombre);
                model.put("precio", precioStr == null ? "" : precioStr);
                model.put("error", e.getMessage());
                ctx.render("templates/admin_productos_form.html", model);
            }
        });

        // FORM EDITAR
        app.get("/admin/productos/{id}/edit", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            var op = sistema.productos.buscarPorId(id);

            if (op.isEmpty()) {
                ctx.status(404).result("Producto no encontrado");
                return;
            }

            Producto p = op.get();

            Map<String, Object> model = baseModel(ctx);
            model.put("titulo", "Editar Producto #" + p.getId());
            model.put("action", "/admin/productos/" + p.getId());
            model.put("btn", "Guardar");
            model.put("nombre", p.getNombre());
            model.put("precio", p.getPrecio());
            ctx.render("templates/admin_productos_form.html", model);
        });

        // GUARDAR EDICIÓN
        app.post("/admin/productos/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            String nombre = ctx.formParam("nombre");
            String precioStr = ctx.formParam("precio");

            try {
                double precio = Double.parseDouble(precioStr);
                boolean ok = sistema.productos.editar(id, nombre, precio);

                if (!ok) {
                    ctx.status(404).result("Producto no encontrado o datos inválidos");
                    return;
                }

                ctx.redirect("/admin/productos");
            } catch (Exception e) {
                Map<String, Object> model = baseModel(ctx);
                model.put("titulo", "Editar Producto #" + id);
                model.put("action", "/admin/productos/" + id);
                model.put("btn", "Guardar");
                model.put("nombre", nombre == null ? "" : nombre);
                model.put("precio", precioStr == null ? "" : precioStr);
                model.put("error", e.getMessage());
                ctx.render("templates/admin_productos_form.html", model);
            }
        });

        // ELIMINAR
        app.post("/admin/productos/{id}/delete", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            sistema.productos.eliminar(id);
            ctx.redirect("/admin/productos");
        });
    }

    private Map<String, Object> baseModel(io.javalin.http.Context ctx) {
        Map<String, Object> model = new HashMap<>();

        List<ItemCarrito> cart = ctx.sessionAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        Usuario user = ctx.sessionAttribute("user");

        model.put("cartCount", sistema.carrito.contadorItems(cart));
        model.put("user", user);
        model.put("isAdmin", user != null && sistema.auth.isAdmin(user));

        return model;
    }


    private void ventas(Javalin app) {
        app.get("/admin/ventas", ctx -> {
            Map<String, Object> model = baseModel(ctx);
            model.put("compras", sistema.compras.listarCompras()); // o listarCompras()
            ctx.render("templates/admin_ventas.html", model);
        });
    }

}
