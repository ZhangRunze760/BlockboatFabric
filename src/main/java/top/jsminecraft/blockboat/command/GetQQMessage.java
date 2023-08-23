package top.jsminecraft.blockboat.command;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.server.MinecraftServer;
import top.jsminecraft.blockboat.BlockboatFabric;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;

public class GetQQMessage {
    public static MinecraftServer server = null;
    private static final int PORT = BlockboatFabric.config.HttpPostPort;
    public GetQQMessage()
    {
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
        public SendMessage sendMessage = new SendMessage(BlockboatFabric.config.qqGroupID, BlockboatFabric.config.BOT_API_URL);
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Gson jObject = new Gson();
            String request = new String(exchange.getRequestBody().readAllBytes());
            JObject requestBody = jObject.fromJson(request, JObject.class);
            if (Objects.equals(requestBody.getPost_type(), "message")) {
                Sender sender = requestBody.getSender();
                sendMessage.sendMCMessage(GetQQMessage.server, parseCQCode(requestBody.getMessage()), sender.getCard());
            }
            byte[] response = "OK".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }
    }

    public static String parseQQCommand(String message) {
        if (message.startsWith("sudo ")) {
            String command = message.replace("sudo ", "");
            if (command == "list") {

            }
        }
        return message;
    }

    public static String parseCQCode(String cqMessage) {
        // 处理图片
        cqMessage = cqMessage.replaceAll("\\[CQ:image,file=(.*?)\\]", "【图片】");
        // 处理@消息
        cqMessage = cqMessage.replaceAll("\\[CQ:at,qq=(.*?)\\]", "@[$1]");
        //处理语音
        cqMessage = cqMessage.replaceAll("\\[CQ:record,(.*?)\\]", "【语音】");
        //处理回复
        cqMessage = cqMessage.replaceAll("\\[CQ:reply,text=(.*?)\\]", "【回复】");
        return cqMessage;
    }
}

class JObject {
    public JObject(String post_type, String message_type, String message, Sender sender) {
        this.post_type = post_type;
        this.message_type = message_type;
        this.message = message;
        this.sender = sender;
    }

    public String getPost_type() {
        return post_type;
    }

    public void setPost_type(String post_type) {
        this.post_type = post_type;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    private String post_type;
    private String message_type;
    private String message;
    private Sender sender;
}

class Sender {
    public Sender(String nickname, String card, String role) {
        this.nickname = nickname;
        this.card = card;
        this.role = role;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    private String nickname;
    private String card;
    private String role;
}
