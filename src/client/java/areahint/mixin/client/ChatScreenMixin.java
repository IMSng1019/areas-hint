package areahint.mixin.client;

import areahint.chat.ClientChatCompat;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Inject(method = "sendMessage(Ljava/lang/String;Z)V", at = @At("HEAD"))
    private void onSendMessage(String chatText, boolean addToHistory, CallbackInfo ci) {
        ClientChatCompat.dispatch(chatText);
    }
}
