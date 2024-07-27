package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;


public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        //
        server.addHandler("GET", "/classic.html", (request, responseStream) -> {
            Path filePath = Path.of(server.getFILE_PATH() + request.getPath());
            String mimeType = Files.probeContentType(filePath);
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            responseStream.write(content);
            responseStream.flush();
        });
        //
        server.addHandler("GET", "/26.jpg", (request, responseStream) -> {
            Path filePath = Path.of(server.getFILE_PATH() + request.getPath());
            String mimeType = Files.probeContentType(filePath);
            final var length = Files.size(filePath);
            responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, responseStream);
            responseStream.flush();
        });
        //
        server.addHandler("POST", "/26.jpg", (request, responseStream) -> {
            Path filePath = Path.of(server.getFILE_PATH() + request.getPath());
            String mimeType = Files.probeContentType(filePath);
            final var length = Files.size(filePath);
            responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, responseStream);
            responseStream.flush();
        });
        // добавляем хендлер для формы с вводом данных
        server.addHandler("GET", "/forms.html", (request, responseStream) -> {
            Path filePath = Path.of(server.getFILE_PATH() + request.getPath());
            String mimeType = Files.probeContentType(filePath);
            final var length = Files.size(filePath);
            responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, responseStream);
            responseStream.flush();
        });
        //
        server.addHandler("POST", "/forms.html", (request, responseStream) -> {
            Path filePath = Path.of(server.getFILE_PATH() + request.getPath());
            String mimeType = Files.probeContentType(filePath);
            final var length = Files.size(filePath);
            responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, responseStream);
            responseStream.flush();
        });
        //
        server.listen(9999);
    }
}
