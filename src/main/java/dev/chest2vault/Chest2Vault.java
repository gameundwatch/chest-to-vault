package dev.chest2vault;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

/** Mod の入り口。登録だけを持つ（plan §4.7）。 */
@Mod(Chest2Vault.MOD_ID)
public final class Chest2Vault {
    public static final String MOD_ID = "chest2vault";

    public Chest2Vault(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modBus.addListener((DataPackRegistryEvent.NewRegistry e) ->
                e.dataPackRegistry(ConversionRule.REGISTRY_KEY, ConversionRule.CODEC));

        ConversionQueue queue = new ConversionQueue();
        NeoForge.EVENT_BUS.addListener(queue::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(queue::onServerTick);
        NeoForge.EVENT_BUS.addListener(Validation::onServerStarting);
        NeoForge.EVENT_BUS.addListener(OpenConversion::onRightClickBlock);
    }
}
