package dev.chest2vault;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.storage.loot.LootTable;

/** 規則（何に：to）。データパックから読み、当てはまるルートテーブルの Vault の値を与える（plan §4.5）。 */
public record ConversionRule(
        int priority,
        List<String> patterns,
        Optional<ItemStack> keyItem,
        Optional<Double> activationRange,
        Optional<Double> deactivationRange,
        Optional<ResourceKey<LootTable>> overrideLootTableToDisplay) {
    public static final ResourceKey<Registry<ConversionRule>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Chest2Vault.MOD_ID, "conversion_rule"));

    public static final Codec<ConversionRule> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.INT.fieldOf("priority").forGetter(ConversionRule::priority),
                    Codec.STRING.listOf().fieldOf("patterns").forGetter(ConversionRule::patterns),
                    ItemStack.CODEC.optionalFieldOf("key_item").forGetter(ConversionRule::keyItem),
                    Codec.DOUBLE.optionalFieldOf("activation_range").forGetter(ConversionRule::activationRange),
                    Codec.DOUBLE.optionalFieldOf("deactivation_range").forGetter(ConversionRule::deactivationRange),
                    ResourceKey.codec(Registries.LOOT_TABLE)
                            .optionalFieldOf("override_loot_table_to_display")
                            .forGetter(ConversionRule::overrideLootTableToDisplay))
            .apply(i, ConversionRule::new));

    boolean matches(String lootTableId) {
        return patterns.stream().anyMatch(glob -> Glob.matches(glob, lootTableId));
    }

    /**
     * 規則の照合。当てはまる規則を priority の小さい順（同じなら ID の順）に適用し、書かれている項目だけを上書きする。
     *
     * @throws IllegalStateException 値がそろわないとき（起動時の検証は plan §8 の3）
     */
    static VaultConfig resolve(Map<ResourceLocation, ConversionRule> rules, ResourceKey<LootTable> lootTable) {
        String id = lootTable.location().toString();
        List<Map.Entry<ResourceLocation, ConversionRule>> matched = new ArrayList<>();
        for (Map.Entry<ResourceLocation, ConversionRule> entry : rules.entrySet()) {
            if (entry.getValue().matches(id)) {
                matched.add(entry);
            }
        }
        matched.sort(Comparator.comparingInt((Map.Entry<ResourceLocation, ConversionRule> e) -> e.getValue().priority)
                .thenComparing(Map.Entry::getKey));

        Optional<ItemStack> keyItem = Optional.empty();
        Optional<Double> activationRange = Optional.empty();
        Optional<Double> deactivationRange = Optional.empty();
        Optional<ResourceKey<LootTable>> display = Optional.empty();
        for (Map.Entry<ResourceLocation, ConversionRule> entry : matched) {
            ConversionRule rule = entry.getValue();
            keyItem = rule.keyItem.isPresent() ? rule.keyItem : keyItem;
            activationRange = rule.activationRange.isPresent() ? rule.activationRange : activationRange;
            deactivationRange = rule.deactivationRange.isPresent() ? rule.deactivationRange : deactivationRange;
            display = rule.overrideLootTableToDisplay.isPresent() ? rule.overrideLootTableToDisplay : display;
        }

        List<String> missing = new ArrayList<>();
        if (keyItem.isEmpty()) missing.add("key_item");
        if (activationRange.isEmpty()) missing.add("activation_range");
        if (deactivationRange.isEmpty()) missing.add("deactivation_range");
        if (!missing.isEmpty()) {
            throw new IllegalStateException("[chest2vault] no rule gives " + missing + " for " + id);
        }
        return new VaultConfig(
                lootTable,
                activationRange.get(),
                deactivationRange.get(),
                keyItem.get().copy(),
                display);
    }
}
