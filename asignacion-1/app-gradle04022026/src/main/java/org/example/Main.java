package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    enum KeySession { USUARIO }

    /**
     * DTO
     * param usuario
     * param password
     */record Usuario(String usuario, String password) {}

    public static void main(String[] args) {
        var app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
                staticFiles.precompress = false;
                staticFiles.aliasCheck = null;

            });
        });

        app.before(ctx -> {
            String p = ctx.path();

            if (p.equals("/login.html") || p.equals("/procesarLogin")) return;

            Usuario u = ctx.sessionAttribute(KeySession.USUARIO.name());
            if (u == null) {
                ctx.redirect("/login.html");
            }
        });

        app.get("/", ctx -> ctx.result("Hola Mundo desde Javalin :-D!!"));

        app.get("/logout", ctx -> {
            ctx.req().getSession().invalidate();
            ctx.redirect("/login.html");
        });


        app.post("/procesarLogin", ctx -> {
            String usuario = ctx.formParam("usuario");
            String password = ctx.formParam("password");

            if ("admin".equals(usuario) && "admin".equals(password)) {
                ctx.sessionAttribute(KeySession.USUARIO.name(), new Usuario(usuario, password));
                ctx.redirect("/");
            } else {
                ctx.redirect("/login.html?error=1");
            }
        });

        app.start(7000);
    }
}