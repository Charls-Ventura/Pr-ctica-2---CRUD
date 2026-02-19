package edu.pucmm.icc352;

import edu.pucmm.icc352.controllers.AdminController;
import edu.pucmm.icc352.models.ItemCarrito;
import edu.pucmm.icc352.models.Producto;
import edu.pucmm.icc352.models.Usuario;
import edu.pucmm.icc352.services.SistemaService;
import edu.pucmm.icc352.util.H2Server;
import edu.pucmm.icc352.util.RememberMeUtil;
import edu.pucmm.icc352.util.SessionKeys;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {

    private static final int REMEMBER_SECONDS = 7 * 24 * 60 * 60;

    public static void main(String[] args) {

        // levantar H2 (TCP 9092 + WEB 8082) antes de Hibernate/login
        H2Server.start();

        JavalinThymeleaf.init();
        Javalin app = Javalin.create().start(7000);

        SistemaService sistema = new SistemaService();

        //  Requisito 4: auto-login por cookie si no hay sesión
        app.before(ctx -> {
            Usuario user = ctx.sessionAttribute(SessionKeys.USER);
            if (user != null) return;

            String token = ctx.cookie(SessionKeys.REMEMBER_COOKIE);
            if (token == null) return;

            RememberMeUtil.Decoded decoded = RememberMeUtil.decodeToken(token);
            if (decoded == null) {
                ctx.cookie(SessionKeys.REMEMBER_COOKIE, "", 0);
                return;
            }

            if (decoded.expiresAtMillis < System.currentTimeMillis()) {
                ctx.cookie(SessionKeys.REMEMBER_COOKIE, "", 0);
                return;
            }

            sistema.auth.findByUsername(decoded.username)
                    .ifPresent(u -> ctx.sessionAttribute(SessionKeys.USER, u));
        });

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
                ctx.sessionAttribute(SessionKeys.USER, opt.get());

                //  Si marcó "recordarme"
                String remember = ctx.formParam("remember"); // "on" si se marcó
                if ("on".equals(remember)) {
                    long exp = System.currentTimeMillis() + (REMEMBER_SECONDS * 1000L);
                    String token = RememberMeUtil.buildToken(opt.get().getUsername(), exp);
                    ctx.cookie(SessionKeys.REMEMBER_COOKIE, token, REMEMBER_SECONDS);
                }

                ctx.redirect("/admin");
                return;
            }

            Map<String, Object> model = baseModel(ctx, sistema);
            model.put("error", "Credenciales invalidas");
            ctx.render("templates/login.html", model);
        });

        // LOGOUT
        app.get("/logout", ctx -> {
            ctx.req().getSession().invalidate();
            ctx.cookie(SessionKeys.REMEMBER_COOKIE, "", 0); // borra cookie
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

            List<ItemCarrito> cart = ctx.sessionAttribute(SessionKeys.CART);
            if (cart == null) cart = new ArrayList<>();

            Producto p = sistema.productos.buscarPorId(productoId).orElse(null);
            if (p != null) {
                sistema.carrito.agregar(cart, p, cantidad);
            }

            ctx.sessionAttribute(SessionKeys.CART, cart);
            ctx.redirect("/");
        });

        // VER CARRITO
        app.get("/cart", ctx -> {
            Map<String, Object> model = baseModel(ctx, sistema);

            List<ItemCarrito> cart = ctx.sessionAttribute(SessionKeys.CART);
            if (cart == null) cart = new ArrayList<>();

            model.put("cart", cart);
            model.put("total", sistema.carrito.total(cart));

            ctx.render("templates/cart.html", model);
        });

        // RESTAR CANTIDAD
        app.post("/cart/decrease", ctx -> {
            int productoId = Integer.parseInt(ctx.formParam("productoId"));
            int cantidad = Integer.parseInt(ctx.formParam("cantidad"));

            List<ItemCarrito> cart = ctx.sessionAttribute(SessionKeys.CART);
            if (cart == null) cart = new ArrayList<>();

            sistema.carrito.restarCantidad(cart, productoId, cantidad);

            ctx.sessionAttribute(SessionKeys.CART, cart);
            ctx.redirect("/cart");
        });

        // QUITAR COMPLETO
        app.post("/cart/remove", ctx -> {
            int productoId = Integer.parseInt(ctx.formParam("productoId"));

            List<ItemCarrito> cart = ctx.sessionAttribute(SessionKeys.CART);
            if (cart == null) cart = new ArrayList<>();

            sistema.carrito.quitar(cart, productoId);

            ctx.sessionAttribute(SessionKeys.CART, cart);
            ctx.redirect("/cart");
        });

        // CHECKOUT
        app.post("/cart/checkout", ctx -> {
            String nombreCliente = ctx.formParam("nombreCliente");

            List<ItemCarrito> cart = ctx.sessionAttribute(SessionKeys.CART);
            if (cart == null) cart = new ArrayList<>();

            if (!cart.isEmpty() && nombreCliente != null && !nombreCliente.trim().isEmpty()) {
                sistema.compras.procesarCompra(nombreCliente, cart);
                sistema.carrito.limpiar(cart);
                ctx.sessionAttribute(SessionKeys.CART, cart);
                ctx.sessionAttribute("success", "Su compra se realizó correctamente.");
            }

            ctx.redirect("/");
        });
    }

    private static Map<String, Object> baseModel(io.javalin.http.Context ctx, SistemaService sistema) {
        Map<String, Object> model = new HashMap<>();

        List<ItemCarrito> cart = ctx.sessionAttribute(SessionKeys.CART);
        if (cart == null) cart = new ArrayList<>();

        Usuario user = ctx.sessionAttribute(SessionKeys.USER);

        model.put("cartCount", sistema.carrito.contadorItems(cart));
        model.put("user", user);
        model.put("isAdmin", user != null && sistema.auth.isAdmin(user));

        return model;
    }
}
