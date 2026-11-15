package dev.chest2vault;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.junit.jupiter.api.Test;

class TargetTest {
    private static final List<String> NO_BLACKLIST = List.of();

    @Test
    void chestWithLootTableIsTarget() {
        assertTrue(Target.isTarget(Blocks.CHEST.defaultBlockState(), BuiltInLootTables.SIMPLE_DUNGEON, NO_BLACKLIST));
    }

    @Test
    void trappedChestWithLootTableIsTarget() {
        assertTrue(Target.isTarget(
                Blocks.TRAPPED_CHEST.defaultBlockState(), BuiltInLootTables.SIMPLE_DUNGEON, NO_BLACKLIST));
    }

    @Test
    void barrelWithLootTableIsTarget() {
        assertTrue(Target.isTarget(Blocks.BARREL.defaultBlockState(), BuiltInLootTables.SIMPLE_DUNGEON, NO_BLACKLIST));
    }

    @Test
    void otherContainerIsNotTarget() {
        assertFalse(
                Target.isTarget(Blocks.DISPENSER.defaultBlockState(), BuiltInLootTables.SIMPLE_DUNGEON, NO_BLACKLIST));
    }

    @Test
    void openedChestIsNotTarget() {
        assertFalse(Target.isTarget(Blocks.CHEST.defaultBlockState(), null, NO_BLACKLIST));
    }

    @Test
    void blacklistedLootTableIsNotTarget() {
        assertFalse(Target.isTarget(
                Blocks.CHEST.defaultBlockState(),
                BuiltInLootTables.SIMPLE_DUNGEON,
                List.of("minecraft:chests/simple_*")));
    }

    @Test
    void defaultBlacklistExcludesTrialChamberChestsAndBarrels() {
        assertFalse(Target.isTarget(
                Blocks.CHEST.defaultBlockState(),
                ResourceKey.create(
                        Registries.LOOT_TABLE, ResourceLocation.parse("minecraft:chests/trial_chambers/entrance")),
                Config.DEFAULT_BLACKLIST));
        assertFalse(Target.isTarget(
                Blocks.BARREL.defaultBlockState(),
                ResourceKey.create(
                        Registries.LOOT_TABLE,
                        ResourceLocation.parse("minecraft:chests/trial_chambers/intersection_barrel")),
                Config.DEFAULT_BLACKLIST));
    }

    @Test
    void defaultBlacklistExcludesBonusChest() {
        assertFalse(Target.isTarget(
                Blocks.CHEST.defaultBlockState(), BuiltInLootTables.SPAWN_BONUS_CHEST, Config.DEFAULT_BLACKLIST));
    }
}
