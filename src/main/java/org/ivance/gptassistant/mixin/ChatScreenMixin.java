package org.ivance.gptassistant.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.ivance.gptassistant.ChatGptAssistantMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ChatScreenMixin {

    @Inject(at = @At("HEAD"), method = "sendMessage(Ljava/lang/String;Z)V")
    private void sendMessage(String message, boolean toHud, CallbackInfo ci) {
        ChatGptAssistantMod.handleChatMessage(message, MinecraftClient.getInstance().player);
    }

}
