package dev.chest2vault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.junit.jupiter.api.Test;

class ConversionRuleTest {
    private static DataResult<ConversionRule> parse(String json) {
        return ConversionRule.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
    }

    private static ConversionRule rule(String json) {
        return parse(json).getOrThrow();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("test", path);
    }

    private static final ConversionRule DEFAULT = rule("""
            {"priority": 0, "patterns": ["*"], "key_item": {"id": "minecraft:trial_key", "count": 1},
             "activation_range": 4.0, "deactivation_range": 4.5}""");

    // 読み込み

    @Test
    void readsAllFields() {
        ConversionRule r = rule("""
                {"priority": 5, "patterns": ["a:*"], "key_item": {"id": "minecraft:ominous_trial_key"},
                 "activation_range": 2.0, "deactivation_range": 3.0,
                 "override_loot_table_to_display": "minecraft:chests/simple_dungeon"}""");
        assertEquals(5, r.priority());
        assertEquals(List.of("a:*"), r.patterns());
        assertTrue(r.keyItem().orElseThrow().is(Items.OMINOUS_TRIAL_KEY));
        assertEquals(Optional.of(2.0), r.activationRange());
        assertEquals(Optional.of(3.0), r.deactivationRange());
        assertEquals(Optional.of(BuiltInLootTables.SIMPLE_DUNGEON), r.overrideLootTableToDisplay());
    }

    @Test
    void valueFieldsAreOptional() {
        ConversionRule r = rule("{\"priority\": 1, \"patterns\": [\"*\"]}");
        assertTrue(r.keyItem().isEmpty());
        assertTrue(r.activationRange().isEmpty());
    }

    @Test
    void rejectsMissingPriority() {
        assertTrue(parse("{\"patterns\": [\"*\"]}").isError());
    }

    @Test
    void rejectsMissingPatterns() {
        assertTrue(parse("{\"priority\": 0}").isError());
    }

    @Test
    void rejectsNonIntegerPriority() {
        assertTrue(parse("{\"priority\": \"high\", \"patterns\": [\"*\"]}").isError());
    }

    @Test
    void rejectsUnknownItem() {
        assertTrue(parse("{\"priority\": 0, \"patterns\": [\"*\"], \"key_item\": {\"id\": \"minecraft:nope\"}}")
                .isError());
    }

    // 照合

    @Test
    void defaultRuleGivesAllValues() {
        VaultConfig c = ConversionRule.resolve(Map.of(id("default"), DEFAULT), BuiltInLootTables.SIMPLE_DUNGEON);
        assertEquals(BuiltInLootTables.SIMPLE_DUNGEON, c.lootTable());
        assertTrue(c.keyItem().is(Items.TRIAL_KEY));
        assertEquals(4.0, c.activationRange());
        assertEquals(4.5, c.deactivationRange());
        assertTrue(c.overrideLootTableToDisplay().isEmpty());
    }

    @Test
    void rangesFallBackToVanillaDefaults() {
        ConversionRule keyOnly =
                rule("{\"priority\": 0, \"patterns\": [\"*\"], \"key_item\": {\"id\": \"minecraft:trial_key\"}}");
        VaultConfig c = ConversionRule.resolve(Map.of(id("key_only"), keyOnly), BuiltInLootTables.SIMPLE_DUNGEON);
        assertEquals(4.0, c.activationRange());
        assertEquals(4.5, c.deactivationRange());
    }

    @Test
    void higherPriorityOverridesOnlyWrittenFields() {
        ConversionRule dungeon = rule("""
                {"priority": 100, "patterns": ["minecraft:chests/simple_dungeon"],
                 "key_item": {"id": "minecraft:ominous_trial_key"}}""");
        VaultConfig c = ConversionRule.resolve(
                Map.of(id("default"), DEFAULT, id("dungeon"), dungeon), BuiltInLootTables.SIMPLE_DUNGEON);
        assertTrue(c.keyItem().is(Items.OMINOUS_TRIAL_KEY));
        assertEquals(4.0, c.activationRange());
    }

    @Test
    void ignoresRuleThatDoesNotMatch() {
        ConversionRule other = rule("""
                {"priority": 100, "patterns": ["othermod:*"], "key_item": {"id": "minecraft:ominous_trial_key"}}""");
        VaultConfig c = ConversionRule.resolve(
                Map.of(id("default"), DEFAULT, id("other"), other), BuiltInLootTables.SIMPLE_DUNGEON);
        assertTrue(c.keyItem().is(Items.TRIAL_KEY));
    }

    @Test
    void samePriorityAppliesInIdOrder() {
        ConversionRule a = rule("{\"priority\": 10, \"patterns\": [\"*\"], \"activation_range\": 1.0}");
        ConversionRule b = rule("{\"priority\": 10, \"patterns\": [\"*\"], \"activation_range\": 2.0}");
        VaultConfig c = ConversionRule.resolve(
                Map.of(id("default"), DEFAULT, id("b"), b, id("a"), a), BuiltInLootTables.SIMPLE_DUNGEON);
        assertEquals(2.0, c.activationRange()); // test:b は test:a のあと
    }

    @Test
    void throwsWhenValuesAreMissing() {
        ConversionRule partial = rule("{\"priority\": 0, \"patterns\": [\"*\"], \"activation_range\": 1.0}");
        assertThrows(
                IllegalStateException.class,
                () -> ConversionRule.resolve(Map.of(id("partial"), partial), BuiltInLootTables.SIMPLE_DUNGEON));
    }

    @Test
    void keyItemIsCopiedPerVault() {
        Map<ResourceLocation, ConversionRule> rules = Map.of(id("default"), DEFAULT);
        ItemStack first =
                ConversionRule.resolve(rules, BuiltInLootTables.SIMPLE_DUNGEON).keyItem();
        first.setCount(64);
        assertEquals(
                1,
                ConversionRule.resolve(rules, BuiltInLootTables.SIMPLE_DUNGEON)
                        .keyItem()
                        .getCount());
    }

    /** null は「書いていない」と同じ。上書きしないので、どの規則も書かなければチェストのルートテーブルを表示する。 */
    @Test
    void nullOverrideMeansNotWritten() {
        ConversionRule r = rule("{\"priority\": 0, \"patterns\": [\"*\"], \"override_loot_table_to_display\": null}");
        assertTrue(r.overrideLootTableToDisplay().isEmpty());
    }
}
