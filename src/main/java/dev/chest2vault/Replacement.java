package dev.chest2vault;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.state.BlockState;

/** 置き換え（どう）。チェストか樽1つを、同じルートテーブルを持つ Vault にする（plan §4.2・§4.8）。 */
final class Replacement {
    private Replacement() {}

    /**
     * @param config 規則の照合で決めた値（plan §4.5）
     * @return Vault に置き換えたか
     */
    static boolean replace(
            ServerLevel level,
            BlockPos pos,
            BlockState containerState,
            RandomizableContainerBlockEntity container,
            VaultConfig config) {
        // ルートテーブルを持ったまま壊すと、onRemove → Containers.dropContents → getItem()
        // の流れで中身が抽選され、地面に落ちる。先にルートテーブルを外しておく。
        container.setLootTable(null);

        level.setBlock(pos, vaultState(containerState), Block.UPDATE_ALL);

        if (!(level.getBlockEntity(pos) instanceof VaultBlockEntity vault)) {
            return false;
        }
        vault.setConfig(config);
        vault.setChanged();
        return true;
    }

    /**
     * 入れ物の状態から決まる Vault の状態。チェストは同じ向きの普通の Vault。
     * 樽は見た目を barrel にし、横向きは同じ向き、上・下向きは北向きにする（Vault は水平の4方向だけ）。
     */
    static BlockState vaultState(BlockState containerState) {
        BlockState vault = Blocks.VAULT.defaultBlockState();
        if (containerState.is(Blocks.BARREL)) {
            Direction facing = containerState.getValue(BarrelBlock.FACING);
            return vault.setValue(VaultBlock.FACING, facing.getAxis().isHorizontal() ? facing : Direction.NORTH)
                    .setValue(VaultAppearance.PROPERTY, VaultAppearance.BARREL);
        }
        return vault.setValue(VaultBlock.FACING, containerState.getValue(ChestBlock.FACING));
    }
}
