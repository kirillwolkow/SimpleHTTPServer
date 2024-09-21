package engine;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class TemplateEngine implements HttpHandler {

    private Document template;

    public TemplateEngine() {

    }

    public String render(String templateName, String variable) {
        Parser parser = new Parser(templateName, variable);
        template = parser.parse();
        if (template != null) {
            return template.outerHtml();
        }
        return "";
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String templateContent = template.outerHtml();
        exchange.sendResponseHeaders(200, templateContent.length());
        OutputStream os = exchange.getResponseBody();
        os.write(templateContent.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
}
