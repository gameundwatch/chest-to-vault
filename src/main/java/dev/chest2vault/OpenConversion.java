package dev.chest2vault;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** 開封時の変換（経路 iii）。プレイヤーが右クリックしたチェストが対象なら Vault に変え、その右クリックは止める（plan §4.2）。 */
final class OpenConversion {
    private OpenConversion() {}

    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (Conversion.convert(level, event.getPos())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
