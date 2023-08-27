package top.jsminecraft.blockboat;

import com.google.gson.annotations.SerializedName;

//这里是Fabric API所规定的配置信息存放位置。这里是默认配置。
//如果需要添加的话，可以直接添加变量。

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
