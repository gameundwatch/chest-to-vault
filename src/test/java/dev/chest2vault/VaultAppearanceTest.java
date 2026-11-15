package dev.chest2vault;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class VaultAppearanceTest {
    @Test
    void vanillaVaultHasAppearanceDefaultingToVault() {
        assertEquals(VaultAppearance.VAULT, Blocks.VAULT.defaultBlockState().getValue(VaultAppearance.PROPERTY));
    }

    @Test
    void vaultCanTakeBarrelAppearance() {
        assertEquals(
                VaultAppearance.BARREL,
                Blocks.VAULT
                        .defaultBlockState()
                        .setValue(VaultAppearance.PROPERTY, VaultAppearance.BARREL)
                        .getValue(VaultAppearance.PROPERTY));
    }
}
