package engine;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

public final class Parser {
    private final String templatePath;
    private final String variable;
    private Document output;

    public Parser(String templatePath, String variable) {
        this.templatePath = templatePath;
        this.variable = variable;
    }

    public Document parse() {
        try {
            if (Objects.equals(variable, "")) {
                File input = new File(templatePath);
                return Jsoup.parse(input);
            }

            String fileContent = new String(Files.readAllBytes(new File(templatePath).toPath()), StandardCharsets.UTF_8);
            output = Jsoup.parse(fileContent);
            return parseBlockQuotes(fileContent);

        } catch (IOException e) {
            System.out.println("Error from Parser.parse(): " + e.getMessage());
        }
        return null;
    }

    private Document parseBlockQuotes(String fileContent) {
        int startIndex = fileContent.indexOf("{{");
        int endIndex = fileContent.indexOf("}}");

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            output = injectVariable(fileContent, startIndex, endIndex);
        } else if (startIndex == -1 || endIndex == -1 && !Objects.equals(variable, "")) {
            throw new RuntimeException("No {{}} Block found for given variable value: " + variable);
        }

        return output;
    }

    private Document injectVariable(String fileContent, int startIndex, int endIndex) {
        String extractedContent = fileContent.substring(startIndex, endIndex + 2);
        fileContent = fileContent.replace(extractedContent, variable);
        return Jsoup.parse(fileContent);
    }
}
