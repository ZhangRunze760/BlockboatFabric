package fun.jsserver.blockboat.mixin;

import com.mojang.brigadier.ParseResults;
import fun.jsserver.blockboat.BlockboatFabric;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.server.command.CommandManager.class)
public abstract class CommandManagerMixin {
    @Inject(method = "execute", at = @At("HEAD"))
    private void onExecute(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfo ci) {
        if (BlockboatFabric.config.ListenCommand) {
            String Message = String.format("%s 玩家输入了命令：/%s", parseResults.getContext().getSource().getName(), command);
            if ((command.startsWith("give") || command.startsWith("gamemode") || command.startsWith("gamerule")
                    || command.startsWith("carpet") || command.startsWith("op") || command.startsWith("deop") || command.startsWith("ban")
                    || command.startsWith("pardon") || command.startsWith("kill") || command.startsWith("tp")
                    || command.startsWith("summon") || command.startsWith("setblock") || command.startsWith("clone") || command.startsWith("fill")
                    || command.startsWith("/") || command.startsWith("tick") || command.startsWith("effect") || command.startsWith("stop"))
            && !parseResults.getContext().getSource().getName().equals("Server")) {
                BlockboatFabric.LOGGER.warn(String.format("%s 玩家输入高危命令！请引起重视。", parseResults.getContext().getSource().getName()));
                BlockboatFabric.LOGGER.warn(String.format("命令内容：%s", command));
                BlockboatFabric.sendMessage.sendMessageToGroup(Message + "\n高危险性！");
            } else BlockboatFabric.LOGGER.info(Message);
        }
    }
}
