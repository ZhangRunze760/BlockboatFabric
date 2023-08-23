package top.jsminecraft.blockboat.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CQParse {
    static BindManager bindManager = new BindManager();
    public static String replaceCQ(String rawMessage) {
        BindManager bindManager = new BindManager();
        String regex = "\\[CQ:(\\w+)(,[^\\]]+)?\\]"; // 正则表达式匹配CQ码
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(rawMessage);

        StringBuffer resultBuffer = new StringBuffer();
        while (matcher.find()) {
            String cqCode = matcher.group();
            String messageType = matcher.group(1);
            String messageParams = "";
            if (matcher.groupCount() > 1) messageParams = matcher.group(2);
            switch (messageType) {
                case "at":
                    String qqID = extractQQNumber(messageParams);
                    if(bindManager.IsIdBind(qqID)) matcher.appendReplacement(resultBuffer, "@" + bindManager.getNameById(qqID) + " ");
                    else matcher.appendReplacement(resultBuffer, "[at]");
                    break;
                case "image":
                    matcher.appendReplacement(resultBuffer, "【图片】");
                    break;
                case "reply":
                    matcher.appendReplacement(resultBuffer, "【回复】");
                    break;
                case "record":
                    matcher.appendReplacement(resultBuffer, "【语音】");
                    break;
                case "forward":
                    matcher.appendReplacement(resultBuffer, "【合并转发】");
                    break;
                case "video":
                    matcher.appendReplacement(resultBuffer, "【视频】");
                    break;
                case "music":
                    matcher.appendReplacement(resultBuffer, "【音乐】");
                    break;
                case "redbag":
                    matcher.appendReplacement(resultBuffer, "【红包】");
                    break;
                case "face":
                    matcher.appendReplacement(resultBuffer, "【表情】");
                    break;
                default:
                    matcher.appendReplacement(resultBuffer, "【未知】");
                    break;
            }
        }
        matcher.appendTail(resultBuffer);
        return resultBuffer.toString();
    }

    private static String extractQQNumber(String params) {
        String[] keyValuePairs = params.split(",");
        for (String pair : keyValuePairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 && "qq".equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return "";
    }
}