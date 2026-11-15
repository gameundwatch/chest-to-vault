package dev.chest2vault;

import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

/** 変換対象（何を：from）。ワールドに触らずに判定する（plan §4.2）。 */
final class Target {
    private Target() {}

    /**
     * @param lootTable チェストのルートテーブル。開封済みなら null
     * @param blacklist ルートテーブルの ID の glob（plan §4.4）
     */
    static boolean isTarget(
            BlockState state, @Nullable ResourceKey<LootTable> lootTable, List<? extends String> blacklist) {
        if (!state.is(Blocks.CHEST) && !state.is(Blocks.TRAPPED_CHEST)) {
            return false;
        }
        if (lootTable == null) {
            return false; // 開封済み
        }
        String id = lootTable.location().toString();
        return blacklist.stream().noneMatch(glob -> Glob.matches(glob, id));
    }
}
