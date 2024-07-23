package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    final static List<String> VALID_PATHS = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    final static Integer PORT = 9999;

    public static void main(String[] args) throws IOException {
        serverMain();
    }

    public static void serverMain() throws IOException {
        // стартуем сервер
        try (ServerSocket socket = new ServerSocket(PORT)) {
            System.out.println("start server");
            awaitingConnectionMainLoop(socket);
        }
    }

    public static void awaitingConnectionMainLoop(ServerSocket serverSocket) {
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
                            Path filePath = Path.of("src/main/resources" + path);
                            String mimeType = Files.probeContentType(filePath);
                            //
                            if (path.equals("/classic.html")) {
                                classic(out, filePath, mimeType);
                            } else {
                                def(out, filePath, mimeType);
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

    public static boolean invalidRequest_Check(String[] parts, BufferedOutputStream out, String path) throws IOException {
        boolean validReq = true;
        if (!VALID_PATHS.contains(path)) {
            invalidRequest(out);
            validReq = false;
        }
        return parts.length == 3 && validReq;
    }

    public static void invalidRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    public static void classic(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        final var template = Files.readString(filePath);
        final var content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
        ).getBytes();
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.write(content);
        out.flush();
    }

    public static void def(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        final var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }
}
