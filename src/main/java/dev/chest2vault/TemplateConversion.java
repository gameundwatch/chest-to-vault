package dev.chest2vault;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

/**
 * テンプレートの書き換え（経路 i）。テンプレートの中の、ルートテーブルが書いてあるチェストを Vault に書き換える（plan §4.2）。
 * 何を（Target）・何に（ConversionRule）はほかの経路と共通で、どうだけをワールドではなくテンプレートに対して行う。
 */
public final class TemplateConversion {
    private static final Logger LOGGER = LogUtils.getLogger();

    private TemplateConversion() {}

    /** Mixin から呼ばれる。サーバーが動いていなければ何もしない（経路 ii・iii に任せる）。 */
    public static void onTemplateLoaded(ResourceLocation id, List<StructureTemplate.Palette> palettes) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        Map<ResourceLocation, ConversionRule> rules = ConversionRule.rules(server.registryAccess());
        int rewritten = 0;
        for (StructureTemplate.Palette palette : palettes) {
            rewritten += rewrite(palette.blocks(), rules, Config.blacklist(), server.registryAccess());
        }
        if (rewritten > 0) {
            LOGGER.info("[chest2vault] rewrote {} chests in template {}", rewritten, id);
        }
    }

    /** @return 書き換えたチェストの数 */
    static int rewrite(
            List<StructureTemplate.StructureBlockInfo> blocks,
            Map<ResourceLocation, ConversionRule> rules,
            List<? extends String> blacklist,
            HolderLookup.Provider registries) {
        int rewritten = 0;
        for (int i = 0; i < blocks.size(); i++) {
            StructureTemplate.StructureBlockInfo info = blocks.get(i);
            ResourceKey<LootTable> lootTable = lootTable(info.nbt());
            if (!Target.isTarget(info.state(), lootTable, blacklist)) {
                continue;
            }
            // 値がそろうことは、起動時の検証（Validation）で確かめてある
            VaultConfig config = ConversionRule.resolve(rules, lootTable).getOrThrow();
            BlockState vaultState = Blocks.VAULT
                    .defaultBlockState()
                    .setValue(VaultBlock.FACING, info.state().getValue(ChestBlock.FACING));
            blocks.set(
                    i,
                    new StructureTemplate.StructureBlockInfo(
                            info.pos(), vaultState, vaultNbt(vaultState, config, registries)));
            rewritten++;
        }
        return rewritten;
    }

    /** テンプレートのチェストに書いてあるルートテーブル。なければ（開封済み・飾り）null */
    private static ResourceKey<LootTable> lootTable(CompoundTag nbt) {
        if (nbt == null || !nbt.contains("LootTable", Tag.TAG_STRING)) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(nbt.getString("LootTable"));
        return id == null ? null : ResourceKey.create(Registries.LOOT_TABLE, id);
    }

    /** Vault のブロックエンティティが読み込める形の NBT。置いたときに、この設定の Vault になる。 */
    private static CompoundTag vaultNbt(BlockState vaultState, VaultConfig config, HolderLookup.Provider registries) {
        VaultBlockEntity vault = new VaultBlockEntity(BlockPos.ZERO, vaultState);
        vault.setConfig(config);
        return vault.saveWithId(registries);
    }
}
