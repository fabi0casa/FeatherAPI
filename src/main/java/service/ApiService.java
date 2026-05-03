package service;

import model.RequestData;
import java.net.URI;
import java.net.http.*;
import java.util.Map;

public class ApiService {

    private final HttpClient client = HttpClient.newHttpClient();

    public HttpResponse<String> send(RequestData data) throws Exception {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(data.url));

        for (Map.Entry<String, String> h : data.headers.entrySet()) {
            builder.header(h.getKey(), h.getValue());
        }

        switch (data.method) {
            case "POST":
                builder.POST(HttpRequest.BodyPublishers.ofString(data.body));
                break;
            case "PUT":
                builder.PUT(HttpRequest.BodyPublishers.ofString(data.body));
                break;
            case "DELETE":
                builder.DELETE();
                break;
            default:
                builder.GET();
        }

        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}