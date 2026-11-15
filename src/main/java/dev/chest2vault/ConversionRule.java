package dev.chest2vault;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/** 確認用：データパックから読む規則。いまは priority だけを持つ。 */
public record ConversionRule(int priority) {
    public static final ResourceKey<Registry<ConversionRule>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Chest2Vault.MOD_ID, "conversion_rule"));

    public static final Codec<ConversionRule> CODEC =
            RecordCodecBuilder.create(i -> i.group(Codec.INT.fieldOf("priority").forGetter(ConversionRule::priority))
                    .apply(i, ConversionRule::new));
}
