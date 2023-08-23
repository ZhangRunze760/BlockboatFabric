package top.jsminecraft.blockboat;

import com.google.gson.annotations.SerializedName;

public class ModConfig {
    @SerializedName("isQQSendEnabled")
    public boolean isQQSendEnabled = true;
    @SerializedName("isMCSendEnabled")
    public boolean isMCSendEnabled = true;
    @SerializedName("qqGroupID")
    public String qqGroupID = "123456";
    public String BOT_API_URL = "http://127.0.0.1:5700";
    public int HttpPostPort = 5710;

}
