package top.jsminecraft.blockboat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import top.jsminecraft.blockboat.command.GetQQMessage;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.minecraft.server.command.CommandManager.*;

public class BlockboatFabric implements ModInitializer {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("blockboat.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static ModConfig config;
    private static ServerCommandSource source;
    @Override
    public void onInitialize() {
        //配置文件注册开始
        config = loadConfig();
        //配置文件注册结束

        //配置消息接收器
        GetQQMessage getQQMessage = new GetQQMessage();
        //NewMessageEvent.INSTANCE.register(this::sendBroadcastMessage);
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
                                .then(literal("test")
                                        .executes(context -> {
                                            source = context.getSource().getServer().getCommandSource();
                                            Iterable<ServerPlayerEntity> playerEntities = source.getServer().getPlayerManager().getPlayerList();
                                            for (ServerPlayerEntity player : playerEntities) {
                                                player.sendMessage(Text.literal("This is a test"));
                                                player.sendMessage(Text.literal(context.getSource().getServer().toString()));
                                            }
                                            return 0;
                                        }))
                        .then(literal("config")
                                .executes(context -> {

                                    return 0;
                                }))
                        .executes(context -> {
                            context.getSource().sendMessage(Text.literal("我是憨批机器人"));
                            return 0;
                        })
                ));
        //qqbot命令注册结束
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
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
