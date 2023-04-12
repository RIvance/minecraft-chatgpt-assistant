package org.ivance.gptassistant;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ivance.gptassistant.core.AssistantManager;
import org.ivance.gptassistant.core.AssistantService;

public class ChatGptAssistantMod implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("ChatGPTAssistant");

    private static final AssistantManager manager = new AssistantManager(LOGGER);

    @Override
    public void onInitialize() {
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, parameters) -> {
            String messageText = message.getContent().getString();
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
                    LOGGER.warn("Assistant service is not available, please set the API-Key first");
                }
            }
        });
    }
}
