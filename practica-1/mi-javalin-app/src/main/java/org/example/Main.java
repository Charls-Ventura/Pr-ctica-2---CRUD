package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Scanner;

public class Main {

    private static final String MATRICULA = "10153529";

    public static void main(String[] args) throws Exception {

        System.out.print("Digite una URL valida: ");

        Scanner sc = new Scanner(System.in);
        String urlStr = sc.nextLine().trim();

        URI uri;
        try {
            uri = URI.create(urlStr);
        } catch (Exception e) {
            System.out.println("[ERROR] URL invalida: " + urlStr);
            return;
        }

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        System.out.println("\n[INFO] Haciendo GET a: " + uri);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();

        System.out.println("[INFO] Status: " + status);

        String contentType = response.headers().firstValue("content-type").orElse("desconocido");
        System.out.println("[INFO] Content-Type: " + contentType);

        String tipo = detectarTipo(contentType, uri);
        System.out.println("[A] Tipo de recurso: " + tipo);

        if (!tipo.equals("HTML")) {
            System.out.println("[INFO] No es HTML, se termina aqui segun el enunciado.");
            return;
        }
        String html = response.body();

        // 1) Cantidad de lineas
        long lineas = html.lines().count();
        System.out.println("\n[B1] Cantidad de lineas: " + lineas);

        // Parse con Jsoup
        Document doc = Jsoup.parse(html, uri.toString());

        // 2) Cantidad de parrafos <p>
        Elements ps = doc.select("p");
        System.out.println("[B2] Cantidad de <p>: " + ps.size());

        // 3) Cantidad de <img> dentro de <p>
        int imgDentroDeP = doc.select("p img").size();
        System.out.println("[B3] Cantidad de <img> dentro de <p>: " + imgDentroDeP);

        // 4) Cantidad de formularios por metodo GET/POST
        Elements forms = doc.select("form");
        int postCount = 0, getCount = 0, otherCount = 0;

        for (Element f : forms) {
            String method = f.attr("method");
            method = method == null ? "" : method.trim().toUpperCase(Locale.ROOT);
            if (method.isEmpty()) method = "GET"; // HTML default

            if ("POST".equals(method)) postCount++;
            else if ("GET".equals(method)) getCount++;
            else otherCount++;
        }

        System.out.println("[B4] Formularios total: " + forms.size());
        System.out.println("     - GET : " + getCount);
        System.out.println("     - POST: " + postCount);
        if (otherCount > 0) System.out.println("     - OTROS: " + otherCount);

        // 5) Para cada form: inputs y type
        System.out.println("\n[B5] Detalle de formularios e inputs:");
        int idx = 1;
        for (Element f : forms) {
            String method = f.attr("method");
            method = method == null ? "" : method.trim().toUpperCase(Locale.ROOT);
            if (method.isEmpty()) method = "GET";

            String action = f.attr("action");
            if (action == null || action.isBlank()) action = uri.toString();

            System.out.println("  Form #" + idx + " method=" + method + " action=" + action);

            Elements inputs = f.select("input");
            if (inputs.isEmpty()) {
                System.out.println("    (sin inputs)");
            } else {
                for (Element in : inputs) {
                    String name = in.attr("name");
                    String type = in.attr("type");
                    if (type == null || type.isBlank()) type = "text"; // default típico
                    System.out.println("    - input name=" + (name.isBlank() ? "(sin name)" : name) + " type=" + type);
                }
            }

            // 6) Si el form es POST: enviar POST con asignatura=practica1 y header matricula-id
            if ("POST".equals(method)) {
                URI postUri = resolverAction(uri, action);

                String body = "asignatura=" + URLEncoder.encode("practica1", StandardCharsets.UTF_8);
                HttpRequest postReq = HttpRequest.newBuilder()
                        .uri(postUri)
                        .timeout(Duration.ofSeconds(30))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("matricula-id", MATRICULA)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                System.out.println("    [B6] Enviando POST a: " + postUri);
                try {
                    HttpResponse<String> postResp = client.send(postReq, HttpResponse.BodyHandlers.ofString());
                    System.out.println("         POST status: " + postResp.statusCode());
                } catch (Exception ex) {
                    System.out.println("         [WARN] POST fallo: " + ex.getMessage());
                }
            }

            idx++;
        }
    }

    private static String detectarTipo(String contentType, URI uri) {
        String ct = contentType.toLowerCase(Locale.ROOT);

        if (ct.contains("text/html") || ct.contains("application/xhtml")) return "HTML";
        if (ct.contains("application/pdf")) return "PDF";
        if (ct.startsWith("image/")) return "IMAGEN";

        // fallback por extension
        String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase(Locale.ROOT);
        if (path.endsWith(".html") || path.endsWith(".htm")) return "HTML";
        if (path.endsWith(".pdf")) return "PDF";
        if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".gif") || path.endsWith(".webp")) return "IMAGEN";

        return "OTRO";
    }

    private static URI resolverAction(URI base, String action) {
        try {
            // Si action es absoluta, URI.create funciona.
            URI actionUri = URI.create(action);
            if (actionUri.isAbsolute()) return actionUri;
        } catch (Exception ignored) {}

        // Si es relativa, resolver contra la base
        return base.resolve(action);
    }
}
