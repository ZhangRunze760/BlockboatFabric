package top.jsminecraft.blockboat.command;

import net.fabricmc.api.ModInitializer;
import okhttp3.*;

public class SendQQMessage implements ModInitializer {
    private final String group_id;
    private final String message;

    private static final String BOT_API_URL = "http://127.0.0.1:5700"; // gocqhttp 服务地址
    private static final OkHttpClient httpClient = new OkHttpClient();

    public SendQQMessage(String group_id, String message) {
        this.group_id = group_id;
        this.message = message;
    }

    @Override
    public void onInitialize() {
        // 在模组初始化时发送一条消息到 QQ 群组
        sendMessageToGroup(group_id, message);
    }

    private void sendMessageToGroup(String groupId, String message) {
        String apiUrl = BOT_API_URL + "/send_group_msg";

        RequestBody requestBody = new FormBody.Builder()
                .add("group_id", groupId)
                .add("message", message)
                .build();

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                if (response.body() != null) System.out.println("消息发送成功：" + response.body().string());
            } else {
                System.out.println("消息发送失败：" + response.message());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
