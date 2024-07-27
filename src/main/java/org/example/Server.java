package org.example;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;


public class Server {
    private static final String enctype_App = "application/x-www-form-urlencoded";
    private static final String GET = "GET";
    
    private static final String POST = "POST";
    final List<String> allowedMethods = List.of(GET, POST);
    private final int limit = 4096;
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
                var in = new BufferedInputStream(socket.getInputStream());
                var out = new BufferedOutputStream(socket.getOutputStream());

                //  при мультипотоковом приложении сокет в трай с ресурсами сразу закрывается и не обрабатывает подключение
                // Socket closed, если вы засчитаете это как ошибку, в ответе ПОЖАЛЙСТА ОПИШИТЕ, как этого избежать при
                // использовании трай с ресурсами. спасибо.
                // upd. перечитал спецификацию и StackOverflow : нет возможности сохранить трай с ресурсами и обрабатывать
                // новое подключение в новом потоке, сокет сразу закрывается в ресурсах.

                threadPool.execute(new Thread(() -> {
                    //
                    in.mark(limit);
                    //
                    try {
                        final var buffer = new byte[limit];
                        final var read = in.read(buffer);
                        //
                        final var requestLineDelimiter = new byte[]{'\r', '\n'};
                        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
                        //
                        if (requestLineEnd == -1) {
                            invalidRequest(out);
                            Thread.currentThread().interrupt(); // если запрос неверный, прерываем поток.
                        } else {
                            final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
                            //
                            if (requestLine.length != 3) {
                                invalidRequest(out);
                                Thread.currentThread().interrupt(); // если запрос неверный, прерываем поток.
                            } else {
                                final var method = requestLine[0];
                                if (!allowedMethods.contains(method)) {
                                    invalidRequest(out);
                                    Thread.currentThread().interrupt(); //
                                    //
                                } else {
                                    // когда предварительные проверки пройдены, можем создавать объект запроса.
                                    Request request = new Request();
                                    request.setMethod(method); // добавляем в объект метод.
                                    System.out.println("method: " + method); // логируем метод.
                                    //
                                    final var pathTemp = requestLine[1];
                                    String surl = "http://localhost:9999" + pathTemp;
                                    URI uri = new URI(surl);
                                    if (!(uri.getQuery() == null)) {
                                        addQueryParam(uri, request);
                                    }
                                    final var path = uri.getPath();
                                    //
                                    if (!path.startsWith("/") || !VALID_PATHS.contains(path)) {
                                        invalidRequest(out);
                                        Thread.currentThread().interrupt(); //
                                    } else {
                                        //
                                        request.setPath(path); // добавляем в объект путь.
                                        System.out.println("path: " + path); // логируем путь.
                                        //
                                        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
                                        final var headersStart = requestLineEnd + requestLineDelimiter.length;
                                        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
                                        if (headersEnd == -1) {
                                            invalidRequest(out);
                                            Thread.currentThread().interrupt(); //
                                        } else {
                                            in.reset();
                                            in.skip(headersStart);
                                            final var headersBytes = in.readNBytes(headersEnd - headersStart);
                                            final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
                                            //
                                            request.setHeaders(headers); // // добавляем в объект заголовки.
                                            System.out.println("headers: " + headers); // логируем заголовки.
                                            //
                                            if (!method.equals(GET)) {
                                                in.skip(headersDelimiter.length);
                                                // вычитываем Content-Length, чтобы прочитать body
                                                final var contentLength = extractHeader(headers, "Content-Length");
                                                if (contentLength.isPresent()) {
                                                    final var length = Integer.parseInt(contentLength.get());
                                                    final var bodyBytes = in.readNBytes(length);
                                                    final var body = new String(bodyBytes);
                                                    System.out.println("body: " + body); // логируем тело.
                                                    request.setBody(body); // добавляем тело при наличии.
                                                    //  парсим тело
                                                    final var contentTypeTemp = extractHeader(headers, "Content-Type");
                                                    if (contentTypeTemp.isPresent()){
                                                        final String contentType = extractHeader(headers, "Content-Type").get();
                                                        if (contentType.equals(enctype_App)) {
                                                            //  парсим тело
                                                            addBodyParam(body, request);
                                                            System.out.println("logins: " + request.getBodyParam("login"));
                                                            System.out.println("passwords: " + request.getBodyParam("password"));
                                                        }
                                                    }
                                                }
                                            }
                                            if (handlers.containsKey(request.getMethod_and_Header())) {
                                                handlers.get(request.getMethod_and_Header()).handle(request, out);
                                            } else {
                                                System.out.println("handler not found");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private void addQueryParam(URI uri, Request request) {
        List<NameValuePair> listParsed = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
        System.out.println("11     " + listParsed);
        for (NameValuePair s : listParsed) {
            request.addQueryParam(s.getName(), s.getValue());
        }
    }

    private void addBodyParam(String body, Request request) {
        Map<String, List<String>> listMap = new HashMap<>();
        List<String> parsedBody1 = Arrays.stream(body.split("&")).toList();
        for (String s : parsedBody1) {
            String name = s.split("=")[0];
            String value = s.split("=")[1];
            if (listMap.containsKey(name)) {
                listMap.get(name).add(value);
            } else {
                List newValueList = new ArrayList();
                newValueList.add(value);
                listMap.put(name, newValueList);
            }
        }
        request.addBodyParam(listMap);
    }
}