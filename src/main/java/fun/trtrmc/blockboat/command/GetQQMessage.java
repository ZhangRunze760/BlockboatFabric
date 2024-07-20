package fun.trtrmc.blockboat.command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fun.trtrmc.blockboat.BlockboatFabric;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Objects;

//获取QQ消息。
public class GetQQMessage {
    private static final int PORT = BlockboatFabric.config.WSPort;
    public static BindManager bindManager = new BindManager("config/blockboat-bind.json");
    public static MinecraftServer server = null;
    private static final Logger LOGGER = LogManager.getLogger();
    private final SendMessage sendMessage = BlockboatFabric.sendMessage;

    private static QQBot bot;
    public GetQQMessage() {
        startListening();
    }

    @SneakyThrows
    public static String parseQQCommand(String message, Sender sender) {
        //利用字符串处理QQ发生的命令。具体原理十分简单。
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
        }
        else if (command.startsWith("bind ")) {
            if (command.replace("bind ", "").contains(" ")) return "不支持带有空格的游戏ID。";
            else {
                boolean result = bindManager.bind(sender.getUser_id(), command.replace("bind ", ""));
                if (result) {
                    String bindcmd1 = "easywhitelist add " + command.replace("bind ", "");
                    String bindcmd2 = "whitelist reload";
                    server.getCommandManager().execute(server.getCommandManager().getDispatcher().parse(command, server.getCommandSource()), bindcmd1);
                    server.getCommandManager().execute(server.getCommandManager().getDispatcher().parse(command, server.getCommandSource()), bindcmd2);
                    return "绑定成功！";
                }
                else return "绑定失败，ID已绑定。单个QQ号只能绑定一个ID。";
            }
        } else if (command.startsWith("unbind")) {
            boolean result = bindManager.unbindById(sender.getUser_id());
            if (result) return "解绑成功！";
            else return "解绑失败，账号没有绑定过ID。";
        } else if (Objects.equals(sender.getRole(), "admin") || Objects.equals(sender.getRole(), "owner")) {
            server.getCommandManager().execute(server.getCommandManager().getDispatcher().parse(command, server.getCommandSource()), command);
            return "发送成功！";
        } else return "发送失败，权限不够！";
    }

    private void startListening() {
        bot = new QQBot(new InetSocketAddress(PORT));
        bot.start();
    }

    public static void stopListening() throws InterruptedException {
        bot.stop();
    }

    private class QQBot extends WebSocketServer {
        private static final Gson gson = new Gson();

        public QQBot(InetSocketAddress serverAddress) {
            super(serverAddress);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshakedata) {
            LOGGER.info("WebSocket连接已建立！");
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            LOGGER.info("WebSocket服务器已关闭。");
        }

        @Override
        public void onMessage(WebSocket conn, String message) {

            LOGGER.info("WebSocket收到消息：" + message);

            JsonObject json = gson.fromJson(message, JsonObject.class);
            String postType = json.get("post_type").getAsString();
            String messageType = postType.equals("message") ? json.get("message_type").getAsString() : null;

            if (postType.equals("message") && messageType.equals("group")) {
                String userId = json.get("user_id").getAsString();
                String msg = json.get("raw_message").getAsString();
                JsonObject sender = json.get("sender").getAsJsonObject();

                String nickname = sender.get("nickname").getAsString();
                String card = sender.get("card").getAsString();
                String role = sender.get("role").getAsString();


                String parsedMsg = CQParse.replaceCQ(msg);

                Sender senderFormat = new Sender(nickname, card, role, userId);

                if (parsedMsg.startsWith("sudo ")) {
                   String returnMsg = parseQQCommand(parsedMsg, senderFormat);
                   sendMessage.sendMessageToGroup(returnMsg);
                }
                sendMessage.sendMCMessage(server, parsedMsg, nickname);
            }
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public void onStart() {
            LOGGER.info("WebSocket服务器已开启！");
        }
    }
}

@Getter
@Setter
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
}

@Getter
@Setter
class Sender {
    private String nickname;
    private String card;
    @Getter
    private String role;
    @Getter
    private String user_id;

    public Sender(String nickname, String card, String role, String user_id) {
        this.nickname = nickname;
        this.card = card;
        this.role = role;
        this.user_id = user_id;
    }
}

