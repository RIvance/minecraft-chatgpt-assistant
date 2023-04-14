package org.ivance.gptassistant.forge;

import dev.architectury.platform.forge.EventBuses;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ivance.gptassistant.config.AssistantConfig;
import org.ivance.gptassistant.core.AssistantManager;
import org.ivance.gptassistant.core.AssistantService;
import org.ivance.gptassistant.forge.config.ModConfig;

@Mod(ChatGptAssistantForge.MOD_ID)
public class ChatGptAssistantForge {

    public static final String MOD_ID = "gptassistant";

    public static final Logger LOGGER = LogManager.getLogger("ChatGPTAssistant");

    private static final AssistantManager manager = new AssistantManager(LOGGER);

    public static void setConfig(AssistantConfig config) {
        manager.setConfig(config);
    }

    public ChatGptAssistantForge() {
        EventBuses.registerModEventBus(MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> {
                return AutoConfig.getConfigScreen(ModConfig.class, screen).get();
            })
        );
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        String messageText = event.getMessage().getString();
        Player sender = event.getPlayer();
        if (messageText.startsWith(":")) {
            LOGGER.info("Received prompt message: " + messageText);
            AssistantService service = manager.getService();
            if (service != null) {
                new Thread(() -> {
                    synchronized (sender) {
                        service.executeCommandByPrompt(sender, messageText);
                    }
                }).start();
            } else {
                String warningMessage = "ChatGPT assistant service is not available, please set the API key first.";
                sender.displayClientMessage(Component.literal(warningMessage), false);
                LOGGER.warn(warningMessage);
            }
        }
    }
}
