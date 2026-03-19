package areahint.mixin.client;

import areahint.chat.ClientChatCompat;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow
    protected TextFieldWidget chatField;

    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"))
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (keyCode != 257 && keyCode != 335) {
            return;
        }

        String chatText = this.chatField.getText().trim();
        if (!chatText.isEmpty()) {
            ClientChatCompat.dispatch(chatText);
        }
    }
}
