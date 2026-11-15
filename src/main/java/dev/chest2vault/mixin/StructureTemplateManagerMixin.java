package dev.chest2vault.mixin;

import dev.chest2vault.TemplateConversion;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** テンプレートを読み込んだ直後（覚えられる前）に、経路 i の書き換えを行う。各テンプレートにつき1回だけ呼ばれる。 */
@Mixin(StructureTemplateManager.class)
public abstract class StructureTemplateManagerMixin {
    @Inject(method = "tryLoad", at = @At("RETURN"))
    private void chest2vault$rewriteLoadedTemplate(
            ResourceLocation id, CallbackInfoReturnable<Optional<StructureTemplate>> cir) {
        cir.getReturnValue()
                .ifPresent(template -> TemplateConversion.onTemplateLoaded(
                        id, ((StructureTemplateAccessor) template).chest2vault$palettes()));
    }
}
