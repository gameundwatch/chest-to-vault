package dev.chest2vault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ValidationTest {
    private static final ResourceLocation DUNGEON = ResourceLocation.parse("minecraft:chests/simple_dungeon");
    private static final ResourceLocation BONUS = ResourceLocation.parse("minecraft:chests/spawn_bonus_chest");

    private static ConversionRule rule(String json) {
        return ConversionRule.CODEC
                .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                .getOrThrow();
    }

    private static final Map<ResourceLocation, ConversionRule> COMPLETE =
            Map.of(ResourceLocation.parse("test:default"), rule("""
                    {"priority": 0, "patterns": ["*"], "key_item": {"id": "minecraft:trial_key"},
                     "activation_range": 4.0, "deactivation_range": 4.5}"""));

    private static final Map<ResourceLocation, ConversionRule> PARTIAL = Map.of(
            ResourceLocation.parse("test:partial"),
            rule("{\"priority\": 0, \"patterns\": [\"*\"], \"activation_range\": 4.0}"));

    @Test
    void passesWhenEveryLootTableHasValues() {
        assertTrue(Validation.problems(List.of(DUNGEON, BONUS), List.of(), COMPLETE)
                .isEmpty());
    }

    @Test
    void reportsEveryLootTableWithMissingValues() {
        List<String> problems = Validation.problems(List.of(DUNGEON, BONUS), List.of(), PARTIAL);
        assertEquals(2, problems.size());
        assertTrue(problems.get(0).contains("key_item"));
        assertTrue(problems.get(0).contains(DUNGEON.toString())); // ID の順
    }

    @Test
    void skipsBlacklistedLootTables() {
        assertTrue(Validation.problems(List.of(BONUS), List.of("minecraft:chests/spawn_*"), PARTIAL)
                .isEmpty());
    }
}
