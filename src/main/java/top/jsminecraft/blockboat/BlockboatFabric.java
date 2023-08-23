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
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.jsminecraft.blockboat.command.GetQQMessage;
import top.jsminecraft.blockboat.command.SendMessage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.*;

public class BlockboatFabric implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static MinecraftServer server = null;
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("blockboat.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static ModConfig config;
    private static ServerCommandSource source;
    public static final Identifier CHAT_CHANNEL = new Identifier("blockboat", "chat");
    private final Map<ServerPlayerEntity, String> playerChatMap = new HashMap<>();
    private SendMessage sendMessage;
    @Override
    public void onInitialize() {

        LOGGER.info("正在加载Blockboat...");
        long starttime = System.nanoTime();
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarting);

        //配置文件注册开始
        config = loadConfig();
        sendMessage = new SendMessage(config.qqGroupID, config.BOT_API_URL);
        //配置文件注册结束

        //玩家登入登出事件注册开始
        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            String playerName = handler.getPlayer().getName().getString();
            sendMessage.sendMessageToGroup(String.format("%s 加入了游戏"));
        }));

        ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) -> {
            String playerName = handler.getPlayer().getName().getString();
            sendMessage.sendMessageToGroup(String.format("%s 退出了游戏"));
        }));
        //玩家登入登出事件注册结束

        //配置消息接收器开始
        GetQQMessage getQQMessage = new GetQQMessage();
        ServerMessageEvents.CHAT_MESSAGE.register(this::onServerChatMessage);
        //配置消息接收器结束

        //qqbot命令注册开始
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("qqbot")
                        //credits子命令注册开始
                        .then(literal("credits")
                                .executes(context -> {
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
                        .then(literal("config")
                                .then(literal("isQQSendEnabled")
                                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    config.isQQSendEnabled = BoolArgumentType.getBool(context, "value");
                                                    saveConfig(config);
                                                    context.getSource().sendMessage(Text.literal("成功设置"));
                                                    return 0;
                                                }))
                                        .executes(context -> {
                                            context.getSource().sendMessage(Text.literal(String.format("isQQSendEnabled = %s", config.isQQSendEnabled)));
                                            return 1;
                                        }))
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

                        //sendMessage子命令注册开始
                        .then(literal("sendMessage")
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

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            literal("badapple!!").executes(context -> {
                context.getSource().sendMessage(Text.literal("Bad Apple!!播放功能正在开发（"));
                return 0;
            });
        }));

        double pass = (System.nanoTime() - starttime) / 1e9;
        LOGGER.info(String.format("加载完成！耗时：%.2f秒。",pass));
    }

    private void onServerChatMessage(SignedMessage signedMessage, ServerPlayerEntity player, MessageType.Parameters parameters) {
        String message = signedMessage.getContent().getString();
        String sender = player.getName().getString();
        sendMessage.sendMessageToGroup(String.format("<%s> %s", sender, message));
    }

    private void onServerStarting(MinecraftServer server) {
        BlockboatFabric.server = server;
        GetQQMessage.server = server;
    }

    private void setPlayerChatMessage(ServerPlayerEntity player, String message) {
        playerChatMap.put(player, message);
    }

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

    private void sendCommand(String command) {
        server.getCommandManager().execute(server.getCommandManager().getDispatcher().parse(command, source), command);
    }

    private void onPlayerLoggedOn() {

    }
}
