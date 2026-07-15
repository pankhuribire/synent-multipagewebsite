import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class StudentHelpHubServer {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8081),
                0
        );

        server.createContext(
                "/contact",
                StudentHelpHubServer::handleContactForm
        );

        server.start();

        System.out.println("Student Help Hub backend started...");
        System.out.println(
                "Server running at http://localhost:8081"
        );
    }

    private static void handleContactForm(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if (exchange.getRequestMethod()
                .equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(204, -1);

            exchange.close();

            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"message\":\"Only POST requests are allowed\"}"
            );

            return;
        }

        String requestBody = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Map<String, String> formData =
                parseFormData(requestBody);

        String name = formData.get("name");
        String email = formData.get("email");
        String subject = formData.get("subject");
        String message = formData.get("message");

        if (name == null ||
                email == null ||
                subject == null ||
                message == null ||
                name.isEmpty() ||
                email.isEmpty() ||
                subject.isEmpty() ||
                message.isEmpty()) {

            sendResponse(
                    exchange,
                    400,
                    "{\"message\":\"Please fill all fields\"}"
            );

            return;
        }

        String contactMessage =

                "----------------------------------------\n" +

                "Date: " + LocalDateTime.now() + "\n" +

                "Name: " + name + "\n" +

                "Email: " + email + "\n" +

                "Subject: " + subject + "\n" +

                "Message: " + message + "\n" +

                "----------------------------------------\n\n";

        Files.writeString(
                Path.of("messages.txt"),
                contactMessage,
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND
        );

        sendResponse(
                exchange,
                200,
                "{\"message\":\"Message submitted successfully\"}"
        );
    }

    private static Map<String, String> parseFormData(
            String requestBody
    ) {

        Map<String, String> data = new HashMap<>();

        String[] pairs = requestBody.split("&");

        for (String pair : pairs) {

            String[] keyValue = pair.split("=", 2);

            if (keyValue.length == 2) {

                String key = URLDecoder.decode(
                        keyValue[0],
                        StandardCharsets.UTF_8
                );

                String value = URLDecoder.decode(
                        keyValue[1],
                        StandardCharsets.UTF_8
                );

                data.put(key, value);
            }
        }

        return data;
    }

    private static void addCorsHeaders(
            HttpExchange exchange
    ) {

        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Methods",
                "POST, OPTIONS"
        );

        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Headers",
                "Content-Type"
        );

        exchange.getResponseHeaders().add(
                "Content-Type",
                "application/json"
        );
    }

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response
    ) throws IOException {

        byte[] responseBytes =
                response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length
        );

        OutputStream outputStream =
                exchange.getResponseBody();

        outputStream.write(responseBytes);

        outputStream.close();
    }
}