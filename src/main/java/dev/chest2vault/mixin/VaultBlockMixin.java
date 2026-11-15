package dev.chest2vault.mixin;

import dev.chest2vault.VaultAppearance;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** バニラの Vault に、状態 appearance（見た目）を足す（plan §4.8）。 */
@Mixin(VaultBlock.class)
public abstract class VaultBlockMixin {
    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void chest2vault$addAppearance(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(VaultAppearance.PROPERTY);
    }
}
