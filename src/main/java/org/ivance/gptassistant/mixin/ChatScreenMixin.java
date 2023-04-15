package org.ivance.gptassistant.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.ivance.gptassistant.ChatGptAssistantMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(at = @At("HEAD"), method = "sendMessage")
    private void sendMessage(String chatText, boolean addToHistory, CallbackInfoReturnable<Boolean> cir) {
        ChatGptAssistantMod.handleChatMessage(chatText, MinecraftClient.getInstance().player);
    }

}
