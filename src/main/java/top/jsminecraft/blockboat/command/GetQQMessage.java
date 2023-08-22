package top.jsminecraft.blockboat.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class GetQQMessage {

    private static final int PORT = 5710;


    public GetQQMessage() {
        startListening();
    }

    private void startListening() {
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/", new MessageHandler());
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static class MessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            NewMessageEvent.INSTANCE.invoker().sendBroadcastMessage(requestBody);

            byte[] response = "OK".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }
    }
}

