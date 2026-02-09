package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    private static final Set<String> TOKENS = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {

        int port = 7000;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                System.out.println("[WARN] Puerto invalido, usando 7000.");
            }
        }

        System.out.println("[INFO] Iniciando Javalin en puerto: " + port);

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
            });
        });

        app.before(ctx -> {
            String path = ctx.path();

            if (path.equals("/login") || path.equals("/login.html")
                    || path.equals("/styles.css")) {
                return;
            }

            if (path.equals("/") || path.equals("/index.html") || path.equals("/logout")) {
                String token = ctx.cookie("auth_token");
                if (token == null || !TOKENS.contains(token)) {
                    System.out.println("[AUTH] Acceso denegado a " + path + " (no token)");
                    ctx.redirect("/login");
                }
            }
        });

        app.get("/login", ctx -> {
            System.out.println("[ROUTE] GET /login");
            ctx.redirect("/login.html");
        });

        app.post("/login", ctx -> {
            System.out.println("[ROUTE] POST /login");

            String user = ctx.formParam("user");
            String pass = ctx.formParam("pass");

            System.out.println("[LOGIN] user=" + user + " pass=" + (pass == null ? "null" : "***"));
            boolean ok = "admin".equals(user) && "1234".equals(pass);

            if (!ok) {
                System.out.println("[LOGIN] Fallo de autenticacion");
                ctx.redirect("/login.html?error=1");
                return;
            }

            // Crear token y guardarlo
            String token = UUID.randomUUID().toString();
            TOKENS.add(token);

            // Guardar token en cookie
            ctx.cookie("auth_token", token);
            System.out.println("[LOGIN] OK. Token creado: " + token);

            ctx.redirect("/");
        });

        app.get("/", ctx -> {
            System.out.println("[ROUTE] GET /");
            ctx.redirect("/index.html");
        });

        app.get("/logout", ctx -> {
            System.out.println("[ROUTE] GET /logout");
            String token = ctx.cookie("auth_token");
            if (token != null) {
                TOKENS.remove(token);
            }
            ctx.removeCookie("auth_token");
            ctx.redirect("/login");
        });

        app.get("/ping", ctx -> {
            System.out.println("[ROUTE] GET /ping");
            ctx.result("Prueba correcta. Endpoint /ping funcionando correctamente.");
        });

        app.start(port);
    }
}
