package dev.chest2vault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

/** 起動時の検証。ブラックリストに当てはまらないすべてのルートテーブルで、規則の値がそろうかを確かめる（plan §4.5）。 */
final class Validation {
    private Validation() {}

    static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        List<String> problems = problems(
                server.reloadableRegistries().getKeys(Registries.LOOT_TABLE),
                Config.blacklist(),
                ConversionRule.rules(server.registryAccess()));
        if (!problems.isEmpty()) {
            throw new IllegalStateException(
                    "[chest2vault] conversion rules are incomplete:\n  " + String.join("\n  ", problems));
        }
    }

    /** @return 値がそろわないルートテーブルごとの誤り。空ならそろっている */
    static List<String> problems(
            Collection<ResourceLocation> lootTables,
            List<? extends String> blacklist,
            Map<ResourceLocation, ConversionRule> rules) {
        List<String> problems = new ArrayList<>();
        lootTables.stream()
                .sorted()
                .filter(id -> !Target.isBlacklisted(id, blacklist))
                .forEach(id -> ConversionRule.resolve(rules, ResourceKey.create(Registries.LOOT_TABLE, id))
                        .ifError(e -> problems.add(e.message())));
        return problems;
    }
}
