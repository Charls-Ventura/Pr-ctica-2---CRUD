package edu.pucmm.icc352;

import edu.pucmm.icc352.models.ItemCarrito;
import edu.pucmm.icc352.models.Producto;
import edu.pucmm.icc352.models.Usuario;
import edu.pucmm.icc352.services.SistemaService;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.pucmm.icc352.controllers.AdminController;

public class App {

    public static void main(String[] args) {

        JavalinThymeleaf.init();
        Javalin app = Javalin.create().start(7000);

        SistemaService sistema = new SistemaService();

        app.get("/login", ctx -> {
            Map<String, Object> model = baseModel(ctx, sistema);
            ctx.render("templates/login.html", model);
        });

        // LOGIN (POST)
        app.post("/login", ctx -> {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");

            var opt = sistema.auth.login(username, password);
            if (opt.isPresent()) {
                ctx.sessionAttribute("user", opt.get()); // guardamos usuario en sesión
                ctx.redirect("/admin");
                return;
            }

            // si falla
            Map<String, Object> model = baseModel(ctx, sistema);
            model.put("error", "Credenciales invalidas");
            ctx.render("templates/login.html", model);
        });

        // LOGOUT
        app.get("/logout", ctx -> {
            ctx.req().getSession().invalidate();
            ctx.redirect("/");
        });

        AdminController admin = new AdminController(sistema);
        admin.register(app);

        // HOME
        app.get("/", ctx -> {
            Map<String, Object> model = baseModel(ctx, sistema);
            model.put("productos", sistema.productos.listar());

            String success = ctx.sessionAttribute("success");
            if (success != null) {
                model.put("success", success);
                ctx.sessionAttribute("success", null);
            }

            ctx.render("templates/index.html", model);
        });


        // AGREGAR AL CARRITO
        app.post("/cart/add", ctx -> {
            int productoId = Integer.parseInt(ctx.formParam("productoId"));
            int cantidad = Integer.parseInt(ctx.formParam("cantidad"));

            List<ItemCarrito> cart = ctx.sessionAttribute("cart");
            if (cart == null) cart = new ArrayList<>();

            Producto p = sistema.productos.buscarPorId(productoId).orElse(null);
            if (p != null) {
                sistema.carrito.agregar(cart, p, cantidad);
            }

            ctx.sessionAttribute("cart", cart);
            ctx.redirect("/");
        });

        // VER CARRITO
        app.get("/cart", ctx -> {
            Map<String, Object> model = baseModel(ctx, sistema);

            List<ItemCarrito> cart = ctx.sessionAttribute("cart");
            if (cart == null) cart = new ArrayList<>();

            model.put("cart", cart);
            model.put("total", sistema.carrito.total(cart));

            ctx.render("templates/cart.html", model);
        });

        // RESTAR CANTIDAD (NUEVO)
        app.post("/cart/decrease", ctx -> {
            int productoId = Integer.parseInt(ctx.formParam("productoId"));
            int cantidad = Integer.parseInt(ctx.formParam("cantidad"));

            List<ItemCarrito> cart = ctx.sessionAttribute("cart");
            if (cart == null) cart = new ArrayList<>();

            sistema.carrito.restarCantidad(cart, productoId, cantidad);

            ctx.sessionAttribute("cart", cart);
            ctx.redirect("/cart");
        });

        // QUITAR COMPLETO
        app.post("/cart/remove", ctx -> {
            int productoId = Integer.parseInt(ctx.formParam("productoId"));

            List<ItemCarrito> cart = ctx.sessionAttribute("cart");
            if (cart == null) cart = new ArrayList<>();

            sistema.carrito.quitar(cart, productoId);

            ctx.sessionAttribute("cart", cart);
            ctx.redirect("/cart");
        });

        // CHECKOUT
        app.post("/cart/checkout", ctx -> {
            String nombreCliente = ctx.formParam("nombreCliente");

            List<ItemCarrito> cart = ctx.sessionAttribute("cart");
            if (cart == null) cart = new ArrayList<>();

            if (!cart.isEmpty() && nombreCliente != null && !nombreCliente.trim().isEmpty()) {
                sistema.compras.procesarCompra(nombreCliente, cart);
                sistema.carrito.limpiar(cart);
                ctx.sessionAttribute("cart", cart);
                ctx.sessionAttribute("success", "Su compra se realizó correctamente.");
            }

            ctx.redirect("/");
        });
    }

    private static Map<String, Object> baseModel(
            io.javalin.http.Context ctx,
            SistemaService sistema) {

        Map<String, Object> model = new HashMap<>();

        List<ItemCarrito> cart = ctx.sessionAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        Usuario user = ctx.sessionAttribute("user");

        model.put("cartCount", sistema.carrito.contadorItems(cart));
        model.put("user", user);
        model.put("isAdmin", user != null && sistema.auth.isAdmin(user));

        return model;
    }

}
