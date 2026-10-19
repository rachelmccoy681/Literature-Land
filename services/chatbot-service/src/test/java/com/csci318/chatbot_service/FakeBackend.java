package com.csci318.chatbot_service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

// a local http server that stands in for the anthropic api and the other bookstore services during tests
class FakeBackend {

    record Reply(int status, String body) {
    }

    private final HttpServer server;

    // replies the fake claude api gives, in order, and the request bodies it received
    private final Queue<Reply> claudeReplies = new ConcurrentLinkedQueue<>();
    private final List<String> claudeRequests = new CopyOnWriteArrayList<>();

    // replies for bookstore service paths (path plus query), and the paths that were called
    private final Map<String, Reply> serviceReplies = new ConcurrentHashMap<>();
    private final List<String> serviceCalls = new CopyOnWriteArrayList<>();

    FakeBackend() {
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        server.createContext("/", this::handle);
        server.start();
    }

    String url() {
        return "http://localhost:" + server.getAddress().getPort();
    }

    void reset() {
        claudeReplies.clear();
        claudeRequests.clear();
        serviceReplies.clear();
        serviceCalls.clear();
    }

    void claudeReplies(String body) {
        claudeReplies.add(new Reply(200, body));
    }

    void claudeFails(int status, String body) {
        claudeReplies.add(new Reply(status, body));
    }

    void serviceReplies(String pathAndQuery, int status, String body) {
        serviceReplies.put(pathAndQuery, new Reply(status, body));
    }

    List<String> claudeRequests() {
        return claudeRequests;
    }

    List<String> serviceCalls() {
        return serviceCalls;
    }

    private void handle(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getRawQuery();
        String pathAndQuery = query == null ? path : path + "?" + query;

        Reply reply;

        if (path.endsWith("/messages")) {
            claudeRequests.add(body);
            reply = claudeReplies.poll();

            if (reply == null) {
                reply = new Reply(500, "{\"type\":\"error\",\"error\":{\"type\":\"api_error\",\"message\":\"no reply queued\"}}");
            }
        } else {
            serviceCalls.add(pathAndQuery);
            reply = serviceReplies.getOrDefault(pathAndQuery, new Reply(404, "{\"error\":\"not found\"}"));
        }

        byte[] bytes = reply.body().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(reply.status(), bytes.length);

        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    // builds a messages api response containing the given content blocks
    static String claudeMessage(String stopReason, String contentJson) {
        return """
                {"id": "msg_test", "type": "message", "role": "assistant", "model": "claude-haiku-5-5",
                 "content": %s, "stop_reason": "%s", "stop_sequence": null,
                 "usage": {"input_tokens": 100, "output_tokens": 20}}
                """.formatted(contentJson, stopReason);
    }

    static String textReply(String text) {
        return claudeMessage("end_turn", "[{\"type\": \"text\", \"text\": \"" + text + "\"}]");
    }

    static String toolCall(String id, String toolName, String inputJson) {
        return "{\"type\": \"tool_use\", \"id\": \"" + id + "\", \"name\": \"" + toolName + "\", \"input\": " + inputJson + "}";
    }
}
