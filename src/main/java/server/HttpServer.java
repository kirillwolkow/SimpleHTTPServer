package server;

import engine.TemplateEngine;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class HttpServer {

    private static final int SERVER_PORT = 8080;

    private HttpServer() {

    }

    public static void startServer() {
        try (final ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server running on port " + SERVER_PORT);
            listenToRequests(serverSocket);
        } catch (final Exception e) {
            System.out.println("startServer() failed: " + e);
        }
    }

    private static void listenToRequests(ServerSocket serverSocket) throws IOException {
        while (true) {
            try (final Socket socket = serverSocket.accept()) {

                final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final TemplateEngine engine = new TemplateEngine();

                final Request request = readRequest(in, out, engine);
                if (request != null) {
                    sendResponse(request, out, engine);
                }
            }
        }
    }

    private static Request readRequest(final BufferedReader in, final BufferedWriter out, final TemplateEngine engine) {
        try {
            final String requestLine = in.readLine();
            System.out.println(requestLine);
            final Request request = Request.parseRequest(requestLine);
            validateRequest(request, out, engine);
            return request;
        } catch (final Exception e) {
            System.out.println("readResponse() failed: " + e.getLocalizedMessage());
            return null;
        }
    }

    private static void validateRequest(final Request request, final BufferedWriter out, final TemplateEngine engine) throws IOException {
        if (!Objects.equals("GET", request.getMethod())) {
            final URL resourceUrl = HttpServer.class.getClassLoader().getResource("templates/405.html");
            if (resourceUrl != null) {
                final String templatePath = new File(resourceUrl.getFile()).getAbsolutePath();
                final String renderedContent = engine.render(templatePath, "");
                sendHttpResponse(out, 405, "405 Method Not Allowed", renderedContent);
            }
        }
    }

    private static void sendResponse(final Request request, final BufferedWriter out, final TemplateEngine engine) {
        try {
            final String path = request.getPath();

            if (path.startsWith("/hello/")) {
                renderNamedTemplate(path, engine, out);
            }
            if (path.endsWith("/")) {
                renderIndexTemplate(engine, out);
            }

            if (path.endsWith("/coffee")) {
                renderTeapotTemplate(engine, out);
            }

            final URL dynamicUrl = HttpServer.class.getClassLoader().getResource("templates" + path + ".html");
            if (dynamicUrl != null) {
                renderDynamicTemplate(path, engine, out);
            }

            renderNotFoundTemplate(path, engine, out);
        } catch (IOException e) {
            System.out.println("sendResponse() failed: " + e.getMessage());
        }
    }

    private static void renderNamedTemplate(final String path, final TemplateEngine engine, final BufferedWriter out) throws IOException {
        final String name = path.substring("/hello/".length());
        final String capitalizedName = name.substring(0, 1).toUpperCase() + name.substring(1);
        if (!capitalizedName.isEmpty()) {
            final URL resourceUrl = HttpServer.class.getClassLoader().getResource("templates/hello.html");
            if (resourceUrl != null) {
                final String templatePath = new File(resourceUrl.getFile()).getAbsolutePath();
                final String renderedContent = engine.render(templatePath, capitalizedName);
                sendHttpResponse(out, 200, "OK", renderedContent);
            }
        }
    }

    private static void renderIndexTemplate(final TemplateEngine engine, final BufferedWriter out) throws IOException {
        final URL resourceUrl = HttpServer.class.getClassLoader().getResource("templates/index.html");
        if (resourceUrl != null) {
            final String templatePath = new File(resourceUrl.getFile()).getAbsolutePath();
            final String renderedContent = engine.render(templatePath, "");
            sendHttpResponse(out, 200, "OK", renderedContent);
        }
    }

    private static void renderTeapotTemplate(final TemplateEngine engine, final BufferedWriter out) throws IOException {
        final URL resourceUrl = HttpServer.class.getClassLoader().getResource("templates/coffee.html");
        if (resourceUrl != null) {
            final String templatePath = new File(resourceUrl.getFile()).getAbsolutePath();
            final String renderedContent = engine.render(templatePath, "");
            sendHttpResponse(out, 418, "I'm a teapot", renderedContent);
        }
    }

    private static void renderDynamicTemplate(final String path, final TemplateEngine engine, final BufferedWriter out) throws IOException {
        final URL resourceUrl = HttpServer.class.getClassLoader().getResource("templates" + path + ".html");
        if (resourceUrl != null) {
            final String templatePath = new File(resourceUrl.getFile()).getAbsolutePath();
            final String renderedContent = engine.render(templatePath, "");
            sendHttpResponse(out, 200, "OK", renderedContent);
        }
    }

    private static void renderNotFoundTemplate(final String path, final TemplateEngine engine, final BufferedWriter out) throws IOException {
        final URL resourceUrl = HttpServer.class.getClassLoader().getResource("templates/404.html");
        if (resourceUrl != null) {
            final String templatePath = new File(resourceUrl.getFile()).getAbsolutePath();
            final String renderedContent = engine.render(templatePath, "");
            sendHttpResponse(out, 404, "Not Found", renderedContent);
        }
    }

    private static void sendHttpResponse(BufferedWriter out, int statusCode, String statusMessage, String content) throws IOException {
        out.write("HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n");
        out.write("Content-Type: text/html\r\n");
        out.write("Content-Length: " + content.length() + "\r\n");
        out.write("\r\n");
        out.write(content);
        out.flush();
    }

}
