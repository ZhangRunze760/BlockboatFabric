package top.jsminecraft.blockboat;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.BlockState;

public class BlockboatFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            BlockState state = world.getBlockState(pos);
            if (state.isToolRequired() && !player.isSpectator() && player.getMainHandStack().isEmpty()) {

            }
            return null;
        });
    }
}
