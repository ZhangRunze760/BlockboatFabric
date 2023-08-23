package top.jsminecraft.blockboat.command;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import top.jsminecraft.blockboat.BlockboatFabric;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Objects;

public class GetQQMessage {
    private static final int PORT = BlockboatFabric.config.HttpPostPort;
    public static BindManager bindManager = new BindManager();
    public static MinecraftServer server = null;

    public GetQQMessage() {
        startListening();
    }

    public static String parseQQCommand(String message, Sender sender) {
        String command = message.replace("sudo ", "");
        if (command.equals("list")) {
            Collection<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList();
            if (!Objects.equals(playerList.toString(), "[]")) {
                StringBuilder playerListStr = new StringBuilder();
                for (ServerPlayerEntity player : playerList)
                    playerListStr.append(player.getName().getString()).append("\n");
                playerListStr = new StringBuilder(playerListStr.substring(0, playerListStr.lastIndexOf("\n")));
                return String.format("""
                        服务器当前在线人数：%d
                        在线玩家：%s
                        """, playerList.size(), playerListStr);
            } else return "服务器当前在线人数：0";
        } else if (command.startsWith("bind ")) {
            if (command.replace("bind ", "").contains(" ")) return "不支持带有空格的游戏ID。";
            else {
                boolean result = bindManager.bind(sender.getUser_id(), command.replace("bind ", ""));
                if (result) return "绑定成功！";
                else return "绑定失败，ID已绑定。单个QQ号只能绑定一个ID。";
            }
        } else if (command.startsWith("unbind")) {
            boolean result = bindManager.unbindById(sender.getUser_id());
            if (result) return "解绑成功！";
            else return "解绑失败，账号没有绑定过ID。";
        } else if (command.startsWith("iist") || command.startsWith("Iist")) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
                player.sendMessage(Text.literal("§c米§e依§a吃§b屁"));
            return "[CQ:at,qq=3352452028] 米依吃屁";
        } else if (command.startsWith("chipi ")) {
            String chipier = command.replace("chipi ", "");
            switch (chipier) {
                case "cpx" -> {
                    return "吃屁筱屁吃";
                }
                case "米依", "米依M" -> {
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
                        player.sendMessage(Text.literal("§c米§e依§a吃§b屁"));
                    return "[CQ:at,qq=3352452028] 米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁米依吃屁";
                }
                case "红石", "zrz", "张润泽", "ZRZ" -> {
                    return "抱歉，不能给服务器技术人员吃屁。";
                }
                case "destiny", "天眼" -> {
                    return String.format("抱歉，不能给机器人吃屁，所以，%s吃屁。", sender.getCard());
                }
                default -> {
                    return chipier;
                }
            }
        } else if (Objects.equals(sender.getRole(), "admin")) {
            server.getCommandManager().execute(server.getCommandManager().getDispatcher().parse(command, server.getCommandSource()), command);
            return "发送成功！";
        } else return "发送失败！";
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
            if (Objects.equals(requestBody.getPost_type(), "message") && Objects.equals(requestBody.getMessage_type(), "group") && Objects.equals(requestBody.getGroup_id(), BlockboatFabric.config.qqGroupID)) {
                Sender sender = requestBody.getSender();
                if (sender.getCard() != null) {
                    if (requestBody.getMessage().startsWith("sudo "))
                        sendMessage.sendMessageToGroup(parseQQCommand(requestBody.getMessage(), sender));
                    else
                        sendMessage.sendMCMessage(GetQQMessage.server, CQParse.replaceCQ(requestBody.getMessage()), sender.getCard());
                } else
                    sendMessage.sendMCMessage(GetQQMessage.server, CQParse.replaceCQ(requestBody.getMessage()), sender.getNickname());
            } else if (Objects.equals(requestBody.getPost_type(), "message") && Objects.equals(requestBody.getMessage_type(), "private")) {
                Sender sender = requestBody.getSender();
                BlockboatFabric.LOGGER.info(String.format("收到了来自QQ号为%s、昵称为%s的用户的私信：%s", sender.getUser_id(), sender.getNickname(), requestBody.getMessage()));
            }
            byte[] response = "OK".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }
    }
}

@Getter
class JObject {
    private String post_type;
    private String message_type;
    private String message;
    private Sender sender;
    private String group_id;

    public JObject(String post_type, String message_type, String message, Sender sender, String group_id) {
        this.post_type = post_type;
        this.message_type = message_type;
        this.message = message;
        this.sender = sender;
        this.group_id = group_id;
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

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }
}

@Getter
class Sender {
    private String nickname;
    private String card;
    private String role;
    private String user_id;

    public Sender(String nickname, String card, String role, String user_id) {
        this.nickname = nickname;
        this.card = card;
        this.role = role;
        this.user_id = user_id;
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

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
