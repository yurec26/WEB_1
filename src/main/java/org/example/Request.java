package org.example;

public class Request {
    private final String method;
    private final String header;
    private String body;

    public Request(String method, String header) {
        this.method = method;
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    public String getMethod_and_Header() {
        return method + header;
    }

    public Request(String method, String header, String body) {
        this.method = method;
        this.header = header;
        this.body = body;
    }
}
