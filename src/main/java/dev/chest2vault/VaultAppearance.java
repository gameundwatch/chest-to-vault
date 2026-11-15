package dev.chest2vault;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * 見た目（plan §4.8）。Mixin でバニラの Vault に足す状態 {@code appearance} の値。
 * 最初の値（VAULT）が初期値になるので、値が決まっていなければバニラと同じ見た目になる。
 */
public enum VaultAppearance implements StringRepresentable {
    VAULT("vault"),
    BARREL("barrel");

    public static final EnumProperty<VaultAppearance> PROPERTY =
            EnumProperty.create("appearance", VaultAppearance.class);

    private final String name;

    VaultAppearance(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
