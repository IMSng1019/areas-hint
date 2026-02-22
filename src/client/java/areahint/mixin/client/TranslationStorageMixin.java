package areahint.mixin.client;

import areahint.i18n.I18nManager;
import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TranslationStorage.class)
public class TranslationStorageMixin {

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void areasHintOverrideTranslation(String key, String fallback, CallbackInfoReturnable<String> cir) {
        if (I18nManager.hasKey(key)) {
            cir.setReturnValue(I18nManager.translate(key));
        }
    }
}
