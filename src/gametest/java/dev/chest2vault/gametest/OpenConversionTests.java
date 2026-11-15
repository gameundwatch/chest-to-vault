package dev.chest2vault.gametest;

import dev.chest2vault.Chest2Vault;
import dev.chest2vault.VaultAppearance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** 経路 iii：プレイヤーの右クリックで変換する（plan §4.2）。チャンクの読み込みのイベントは発行しない。 */
@GameTestHolder(Chest2Vault.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OpenConversionTests {
    private static final BlockPos POS = new BlockPos(1, 1, 1);

    private OpenConversionTests() {}

    private static void placeChest(GameTestHelper helper, ResourceKey<LootTable> lootTable) {
        place(helper, Blocks.CHEST.defaultBlockState(), lootTable);
    }

    private static void place(GameTestHelper helper, BlockState state, ResourceKey<LootTable> lootTable) {
        helper.setBlock(POS, state);
        if (lootTable != null) {
            RandomizableContainerBlockEntity container = helper.getBlockEntity(POS);
            container.setLootTable(lootTable);
        }
    }

    /** 実際のプレイヤーと同じ経路（ServerPlayerGameMode.useItemOn）で右クリックする。RightClickBlock が発行される。 */
    // makeMockServerPlayerInLevel はバニラで削除予定とされているが、1.21.1 には代わりがないので使う
    @SuppressWarnings("removal")
    private static ServerPlayer rightClick(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos abs = helper.absolutePos(POS);
        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                player.getItemInHand(InteractionHand.MAIN_HAND),
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
        return player;
    }

    @GameTest(template = "empty", batch = "open_loot_chest")
    public static void convertsLootChestAndStopsOpening(GameTestHelper helper) {
        placeChest(helper, BuiltInLootTables.SIMPLE_DUNGEON);
        ServerPlayer player = rightClick(helper);

        helper.assertBlockPresent(Blocks.VAULT, POS);
        VaultBlockEntity vault = helper.getBlockEntity(POS);
        helper.assertValueEqual(vault.getConfig().lootTable(), BuiltInLootTables.SIMPLE_DUNGEON, "loot table");
        helper.assertTrue(player.containerMenu == player.inventoryMenu, "the right click should not open a menu");
        helper.assertEntityNotPresent(EntityType.ITEM);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "open_loot_barrel")
    public static void convertsLootBarrelAndStopsOpening(GameTestHelper helper) {
        place(helper, Blocks.BARREL.defaultBlockState(), BuiltInLootTables.SIMPLE_DUNGEON);
        ServerPlayer player = rightClick(helper);

        helper.assertBlockPresent(Blocks.VAULT, POS);
        helper.assertBlockProperty(POS, VaultAppearance.PROPERTY, VaultAppearance.BARREL);
        helper.assertTrue(player.containerMenu == player.inventoryMenu, "the right click should not open a menu");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "open_opened_chest")
    public static void opensOpenedChestNormally(GameTestHelper helper) {
        placeChest(helper, null);
        ServerPlayer player = rightClick(helper);

        helper.assertBlockPresent(Blocks.CHEST, POS);
        helper.assertTrue(player.containerMenu != player.inventoryMenu, "the chest should open as usual");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "open_blacklisted_chest")
    public static void opensBlacklistedChestNormally(GameTestHelper helper) {
        placeChest(helper, BuiltInLootTables.SPAWN_BONUS_CHEST);
        ServerPlayer player = rightClick(helper);

        helper.assertBlockPresent(Blocks.CHEST, POS);
        helper.assertTrue(player.containerMenu != player.inventoryMenu, "the chest should open as usual");
        helper.succeed();
    }
}
