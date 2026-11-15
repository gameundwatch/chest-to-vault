package dev.chest2vault;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

/** 置き換え（どう）。チェスト1つを、同じルートテーブルを持つ Vault にする（plan §4.2）。 */
final class Replacement {
    // 規則の照合（plan §8 の2）ができるまでの定数
    private static final ItemStack KEY = new ItemStack(Items.TRIAL_KEY);
    private static final double ACTIVATION_RANGE = 4.0;
    private static final double DEACTIVATION_RANGE = 4.5;

    private Replacement() {}

    /** @return Vault に置き換えたか */
    static boolean replace(
            ServerLevel level, BlockPos pos, BlockState chestState, RandomizableContainerBlockEntity chest) {
        ResourceKey<LootTable> lootTable = chest.getLootTable();

        // ルートテーブルを持ったまま壊すと、ChestBlock.onRemove → Containers.dropContents → getItem()
        // の流れで中身が抽選され、地面に落ちる。先にルートテーブルを外しておく。
        chest.setLootTable(null);

        BlockState vaultState =
                Blocks.VAULT.defaultBlockState().setValue(VaultBlock.FACING, chestState.getValue(ChestBlock.FACING));
        level.setBlock(pos, vaultState, Block.UPDATE_ALL);

        if (!(level.getBlockEntity(pos) instanceof VaultBlockEntity vault)) {
            return false;
        }
        vault.setConfig(new VaultConfig(lootTable, ACTIVATION_RANGE, DEACTIVATION_RANGE, KEY.copy(), Optional.empty()));
        vault.setChanged();
        return true;
    }
}
