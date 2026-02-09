package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Scanner;

public class Main {

    /*URL utilizada al correr el programa:
    * https://www.w3schools.com/html/html_forms.asp
    * https://httpbin.org/forms/post
    * Puede utilizar otras para verificar el funcionamiento
    * Al correr el MAIN, le pedira el URL
    * En caso de usar la terminal del dispositivo, use: .\gradlew run --args="https://www.w3schools.com/html/html_forms.asp"
    * o ".\gradlew run --args="https://httpbin.org/forms/post"
     * */

    private static final String MATRICULA = "10153529";

    public static void main(String[] args) {
        String urlStr = null;
        Scanner sc = new Scanner(System.in);

        System.out.println("Pegue una URL valida: ");

        if (sc.hasNextLine()) {
            urlStr = sc.nextLine().trim();
        }

        if ((urlStr == null || urlStr.isBlank()) && args.length > 0) {
            urlStr = args[0].trim();
            System.out.println("[INFO] URL tomada desde argumento de ejecucion.");
        }

        if (urlStr == null || urlStr.isBlank()) {
            System.out.println("[ERROR] No se recibio ninguna URL.");
            return;
        }

        System.out.println("[INFO] URL a analizar: " + urlStr);

        URI uri;
        try {
            uri = URI.create(urlStr);
        } catch (Exception e) {
            System.out.println("[ERROR] URL invalida.");
            return;
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(60))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(60))
                    .GET()
                    .build();

            System.out.println("[INFO] Realizando peticion GET...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            String contentType = response.headers().firstValue("content-type").orElse("desconocido");

            System.out.println("[INFO] Status HTTP: " + status);
            System.out.println("[INFO] Content-Type: " + contentType);

            String tipo = detectarTipo(contentType, uri);
            System.out.println("Tipo de recurso: " + tipo);

            if (!"HTML".equals(tipo)) {
                System.out.println("[INFO] El recurso no es HTML.");
                return;
            }

            String html = response.body();

            long lineas = html.lines().count();
            System.out.println("Cantidad de lineas del HTML: " + lineas);

            Document doc = Jsoup.parse(html, uri.toString());

            Elements parrafos = doc.select("p");
            System.out.println("Cantidad de <p>: " + parrafos.size());

            int imgsEnP = doc.select("p img").size();
            System.out.println("Cantidad de <img> dentro de <p>: " + imgsEnP);

            Elements forms = doc.select("form");
            int getCount = 0, postCount = 0;

            for (Element f : forms) {
                String method = f.attr("method").toUpperCase(Locale.ROOT);
                if (method.isBlank()) method = "GET";
                if ("POST".equals(method)) postCount++;
                else getCount++;
            }

            System.out.println("Formularios totales: " + forms.size());
            System.out.println("     GET : " + getCount);
            System.out.println("     POST: " + postCount);

            int i = 1;
            for (Element f : forms) {
                String method = f.attr("method").toUpperCase(Locale.ROOT);
                if (method.isBlank()) method = "GET";
                String action = f.attr("action");
                if (action.isBlank()) action = uri.toString();

                System.out.println("Formulario #" + i + " Metodo=" + method);
                Elements inputs = f.select("input");
                for (Element in : inputs) {
                    String name = in.attr("name");
                    String type = in.attr("type");
                    if (type.isBlank()) type = "text";
                    System.out.println("     input name=" + (name.isBlank() ? "(sin name)" : name)
                            + " type=" + type);
                }

                if ("POST".equals(method)) {
                    URI postUri = uri.resolve(action);
                    String body = "asignatura=" + URLEncoder.encode("practica1", StandardCharsets.UTF_8);

                    HttpRequest postReq = HttpRequest.newBuilder()
                            .uri(postUri)
                            .timeout(Duration.ofSeconds(30))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .header("matricula-id", MATRICULA)
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build();

                    System.out.println("Enviando POST a: " + postUri);
                    HttpResponse<String> postResp = client.send(postReq, HttpResponse.BodyHandlers.ofString());
                    System.out.println("     POST Status: " + postResp.statusCode());
                }
                i++;
            }

        } catch (Exception e) {
            System.out.println("[ERROR] Ocurrio un problema durante la ejecucion.");
            System.out.println(e.getMessage());
        }
    }

    private static String detectarTipo(String contentType, URI uri) {
        String ct = contentType.toLowerCase(Locale.ROOT);

        if (ct.contains("text/html")) return "HTML";
        if (ct.contains("application/pdf")) return "PDF";
        if (ct.startsWith("image/")) return "IMAGEN";

        String path = uri.getPath().toLowerCase(Locale.ROOT);
        if (path.endsWith(".html") || path.endsWith(".htm")) return "HTML";
        if (path.endsWith(".pdf")) return "PDF";
        if (path.matches(".*\\.(png|jpg|jpeg|gif|webp)$")) return "IMAGEN";

        return "OTRO";
    }
}
