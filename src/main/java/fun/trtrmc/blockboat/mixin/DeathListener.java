package fun.trtrmc.blockboat.mixin;

import fun.trtrmc.blockboat.BlockboatFabric;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerPlayerEntity.class)
public abstract class DeathListener {
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(CallbackInfo ci) {
        String name = Objects.requireNonNull(((PlayerEntity) (Object) this).getDisplayName()).getString();
        String deathMessage = ((PlayerEntity)(Object)this).getDamageTracker().getDeathMessage().getString();
        Vec3d pos = ((PlayerEntity)(Object)this).getPos();
        String Message = String.format("玩家 %s 寄啦！原因：%s\n舔包位置：%.2f, %.2f, %.2f", name, deathMessage,
                pos.getX(), pos.getY(), pos.getZ());
        BlockboatFabric.sendMessage.sendMessageToGroup(Message);
    }
}
