package fun.jsserver.blockboat.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import fun.jsserver.blockboat.BlockboatFabric;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

//发送消息。整体原理非常简单，不过多赘述。
public class SendMessage {
    private final String group_id;
    private final String BOT_API_URL;

    public SendMessage(String group_id, String BOT_API_URL) {
        this.group_id = group_id;
        this.BOT_API_URL = BOT_API_URL;
    }

    private static void HTTPGET(String url) throws IOException {
        URLConnection urlConnection = new URL(url).openConnection();
        HttpURLConnection connection = (HttpURLConnection) urlConnection;
        connection.setRequestMethod("GET");
        try {
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader
                        (connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder bs = new StringBuilder();
                String l;
                while ((l = bufferedReader.readLine()) != null) {
                    bs.append(l).append("\n");
                }
            }
        } catch (ConnectException e) {
            BlockboatFabric.LOGGER.warn("无法连接至CQHTTP，机器人网络模块将不会启动。请修改配置后再次尝试。");
        }
    }

    public void sendMessageToGroup(String message) {
        SendMessageThread thread = new SendMessageThread(message);
        thread.start();
    }

    public void sendMCMessage(MinecraftServer server, String message, String sender) {
        if (BlockboatFabric.config.isQQSendEnabled) {
            String rawMessage = String.format("*<%s> %s", sender, message);
            Collection<ServerPlayerEntity> PlayerList = server.getPlayerManager().getPlayerList();
            for (ServerPlayerEntity player : PlayerList) {
                player.sendMessage(Text.literal(rawMessage));
            }
        }
    }

    public class SendMessageThread extends Thread {
        String message;
        public SendMessageThread(String message) { this.message = message; }
        public void run() {
            String apiUrl = BOT_API_URL + "/send_group_msg";
            String GETBody;
            GETBody = String.format("""
                ?group_id=%s&message=%s
                """, group_id, URLEncoder.encode(message, StandardCharsets.UTF_8));
            try {
                HTTPGET(apiUrl + GETBody);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

