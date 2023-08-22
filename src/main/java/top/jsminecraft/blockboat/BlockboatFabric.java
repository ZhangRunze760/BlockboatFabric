package top.jsminecraft.blockboat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

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
    private String host;
    private int port;
    @Override
    public void onInitialize() {
        //配置文件注册开始
        config = loadConfig();
        //配置文件注册结束

        //qqbot命令注册开始
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("qqbot")
                                //credits子命令注册开始
                                .then(literal("credits")
                                        .executes(context -> {
                                            context.getSource().sendMessage(Text.literal("==================================================="));
                                            context.getSource().sendMessage(Text.literal("               §6Blockboat QQ互通机器人"));
                                            context.getSource().sendMessage(Text.literal("                  作者：ZhangRunze760"));
                                            context.getSource().sendMessage(Text.literal("项目地址：http://github.com/ZhangRunze760/BlockboatFabric"));
                                            context.getSource().sendMessage(Text.literal("             Apache License, Version 2.0"));
                                            context.getSource().sendMessage(Text.literal("                 All Rights Reserved."));
                                            context.getSource().sendMessage(Text.literal("==================================================="));
                                            return 0;
                                        }))
                                //credits子命令注册结束

                                //connect子命令注册开始
                                .then(literal("connect")
                                .executes(context -> {
                                    context.getSource().sendMessage(Text.literal("连接你妈"));
                                    return 1;
                                })
                                .requires(source -> source.hasPermissionLevel(4))
                                .then(argument("host", StringArgumentType.string())
                                        .executes(context -> {
                                            context.getSource().sendMessage(Text.literal("§4不完整的命令"));
                                            return 0;
                                        })
                                        .then(argument("port", IntegerArgumentType.integer(0, 65536))
                                                .executes(context -> {
                                                    host = StringArgumentType.getString(context, "host");
                                                    int port = IntegerArgumentType.getInteger(context, "port");
                                                    context.getSource().sendMessage(
                                                            Text.literal(String.format("啥？你是说你要连接到%s:%d？",host, port))
                                                    );
                                                    context.getSource().sendMessage(Text.literal("连接你妈"));
                                                    return 0;
                                                })
                                        )
                        ))
                        //connect子命令注册结束
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
