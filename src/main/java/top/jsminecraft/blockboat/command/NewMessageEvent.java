package top.jsminecraft.blockboat.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.command.ServerCommandSource;

public interface NewMessageEvent {
    Event<NewMessageEvent> INSTANCE = EventFactory.createArrayBacked(
            NewMessageEvent.class,
            (listeners) -> (message) -> {
                for (NewMessageEvent listener : listeners) {
                    listener.sendBroadcastMessage(message);
                }
            });

    void sendBroadcastMessage(String message);
}
