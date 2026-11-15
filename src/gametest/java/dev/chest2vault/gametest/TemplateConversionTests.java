package dev.chest2vault.gametest;

import dev.chest2vault.Chest2Vault;
import dev.chest2vault.VaultAppearance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 経路 i：テンプレートの書き換え（plan §4.2）。テストの舞台そのものがテンプレートから置かれるので、
 * チャンクの読み込みのイベントも右クリックも起こさずに、置いた直後の状態を判定する。
 */
@GameTestHolder(Chest2Vault.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TemplateConversionTests {
    // テンプレートの (1, 1, 1)。GameTest は構造物ブロックの1段上からテンプレートを置くので、テストから見ると y が1つ上になる
    private static final BlockPos POS = new BlockPos(1, 2, 1);

    private TemplateConversionTests() {}

    @GameTest(template = "template_loot_chest", batch = "template_loot_chest")
    public static void rewritesLootChestInTemplate(GameTestHelper helper) {
        helper.assertBlockPresent(Blocks.VAULT, POS);
        helper.assertBlockProperty(POS, VaultBlock.FACING, Direction.EAST);
        VaultConfig config = helper.<VaultBlockEntity>getBlockEntity(POS).getConfig();
        helper.assertValueEqual(config.lootTable(), BuiltInLootTables.SIMPLE_DUNGEON, "loot table");
        helper.assertTrue(config.keyItem().is(Items.TRIAL_KEY), "key_item should be minecraft:trial_key");
        helper.assertEntityNotPresent(EntityType.ITEM);
        helper.succeed();
    }

    /** 上向きの樽のテンプレート。北向き・見た目 barrel の Vault になる。 */
    @GameTest(template = "template_loot_barrel", batch = "template_loot_barrel")
    public static void rewritesUpwardBarrelInTemplate(GameTestHelper helper) {
        helper.assertBlockPresent(Blocks.VAULT, POS);
        helper.assertBlockProperty(POS, VaultBlock.FACING, Direction.NORTH);
        helper.assertBlockProperty(POS, VaultAppearance.PROPERTY, VaultAppearance.BARREL);
        VaultConfig config = helper.<VaultBlockEntity>getBlockEntity(POS).getConfig();
        helper.assertValueEqual(config.lootTable(), BuiltInLootTables.SIMPLE_DUNGEON, "loot table");
        helper.succeed();
    }

    @GameTest(template = "template_blacklisted_chest", batch = "template_blacklisted_chest")
    public static void keepsBlacklistedChestInTemplate(GameTestHelper helper) {
        helper.assertBlockPresent(Blocks.CHEST, POS);
        ChestBlockEntity chest = helper.getBlockEntity(POS);
        helper.assertValueEqual(chest.getLootTable(), BuiltInLootTables.SPAWN_BONUS_CHEST, "loot table");
        helper.succeed();
    }

    @GameTest(template = "template_opened_chest", batch = "template_opened_chest")
    public static void keepsChestWithoutLootTableInTemplate(GameTestHelper helper) {
        helper.assertBlockPresent(Blocks.CHEST, POS);
        helper.succeed();
    }
}
