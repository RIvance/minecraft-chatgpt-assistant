package org.ivance.gptassistant;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ivance.gptassistant.config.AssistantConfig;
import org.ivance.gptassistant.config.ModConfig;
import org.ivance.gptassistant.core.AssistantManager;
import org.ivance.gptassistant.core.AssistantService;

public class ChatGptAssistantMod implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("ChatGPTAssistant");

    private static final AssistantManager manager = new AssistantManager(LOGGER);

    public static void setConfig(AssistantConfig config) {
        manager.setConfig(config);
    }

    public static void handleChatMessage(String messageText, PlayerEntity sender) {
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
                sender.sendMessage(Text.of(warningMessage), false);
                LOGGER.warn(warningMessage);
            }
        }
    }

    @Override
    public void onInitialize() {
        ModConfig.init();
    }
}
