package top.jsminecraft.blockboat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.jsminecraft.blockboat.command.GetQQMessage;
import top.jsminecraft.blockboat.command.SendMessage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.minecraft.server.command.CommandManager.*;

public class BlockboatFabric implements ModInitializer {
    //获取log4j2的logManager。可以通过这里调用到MC服务端的日志处理程序。
    public static final Logger LOGGER = LogManager.getLogger();
    //获取Fabric的MinecraftServer实例。
    public static MinecraftServer server = null;
    //利用Fabric API获取配置文件路径。
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("blockboat.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    //Fabric API提供的模组配置对象，由此调用模组配置文件中的一切内容。
    public static ModConfig config;
    private static ServerCommandSource source;
    //实例化SendMessage。
    private SendMessage sendMessage;

    @Override
    public void onInitialize() {

        //机器人初始化配置并注册服务器生命周期事件开始
        //调用LOGGER，输出一段日志内容
        LOGGER.info("正在加载Blockboat...");
        //获取系统时间，达到计时的效果。注意这里单位为纳秒。
        long starttime = System.nanoTime();
        //利用Fabric API注册服务器的生命周期事件。发生时调用对应的方法。
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);
        //机器人初始化配置并注册服务器生命周期事件结束

        //配置文件注册开始
        config = loadConfig();
        sendMessage = new SendMessage(config.qqGroupID, config.BOT_API_URL);
        //配置文件注册结束

        //玩家登入登出事件注册开始
        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            //由此可以获取玩家的名字，注意getName()方法返回的是Minecraft中的Text类型，需要进行转换，利用的是Text类的getString()方法而非toString()方法。
            String playerName = handler.getPlayer().getName().getString();
            //显然这里可以发送群消息，调用了sendMessage对象的发生群消息方法。
            sendMessage.sendMessageToGroup(String.format("%s 加入了游戏", playerName));
        }));
        ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) -> {
            //同上。
            String playerName = handler.getPlayer().getName().getString();
            sendMessage.sendMessageToGroup(String.format("%s 退出了游戏", playerName));
        }));
        //玩家登入登出事件注册结束

        //配置消息接收器开始
        //实例化，同上。
        GetQQMessage getQQMessage = new GetQQMessage();
        ServerMessageEvents.CHAT_MESSAGE.register(this::onServerChatMessage);
        //配置消息接收器结束

        //qqbot命令注册开始
        //调用了Fabric API的执行命令事件，当玩家执行了这个命令时触发。
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                //注册命令。这里提供了主命令的字符串（即“qqbot”），注意要转换为Minecraft中的Text类型。
                //这里提供的字符串是主命令的一个可选参数。如果要执行者自定义发生参数，添加方法见下。
                dispatcher.register(literal("qqbot")
                                //credits子命令注册开始
                                //这里可以添加子命令，添加方法见下。
                                //提供字符串的方法和上面一样，同样是将String转换为Text。
                                .then(literal("credits")
                                        //这里是需要执行的方法。具体用法见下。
                                        .executes(context -> {
                                            //context.getSource()显然是获取命令发送源，sendMessage()方法可以向发送源发生一段字符串。
                                            //这里仍然需要提供Text而非String类型。
                                            //当然，如果是在服务端控制台发送，那这里的消息当然就是作为命令回显显示，其他发生方法（如Rcon）同理。
                                            context.getSource().sendMessage(Text.literal("==================================================="));
                                            context.getSource().sendMessage(Text.literal("               §6Blockboat QQ互通机器人"));
                                            context.getSource().sendMessage(Text.literal("                  作者：ZhangRunze760"));
                                            context.getSource().sendMessage(Text.literal("项目地址：https://github.com/ZhangRunze760/BlockboatFabric"));
                                            context.getSource().sendMessage(Text.literal("             Apache License, Version 2.0"));
                                            context.getSource().sendMessage(Text.literal("                 All Rights Reserved."));
                                            context.getSource().sendMessage(Text.literal("==================================================="));
                                            return 0;
                                        }))
                                //credits子命令注册结束

                                //config子命令注册开始
                                //同上。
                                .then(literal("config")
                                        //子命令当中当然也可以继续添加子命令。
                                        .then(literal("isQQSendEnabled")
                                                //在这里可以自定义玩家提供的参数类型。这里显然是boolean类型。
                                                .then(CommandManager.argument("value", BoolArgumentType.bool())
                                                        .executes(context -> {
                                                            //这里调用了Fabric API的config对象。调用方法见下。
                                                            //BoolAgrumentType.getBool是为了获取前面所要求玩家输入的值。这里是boolean类型。
                                                            //这里的value相当于是变量名，显示在MC命令的提示当中。
                                                            config.isQQSendEnabled = BoolArgumentType.getBool(context, "value");
                                                            //调用了下面定义的saveConfig()方法以保存配置文件。
                                                            saveConfig(config);
                                                            context.getSource().sendMessage(Text.literal("成功设置"));
                                                            return 0;
                                                        }))
                                                .executes(context -> {
                                                    context.getSource().sendMessage(Text.literal(String.format("isQQSendEnabled = %s", config.isQQSendEnabled)));
                                                    return 1;
                                                }))
                                        //用法见上。
                                        .then(literal("isMCSendEnabled")
                                                .then(CommandManager.argument("value", BoolArgumentType.bool())
                                                        .executes(context -> {
                                                            config.isMCSendEnabled = BoolArgumentType.getBool(context, "value");
                                                            saveConfig(config);
                                                            context.getSource().sendMessage(Text.literal("成功设置"));
                                                            return 0;
                                                        }))
                                                .executes(context -> {
                                                    context.getSource().sendMessage(Text.literal(String.format("isMCSendEnabled = %s", config.isMCSendEnabled)));
                                                    return 1;
                                                }))
                                        .then(literal("QQGroupID")
                                                .then(CommandManager.argument("value", StringArgumentType.string())
                                                        .executes(context -> {
                                                            config.qqGroupID = StringArgumentType.getString(context, "value");
                                                            saveConfig(config);
                                                            context.getSource().sendMessage(Text.literal("成功设置"));
                                                            return 0;
                                                        }))
                                                .executes(context -> {
                                                    context.getSource().sendMessage(Text.literal(String.format("QQGroupID = %s", config.qqGroupID)));
                                                    return 1;
                                                }))
                                        .then(literal("BOT_API_URL")
                                                .then(CommandManager.argument("value", StringArgumentType.string())
                                                        .executes(context -> {
                                                            config.BOT_API_URL = StringArgumentType.getString(context, "value");
                                                            saveConfig(config);
                                                            context.getSource().sendMessage(Text.literal("成功设置"));
                                                            return 0;
                                                        }))
                                                .executes(context -> {
                                                    context.getSource().sendMessage(Text.literal(String.format("BOT_API_URL = %s", config.BOT_API_URL)));
                                                    return 1;
                                                }))
                                        .then(literal("HttpPostPort")
                                                .then(CommandManager.argument("value", IntegerArgumentType.integer(0, 65535))
                                                        .executes(context -> {
                                                            config.HttpPostPort = IntegerArgumentType.getInteger(context, "value");
                                                            saveConfig(config);
                                                            context.getSource().sendMessage(Text.literal("成功设置"));
                                                            return 0;
                                                        }))
                                                .executes(context -> {
                                                    context.getSource().sendMessage(Text.literal(String.format("HttpPostPort = %d", config.HttpPostPort)));
                                                    return 1;
                                                }))
                                        .executes(context -> {
                                            context.getSource().sendMessage(Text.literal("§4不完整的命令"));
                                            return 1;
                                        }))
                                .executes(context -> {
                                    context.getSource().sendMessage(Text.literal("§4不完整的命令"));
                                    return 1;
                                })
                                //config子命令注册结束

                                .then(literal("reload").executes(context -> {
                                    config = loadConfig();
                                    return 0;
                                }))

                                //sendMessage子命令注册开始
                                .then(literal("sendMessage")
                                        //这里可以定义命令所需要的命令源的OP权限级别。这是MC原版的一个特性，比如说这里如果是3级权限，那就无法执行这个命令。
                                        //当然，服务端控制台的权限等级为4即最高，非OP玩家等级为0。
                                        //server.properties指出，OP玩家的默认权限等级为4。
                                        .requires(source -> source.hasPermissionLevel(4))
                                        .then(CommandManager.argument("message", StringArgumentType.string())
                                                .executes(context -> {
                                                    sendMessage.sendMessageToGroup(StringArgumentType.getString(context, "message"));
                                                    context.getSource().sendMessage(Text.literal("发送成功！"));
                                                    return 0;
                                                }))
                                        .executes(context -> {
                                            context.getSource().sendMessage(Text.literal("§4不完整的命令"));
                                            return 0;
                                        }))
                        //sendMessage子命令注册结束

                ));
        //qqbot命令注册结束

        //badapple!!命令注册开始
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("badapple!!").executes(context -> {
                context.getSource().sendMessage(Text.literal("Bad Apple!!播放功能正在开发（"));
                return 0;
            }));
        }));
        //badapple!!命令注册结束

        //再次获取系统时间，完成计时。
        double pass = (System.nanoTime() - starttime) / 1e9;
        LOGGER.info(String.format("加载完成！耗时：%.2f秒。", pass));
    }
    //定义各个事件响应方法开始
    //这里是当玩家发送消息时所调用的方法。具体不过多赘述。
    private void onServerChatMessage(SignedMessage signedMessage, ServerPlayerEntity player, MessageType.Parameters parameters) {
        String message = signedMessage.getContent().getString();
        String sender = player.getName().getString();
        if (config.isMCSendEnabled) sendMessage.sendMessageToGroup(String.format("<%s> %s", sender, message));
    }

    private void onServerStarting(MinecraftServer server) {
        BlockboatFabric.server = server;
        GetQQMessage.server = server;
        sendMessage.sendMessageToGroup("服务器正在开启...");
    }

    private void onServerStarted(MinecraftServer server) {
        sendMessage.sendMessageToGroup("服务器开启成功！");
    }

    private void onServerStopping(MinecraftServer server) {
        sendMessage.sendMessageToGroup("服务器正在关闭...");
    }

    private void onServerStopped(MinecraftServer server) {
        sendMessage.sendMessageToGroup("服务器已关闭。");
    }
    //定义各个事件响应方法结束

    //定义模组配置文件操作开始
    private ModConfig loadConfig() {
        if (!Files.exists(CONFIG_PATH)) {
            saveConfig(new ModConfig());
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            return GSON.fromJson(reader, ModConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new ModConfig();
        }
    }

    private void saveConfig(ModConfig config) {
        Writer writer;
        try {
            writer = Files.newBufferedWriter(CONFIG_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GSON.toJson(config, writer);
    }
    //定义模组配置文件操作结束
}
