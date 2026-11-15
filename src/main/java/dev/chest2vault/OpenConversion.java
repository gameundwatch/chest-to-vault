package dev.chest2vault;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 開封時の変換（経路 iii）。プレイヤーが右クリックしたチェストが対象なら Vault に変え、その右クリックは止める（plan §4.2）。
 * 何が起きたかを伝えるため、効果音を鳴らし、右クリックしたプレイヤーのアクションバーに必要な鍵を出す。
 */
final class OpenConversion {
    private OpenConversion() {}

    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!Conversion.convert(level, event.getPos())) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        // クライアントに Mod がなくても鳴るよう、バニラの音を使う
        level.playSound(null, event.getPos(), SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0f, 1.0f);
        if (level.getBlockEntity(event.getPos()) instanceof VaultBlockEntity vault) {
            event.getEntity().displayClientMessage(message(vault.getConfig().keyItem()), true);
        }
    }

    /** クライアントに Mod がなければ、翻訳の代わりに英語の文を出す。鍵の名前はバニラの翻訳で出る。 */
    static Component message(ItemStack key) {
        return Component.translatableWithFallback(
                "chest2vault.message.converted", "This chest turned into a Vault. Key: %s", key.getHoverName());
    }
}
