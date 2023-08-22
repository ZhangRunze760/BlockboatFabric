package top.jsminecraft.blockboat;

import com.google.gson.annotations.SerializedName;

public class ModConfig {
    @SerializedName("isQQSendEnabled")
    public boolean isQQSendEnabled = true;
    @SerializedName("isMCSendEnabled")
    public boolean isMCSendEnabled = true;
    @SerializedName("qqGroupID")
    public String qqGroupID = "123456";

}
