package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final String FILE_PATH = "src/main/resources";
    private ConcurrentHashMap<String, Handler> handlers = new ConcurrentHashMap<>();
    private static final List<String> VALID_PATHS = List.of("/26.jpg", "/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public void listen(Integer port) throws IOException {
        serverMain(port);
    }

    public String getFILE_PATH() {
        return FILE_PATH;
    }

    private void serverMain(Integer port) throws IOException {
        // стартуем сервер
        try (ServerSocket socket = new ServerSocket(port)) {
            System.out.println("start server");
            awaitingConnectionMainLoop(socket);
        }
    }

    private void awaitingConnectionMainLoop(ServerSocket serverSocket) {
        ExecutorService threadPool = Executors.newFixedThreadPool(64);
        while (true) {
            try {
                var socket = serverSocket.accept();
                var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                var out = new BufferedOutputStream(socket.getOutputStream());
                threadPool.execute(new Thread(() -> {
                    System.out.println("new request received");
                    //
                    String requestLine;
                    try {
                        requestLine = in.readLine();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(requestLine);
                    String[] parts = requestLine.split(" ");
                    String path = parts[1];
                    //
                    try {
                        if (!invalidRequest_Check(parts, out, path)) {
                            System.out.println("invalid request");
                        } else {
                            Request request = new Request(parts[0], parts[1]);
                            if (handlers.containsKey(request.getMethod_and_Header())) {
                                handlers.get(request.getMethod_and_Header()).handle(request, out);
                            } else {
                                System.out.println("handler not found");
                            }
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean invalidRequest_Check(String[] parts, BufferedOutputStream out, String path) throws IOException {
        boolean validReq = true;
        if (!VALID_PATHS.contains(path)) {
            invalidRequest(out);
            validReq = false;
        }
        return parts.length == 3 && validReq;
    }

    private void invalidRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    public void addHandler(String method, String header, Handler handler) {
        handlers.put(method + header, handler);
    }
}