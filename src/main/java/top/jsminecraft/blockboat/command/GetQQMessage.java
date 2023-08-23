package top.jsminecraft.blockboat.command;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import top.jsminecraft.blockboat.BlockboatFabric;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
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
                if (sender.getCard() == null) {
                    if (requestBody.getMessage().startsWith("sudo ")) sendMessage.sendMessageToGroup(parseQQCommand(requestBody.getMessage(), sender));
                    else sendMessage.sendMCMessage(GetQQMessage.server, CQParse.replace(requestBody.getMessage(), sender.getCard()), sender.getCard());
                }
                else sendMessage.sendMCMessage(GetQQMessage.server, CQParse.replace(requestBody.getMessage(), sender.getNickname()), sender.getNickname());
            }
            byte[] response = "OK".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }
    }

    public static String parseQQCommand(String message, Sender sender) {
        String command = message.replace("sudo ", "");
        if (command.equals("list")) {
            Collection<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList();
            String playerListStr = "";
            for (ServerPlayerEntity player : playerList) playerListStr += player.getName().getString() + "\n";
            playerListStr = playerListStr.substring(0, playerListStr.lastIndexOf("\n"));
            return String.format("""
                        服务器当前在线人数：%d
                        在线玩家：%s
                        """, playerList.size(), playerListStr);
        } else if (Objects.equals(sender.getRole(), "admin")) {
            server.getCommandManager().execute(server.getCommandManager().getDispatcher().parse(command, server.getCommandSource()), command);
            return "发送成功！";
        }
        else return "发送失败！";
    }
}

@Getter
class JObject {
    public JObject(String post_type, String message_type, String message, Sender sender) {
        this.post_type = post_type;
        this.message_type = message_type;
        this.message = message;
        this.sender = sender;
    }

    public void setPost_type(String post_type) {
        this.post_type = post_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    private String post_type;
    private String message_type;
    private String message;
    private Sender sender;
}

@Getter
class Sender {
    public Sender(String nickname, String card, String role) {
        this.nickname = nickname;
        this.card = card;
        this.role = role;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public void setRole(String role) {
        this.role = role;
    }

    private String nickname;
    private String card;
    private String role;
}
