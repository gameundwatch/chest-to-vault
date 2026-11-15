package dev.chest2vault.gametest;

import dev.chest2vault.Chest2Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** 本番の経路（チャンクの読み込み → 待ち行列 → ティック → 変換）を通す。イベントはテストのコードが発行する。 */
@GameTestHolder(Chest2Vault.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ChunkLoadConversionTests {
    private static final BlockPos POS = new BlockPos(1, 1, 1);
    /** 待ち行列はサーバーのティックの終わりで処理される。数ティック待てば足りる。 */
    private static final int WAIT_TICKS = 5;

    private ChunkLoadConversionTests() {}

    private static void placeChest(
            GameTestHelper helper, BlockPos pos, BlockState state, ResourceKey<LootTable> lootTable) {
        helper.setBlock(pos, state);
        if (lootTable != null) {
            ChestBlockEntity chest = helper.getBlockEntity(pos);
            chest.setLootTable(lootTable);
        }
    }

    private static void loadChunk(GameTestHelper helper, BlockPos pos) {
        NeoForge.EVENT_BUS.post(new ChunkEvent.Load(helper.getLevel().getChunkAt(helper.absolutePos(pos)), false));
    }

    private static void assertVault(GameTestHelper helper, BlockPos pos, ResourceKey<LootTable> lootTable) {
        helper.assertBlockPresent(Blocks.VAULT, pos);
        VaultBlockEntity vault = helper.getBlockEntity(pos);
        helper.assertValueEqual(vault.getConfig().lootTable(), lootTable, "loot table");
    }

    @GameTest(template = "empty", batch = "loot_chest")
    public static void convertsLootChest(GameTestHelper helper) {
        placeChest(
                helper,
                POS,
                Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST),
                BuiltInLootTables.SIMPLE_DUNGEON);
        loadChunk(helper, POS);
        helper.runAfterDelay(WAIT_TICKS, () -> {
            assertVault(helper, POS, BuiltInLootTables.SIMPLE_DUNGEON);
            helper.assertBlockProperty(POS, VaultBlock.FACING, Direction.EAST);
            // 組み込みの規則 chest2vault:default の値
            VaultConfig config = helper.<VaultBlockEntity>getBlockEntity(POS).getConfig();
            helper.assertTrue(config.keyItem().is(Items.TRIAL_KEY), "key_item should be minecraft:trial_key");
            helper.assertValueEqual(config.activationRange(), 4.0, "activation_range");
            helper.assertValueEqual(config.deactivationRange(), 4.5, "deactivation_range");
            helper.assertEntityNotPresent(EntityType.ITEM);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = "trapped_chest")
    public static void convertsTrappedChest(GameTestHelper helper) {
        placeChest(helper, POS, Blocks.TRAPPED_CHEST.defaultBlockState(), BuiltInLootTables.SIMPLE_DUNGEON);
        loadChunk(helper, POS);
        helper.runAfterDelay(WAIT_TICKS, () -> {
            assertVault(helper, POS, BuiltInLootTables.SIMPLE_DUNGEON);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = "double_chest")
    public static void convertsBothHalvesOfDoubleChest(GameTestHelper helper) {
        BlockPos right = POS.east();
        BlockState chest = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
        placeChest(helper, POS, chest.setValue(ChestBlock.TYPE, ChestType.LEFT), BuiltInLootTables.SIMPLE_DUNGEON);
        placeChest(helper, right, chest.setValue(ChestBlock.TYPE, ChestType.RIGHT), BuiltInLootTables.SIMPLE_DUNGEON);
        // 2つの半分は別のチャンクにまたがることがある（テストの置き場所は毎回変わる）。それぞれのチャンクを読み込む
        loadChunk(helper, POS);
        loadChunk(helper, right);
        helper.runAfterDelay(WAIT_TICKS, () -> {
            assertVault(helper, POS, BuiltInLootTables.SIMPLE_DUNGEON);
            assertVault(helper, right, BuiltInLootTables.SIMPLE_DUNGEON);
            helper.assertEntityNotPresent(EntityType.ITEM);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = "opened_chest")
    public static void keepsOpenedChest(GameTestHelper helper) {
        placeChest(helper, POS, Blocks.CHEST.defaultBlockState(), null);
        loadChunk(helper, POS);
        helper.runAfterDelay(WAIT_TICKS, () -> {
            helper.assertBlockPresent(Blocks.CHEST, POS);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = "blacklisted_chest")
    public static void keepsBlacklistedChestWithItsLootTable(GameTestHelper helper) {
        placeChest(helper, POS, Blocks.CHEST.defaultBlockState(), BuiltInLootTables.SPAWN_BONUS_CHEST);
        loadChunk(helper, POS);
        helper.runAfterDelay(WAIT_TICKS, () -> {
            helper.assertBlockPresent(Blocks.CHEST, POS);
            ChestBlockEntity chest = helper.getBlockEntity(POS);
            helper.assertValueEqual(chest.getLootTable(), BuiltInLootTables.SPAWN_BONUS_CHEST, "loot table");
            helper.succeed();
        });
    }
}
