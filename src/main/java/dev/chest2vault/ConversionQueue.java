package dev.chest2vault;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

/** 待ち行列（いつ）。チャンクの読み込みで積み、サーバーのティックで処理する（plan §4.2）。 */
final class ConversionQueue {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_RETRIES = 100; // 約5秒。チャンクが FULL になるのを待つ上限

    /** ChunkEvent.Load ではブロックに触らず、位置だけ積む（NeoForge の注意書き：その場で触るとデッドロックする）。 */
    private record Pending(ServerLevel level, ChunkPos pos, int retries) {}

    private final Queue<Pending> queue = new ConcurrentLinkedQueue<>();

    void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            queue.add(new Pending(level, event.getChunk().getPos(), 0));
        }
    }

    void onServerTick(ServerTickEvent.Post event) {
        List<Pending> retry = new ArrayList<>();
        Pending p;
        while ((p = queue.poll()) != null) {
            LevelChunk chunk = p.level().getChunkSource().getChunkNow(p.pos().x, p.pos().z);
            if (chunk == null) {
                if (p.retries() < MAX_RETRIES) {
                    retry.add(new Pending(p.level(), p.pos(), p.retries() + 1));
                }
                continue;
            }
            // 置き換えでマップが変わるので、先に位置を写し取る
            List<BlockPos> positions = new ArrayList<>(chunk.getBlockEntities().keySet());
            for (BlockPos pos : positions) {
                process(p.level(), pos);
            }
        }
        queue.addAll(retry);
    }

    private static void process(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity chest)) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!Target.isTarget(state, chest.getLootTable(), Config.blacklist())) {
            return;
        }
        // 値がそろうことは、起動時の検証（Validation）で確かめてある
        VaultConfig config = ConversionRule.resolve(ConversionRule.rules(level.registryAccess()), chest.getLootTable())
                .getOrThrow();
        if (Replacement.replace(level, pos, state, chest, config)) {
            LOGGER.info(
                    "[chest2vault] converted chest at {} in {}",
                    pos,
                    level.dimension().location());
        }
    }
}
