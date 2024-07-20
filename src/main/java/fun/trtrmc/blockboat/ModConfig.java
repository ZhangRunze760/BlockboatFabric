package fun.trtrmc.blockboat;

import com.google.gson.annotations.SerializedName;

//这里是Fabric API所规定的配置信息存放位置。这里是默认配置。
//如果需要添加的话，可以直接添加变量。

public class ModConfig {
    @SerializedName("isQQSendEnabled")
    public boolean isQQSendEnabled;
    @SerializedName("isMCSendEnabled")
    public boolean isMCSendEnabled;
    @SerializedName("qqGroupID")
    public String qqGroupID;
    @SerializedName("BOT_API_URL")
    public String BOT_API_URL;
    @SerializedName("WSPort")
    public int WSPort;
    @SerializedName("listenWhenStart")
    public boolean listenWhenStart;
    @SerializedName("ListenCommand")
    public boolean ListenCommand;
    public ModConfig() {
        this.isQQSendEnabled = true;
        this.isMCSendEnabled = true;
        this.qqGroupID = "123456";
        this.BOT_API_URL = "http://127.0.0.1:5700";
        this.WSPort = 6700;
        this.listenWhenStart = true;
        this.ListenCommand = false;
    }
}
