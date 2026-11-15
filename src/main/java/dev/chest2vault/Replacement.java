package dev.chest2vault;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.state.BlockState;

/** 置き換え（どう）。チェスト1つを、同じルートテーブルを持つ Vault にする（plan §4.2）。 */
final class Replacement {
    private Replacement() {}

    /**
     * @param config 規則の照合で決めた値（plan §4.5）
     * @return Vault に置き換えたか
     */
    static boolean replace(
            ServerLevel level,
            BlockPos pos,
            BlockState chestState,
            RandomizableContainerBlockEntity chest,
            VaultConfig config) {
        // ルートテーブルを持ったまま壊すと、ChestBlock.onRemove → Containers.dropContents → getItem()
        // の流れで中身が抽選され、地面に落ちる。先にルートテーブルを外しておく。
        chest.setLootTable(null);

        BlockState vaultState =
                Blocks.VAULT.defaultBlockState().setValue(VaultBlock.FACING, chestState.getValue(ChestBlock.FACING));
        level.setBlock(pos, vaultState, Block.UPDATE_ALL);

        if (!(level.getBlockEntity(pos) instanceof VaultBlockEntity vault)) {
            return false;
        }
        vault.setConfig(config);
        vault.setChanged();
        return true;
    }
}
