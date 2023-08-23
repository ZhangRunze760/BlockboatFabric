package top.jsminecraft.blockboat.command;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.val;
public class CQParse {

    private final static String CQ_CODE_SPLIT = "(?<=\\[CQ:[^]]{1,99999}])|(?=\\[CQ:[^]]{1,99999}])";

    private final static String CQ_CODE_REGEX = "\\[CQ:(.*?),(.*?)]";


    public static boolean hasImg(String msg) {
        String regex = "\\[CQ:image,[(\\s\\S)]*\\]";
        val p = Pattern.compile(regex);
        val m = p.matcher(msg);
        return m.find();
    }

    public static String replace(String msg, String sender) {
        if (!msg.contains("[CQ:") && !msg.contains("]")) return msg;
        else {
            final ExecutorService exec = Executors.newSingleThreadExecutor();
            String back = "";
            StringBuffer message = new StringBuffer();
            Pattern pattern = Pattern.compile(CQ_CODE_REGEX);
            Matcher matcher = pattern.matcher(msg);

            val call = new FutureTask<>(() -> {
                while (matcher.find()) {//全局匹配
                    val type = matcher.group(1);
                    val data = matcher.group(2);
                    switch (type) {
                        case "image": {
                            matcher.appendReplacement(message, "[图片]");
                            break;
                        }
                        case "at":
                            val id = Arrays.stream(data.split(","))//具体数据分割
                                    .filter(it -> it.startsWith("qq"))//非空判断
                                    .map(it -> it.substring(it.indexOf('=') + 1))
                                    .findFirst();
                            if (id.isPresent()) {
                                matcher.appendReplacement(message, String.format("[@%s]", sender));
                            } else {
                                matcher.appendReplacement(message, "[@]");
                            }
                            break;
                        case "record":
                            matcher.appendReplacement(message, "[语音]");
                            break;
                        case "forward":
                            matcher.appendReplacement(message, "[合并转发]");
                            break;
                        case "video":
                            matcher.appendReplacement(message, "[视频]");
                            break;
                        case "music":
                            matcher.appendReplacement(message, "[音乐]");
                            break;
                        case "redbag":
                            matcher.appendReplacement(message, "[红包]");
                            break;
                        case "face":
                            matcher.appendReplacement(message, "[表情]");
                            break;
                        case "reply":
                            matcher.appendReplacement(message, "[回复]");
                            break;
                        default:
                            matcher.appendReplacement(message, "[?]");
                            break;
                    }
                }
                matcher.appendTail(message);
                return message.toString();
            });
            return back;
        }
    }
}