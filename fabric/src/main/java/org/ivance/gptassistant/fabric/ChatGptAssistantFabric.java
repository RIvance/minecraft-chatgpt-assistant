package org.ivance.gptassistant.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ivance.gptassistant.config.AssistantConfig;
import org.ivance.gptassistant.core.AssistantManager;
import org.ivance.gptassistant.core.AssistantService;
import org.ivance.gptassistant.fabric.config.ModConfig;

public class ChatGptAssistantFabric implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("ChatGPTAssistant");

    private static final AssistantManager manager = new AssistantManager(LOGGER);

    public static void setConfig(AssistantConfig config) {
        manager.setConfig(config);
    }

    @Override
    public void onInitialize() {
        ModConfig.init();
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, parameters) -> {
            String messageText = message.serverContent().getString();
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
        });
    }
}
