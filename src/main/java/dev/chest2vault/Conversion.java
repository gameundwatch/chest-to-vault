package dev.chest2vault;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

/** 変換。何を（Target）→ 何に（ConversionRule）→ どう（Replacement）を1か所で行う。どの経路もこれを呼ぶ（plan §4.2）。 */
final class Conversion {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Conversion() {}

    /** @return Vault に置き換えたか */
    static boolean convert(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity chest)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (!Target.isTarget(state, chest.getLootTable(), Config.blacklist())) {
            return false;
        }
        // 値がそろうことは、起動時の検証（Validation）で確かめてある
        VaultConfig config = ConversionRule.resolve(ConversionRule.rules(level.registryAccess()), chest.getLootTable())
                .getOrThrow();
        if (!Replacement.replace(level, pos, state, chest, config)) {
            return false;
        }
        LOGGER.info(
                "[chest2vault] converted container at {} in {}",
                pos,
                level.dimension().location());
        return true;
    }
}
