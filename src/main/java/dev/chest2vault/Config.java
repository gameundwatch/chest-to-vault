package dev.chest2vault;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    /** plan.md §3.4 の「対象外」。 */
    private static final List<String> DEFAULT_BLACKLIST = List.of(
            "minecraft:chests/village/village_armorer",
            "minecraft:chests/village/village_butcher",
            "minecraft:chests/village/village_cartographer",
            "minecraft:chests/village/village_desert_house",
            "minecraft:chests/village/village_fisher",
            "minecraft:chests/village/village_fletcher",
            "minecraft:chests/village/village_mason",
            "minecraft:chests/village/village_plains_house",
            "minecraft:chests/village/village_savanna_house",
            "minecraft:chests/village/village_shepherd",
            "minecraft:chests/village/village_snowy_house",
            "minecraft:chests/village/village_taiga_house",
            "minecraft:chests/village/village_tannery",
            "minecraft:chests/village/village_temple",
            "minecraft:chests/village/village_toolsmith",
            "minecraft:chests/village/village_weaponsmith",
            "minecraft:chests/igloo_chest",
            "minecraft:chests/spawn_bonus_chest",
            "minecraft:chests/shipwreck_map",
            "minecraft:chests/shipwreck_supply",
            "minecraft:chests/shipwreck_treasure",
            "minecraft:chests/buried_treasure");

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST = BUILDER.comment(
                    "Loot table IDs that are NOT converted to Vaults (glob: * matches any characters including /, ? matches one character).",
                    "Example: [\"minecraft:chests/village/*\", \"othermod:*\"]")
            .defineListAllowEmpty("blacklist", DEFAULT_BLACKLIST, () -> "", o -> o instanceof String);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {}

    /** ルートテーブルの ID の glob（plan §4.4）。 */
    static List<? extends String> blacklist() {
        return BLACKLIST.get();
    }
}
