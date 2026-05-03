package model;

import java.util.Map;

public class RequestData {
    public String url;
    public String method;
    public String body;
    public Map<String, String> headers;

    public RequestData(String url, String method, String body, Map<String,String> headers) {
        this.url = url;
        this.method = method;
        this.body = body;
        this.headers = headers;
    }
}