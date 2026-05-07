package service;

import model.RequestData;
import java.net.URI;
import java.net.http.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ApiService {

    private final HttpClient client = HttpClient.newHttpClient();

    public HttpResponse<String> send(RequestData data) throws Exception {
        return client.send(prepareRequest(data), HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> sendAsync(RequestData data) {
        return client.sendAsync(prepareRequest(data), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest prepareRequest(RequestData data) {
        String url = data.url.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url));

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
            case "PATCH":
                builder.method("PATCH", HttpRequest.BodyPublishers.ofString(data.body));
                break;
            default:
                builder.GET();
        }
        return builder.build();
    }
}