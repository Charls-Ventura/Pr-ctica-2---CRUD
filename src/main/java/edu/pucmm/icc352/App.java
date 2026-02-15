package edu.pucmm.icc352;

import edu.pucmm.icc352.models.ItemCarrito;
import edu.pucmm.icc352.models.Producto;
import edu.pucmm.icc352.services.SistemaService;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {

    public static void main(String[] args) {

        JavalinThymeleaf.init();
        Javalin app = Javalin.create().start(7000);

        SistemaService sistema = new SistemaService();

        // HOME
        app.get("/", ctx -> {
            Map<String, Object> model = new HashMap<>();

            List<ItemCarrito> cart = ctx.sessionAttribute("cart");
            if (cart == null) cart = new ArrayList<>();

            model.put("productos", sistema.productos.listar());
            model.put("cartCount", sistema.carrito.contadorItems(cart));

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
            Map<String, Object> model = new HashMap<>();

            List<ItemCarrito> cart = ctx.sessionAttribute("cart");
            if (cart == null) cart = new ArrayList<>();

            model.put("cart", cart);
            model.put("total", sistema.carrito.total(cart));
            model.put("cartCount", sistema.carrito.contadorItems(cart));

            ctx.render("templates/cart.html", model);
        });

        // ✅ RESTAR CANTIDAD (NUEVO)
        app.post("/cart/decrease", ctx -> {
            int productoId = Integer.parseInt(ctx.formParam("productoId"));
            int cantidad = Integer.parseInt(ctx.formParam("cantidad"));

            List<ItemCarrito> cart = ctx.sessionAttribute("cart");
            if (cart == null) cart = new ArrayList<>();

            sistema.carrito.restarCantidad(cart, productoId, cantidad);

            ctx.sessionAttribute("cart", cart);
            ctx.redirect("/cart");
        });

        // QUITAR COMPLETO (si quieres seguir teniéndolo)
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
            }

            ctx.redirect("/");
        });
    }
}
