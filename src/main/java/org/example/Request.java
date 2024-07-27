package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private Map<String, String> queryParams = new HashMap<>();
    private Map<String, List<String>> bodyParams = new HashMap<>();
    private String path;
    private String method;
    private List<String> headers;
    private String body;

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getBody() {
        return body;
    }

    public String getMethod() {
        return method;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getMethod_and_Header() {
        return method + path;
    }

    public void addQueryParam(String name, String value) {
        this.queryParams.put(name, value);
    }

    public void addBodyParam(Map<String, List<String>> map) {
        this.bodyParams = map;
    }


    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public List<String> getBodyParam(String name) {
        return bodyParams.get(name);
    }


}
