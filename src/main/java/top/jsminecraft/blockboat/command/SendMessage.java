package top.jsminecraft.blockboat.command;

import net.minecraft.server.MinecraftServer;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class SendMessage {
    private final String group_id;
    private final String BOT_API_URL;
    private static final OkHttpClient httpClient = new OkHttpClient();

    public SendMessage(String group_id, String BOT_API_URL) {
        this.group_id = group_id;
        this.BOT_API_URL = BOT_API_URL;
    }

    public void sendMessageToGroup(String message) {
        String apiUrl = BOT_API_URL + "/send_group_msg";
        String GETBody;
        GETBody = String.format("""
                ?group_id=%s&message=%s
                """,group_id, URLEncoder.encode(message, StandardCharsets.UTF_8));
        try {
            HTTPGET(apiUrl + GETBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMCMessage(MinecraftServer server, String message, String sender) {
        String command = String.format("""
        tellraw @a {"text":"*<%s> %s","color":"yellow"}
        """,sender, message);
        server.getCommandManager().execute(server.getCommandManager().getDispatcher().parse(command, server.getCommandSource()), command);
    }

    private static void HTTPGET(String url) throws IOException {
        String content = null;
        URLConnection urlConnection = new URL(url).openConnection();
        HttpURLConnection connection = (HttpURLConnection) urlConnection;
        connection.setRequestMethod("GET");
        //连接
        connection.connect();
        //得到响应码
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
    }
}
