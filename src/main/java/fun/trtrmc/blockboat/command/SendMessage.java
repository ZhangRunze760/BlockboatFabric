package fun.trtrmc.blockboat.command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fun.trtrmc.blockboat.BlockboatFabric;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.java_websocket.WebSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;

//发送消息。整体原理非常简单，不过多赘述。
public class SendMessage {
    private final String group_id;
    private final String BOT_API_URL;

    public SendMessage(String group_id, String BOT_API_URL) {
        this.group_id = group_id;
        this.BOT_API_URL = BOT_API_URL;
    }

    public void sendMessageToGroup(String message) {
        SendMessageThread thread = new SendMessageThread(message);
        thread.start();
    }

    public void sendMCMessage(MinecraftServer server, String message, Sender sender) {
        if (BlockboatFabric.config.isQQSendEnabled) {
            String rawMessage;
            if (!Objects.equals(sender.getTitle(), "")) {
                if (Objects.equals(sender.getRole(), "member"))
                    rawMessage = String.format("*<%s§5[%s]§r> %s", sender.getCard(), sender.getTitle(), message);
                else if (Objects.equals(sender.getRole(), "admin"))
                    rawMessage = String.format("*<%s§2[%s]§r> %s", sender.getCard(), sender.getTitle(), message);
                else if (Objects.equals(sender.getRole(), "owner"))
                    rawMessage = String.format("*<%s§e[%s]§r> %s", sender.getCard(), sender.getTitle(), message);
                else return;
            }
            else {
                rawMessage = String.format("*<%s> %s", sender.getCard(), message);
            }
            Collection<ServerPlayerEntity> PlayerList = server.getPlayerManager().getPlayerList();
            for (ServerPlayerEntity player : PlayerList) {
                player.sendMessage(Text.literal(rawMessage));
            }
        }
    }

    public class SendMessageThread extends Thread {
        String message;
        WebSocket conn = BlockboatFabric.getQQMessage.getConn();
        public SendMessageThread(String message) { this.message = message; }
        public void run() {
            JsonObject json = new JsonObject();
            json.addProperty("action", "send_group_msg"); // 或 "send_group_msg" 根据需要修改
            JsonObject params = new JsonObject();
            params.addProperty("group_id", group_id); // 或 "group_id" 根据需要修改
            params.addProperty("message", message);
            json.add("params", params);

            Gson gson = new Gson();
            String jsonString = gson.toJson(json);

            if(conn != null) conn.send(jsonString);
        }
    }
}

