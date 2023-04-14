package org.ivance.gptassistant.fabric.config;

import blue.endless.jankson.Comment;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.world.InteractionResult;
import org.ivance.gptassistant.config.AssistantConfig;
import org.ivance.gptassistant.config.RequestConfig;
import org.ivance.gptassistant.fabric.ChatGptAssistantFabric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
@Config(name = "assets/gptassistant")
public class ModConfig implements ConfigData, AssistantConfig {

    @ConfigEntry.Gui.Excluded
    public static ModConfig INSTANCE;

    public static void init() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        AutoConfig.getConfigHolder(ModConfig.class).registerLoadListener((manager, data) -> {
            ChatGptAssistantFabric.setConfig(data);
            return InteractionResult.SUCCESS;
        });
        AutoConfig.getConfigHolder(ModConfig.class).registerSaveListener((manager, data) -> {
            ChatGptAssistantFabric.setConfig(data);
            return InteractionResult.SUCCESS;
        });
        ChatGptAssistantFabric.setConfig(INSTANCE);
    }

    @Comment("OpenAI Api key, can be obtained from https://platform.openai.com/account/api-keys")
    private String token = "";

    @Override @NotNull
    public String getToken() {
        return token;
    }

    @Comment("[Optional] Proxy address, e.g. http://127.0.0.1:8889")
    private String proxy = "";

    @Override @Nullable
    public String getProxy() {
        return proxy.isBlank() ? null : proxy;
    }

    @Comment("Request timeout in seconds, 0 for no timeout")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 300)
    private long timeoutSecond = 0;

    @Override
    public long getTimeoutSecond() {
        return timeoutSecond;
    }

    public enum Model {
        DAVINCI_001("text-davinci-001"),
        DAVINCI_002("text-davinci-002"),
        DAVINCI_003("text-davinci-003"),
        CURIE_001("text-curie-001"),
        ADA_001("text-ada-001"),
        GPT_35_TURBO("gpt-3.5-turbo"),
        GPT_35_TURBO_0301("gpt-3.5-turbo-0301");

        private final String ident;

        Model(String ident) {
            this.ident = ident;
        }

        @Override
        public String toString() {
            return ident;
        }
    }

    @Comment("Model to use, see https://platform.openai.com/docs/api-reference/models for more details")
    private Model model = Model.DAVINCI_003;

    @Override @NotNull
    public String getModel() {
        return model.toString();
    }

    @ConfigEntry.BoundedDiscrete(min = 0, max = 1024)
    private int maxTokens = 128;

    private double temperature = 0.9;

    private double topP = 1.0;

    private double frequencyPenalty = 0.0;

    private double presencePenalty = 0.0;

    @Override @NotNull
    public RequestConfig getRequestConfig() {
        return new RequestConfig(maxTokens, temperature, topP, frequencyPenalty, presencePenalty);
    }
}
