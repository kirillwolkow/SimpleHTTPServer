package server;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Request {

    private final String method;
    private final String path;

    private static String regex = "(GET|POST|PUT|DELETE|OPTIONS|HEAD|PATCH|TRACE|CONNECT)\\s(.*)\\s(HTTP.*)";

    private Request(final String method, final String path) {
        this.method = method;
        this.path = path;
    }

    public static Request parseRequest(final String request) throws ParseException {
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(request);

        if (matcher.matches()) {
            final String method = matcher.group(1);
            final String path = matcher.group(2);
            return new Request(method, path);
        } else {
            throw new ParseException("Request could not be parsed", 0);
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}
