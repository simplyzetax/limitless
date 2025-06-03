package me.simplyzetax.limitless.client;

import io.github.retrooper.packetevents.factory.fabric.FabricPacketEventsAPI;
import me.simplyzetax.limitless.client.features.core.FeatureManager;
import me.simplyzetax.limitless.client.listeners.TestPacketListener; // Assuming this is the correct path
import me.simplyzetax.limitless.client.shared.config.ClientConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text; // Import for Text
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LimitlessClient implements ClientModInitializer {
    public static final String MOD_ID = "limitless";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // This is fine if you use it, but ensure it's managed correctly if features
    // change
    public static FeatureSet enabledFeatures = FeatureFlags.FEATURE_MANAGER.getFeatureSet();

    private static KeyBinding rshiftBinding;
    private static KeyBinding gBinding;

    @Override
    public void onInitializeClient() {

        LOGGER.info("Limitless Client Initializing...");
        initializePacketEvents();
        FeatureManager.initializeAllFeatures();

        // Register the right shift keybinding
        rshiftBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.limitless.rshift",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_RIGHT_SHIFT,
                "category.limitless.general"));

        gBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.limitless.g",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_G,
                "category.limitless.general"));

        // Register the tick event to check for key presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (rshiftBinding.wasPressed()) {
                LOGGER.info("Right shift pressed!");
                if (client.player != null) {
                    client.player.sendMessage(Text.literal( // Use Text.literal
                            "§6Limitless settings are now available through Mod Menu! §7(Install Mod Menu mod to access)"),
                            false);
                }
            }
            while (gBinding.wasPressed()) {
                LOGGER.info("G pressed!");
                PlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null) {
                    player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM.value(), 1.0F, 1.0F);
                }
                ClientConfig.PlayersShouldGlow = !ClientConfig.PlayersShouldGlow;
            }
        });

        LOGGER.info("Limitless Client Initialization Complete!");
    }

    private void initializePacketEvents() {
        LOGGER.info("Attempting to initialize PacketEvents for mod ID: {}", MOD_ID);
        try {
            FabricPacketEventsAPI api = FabricPacketEventsAPI.getClientAPI();

            if (api == null) {
                LOGGER.error("PacketEvents.setAPI() resulted in a  null API instance.");
                return;
            }

            LOGGER.info("PacketEvents API instance obtained. Configuring settings...");
            api.getSettings()
                    .checkForUpdates(false);

            LOGGER.info("Loading PacketEvents API...");
            api.load();

            LOGGER.info("Initializing PacketEvents API...");
            api.init();

            if (api.isLoaded() && api.isInitialized()) {
                LOGGER.info("PacketEvents API loaded and initialized successfully!");
                LOGGER.info("Registering PacketEvents listeners...");
                api.getEventManager().registerListener(new TestPacketListener()); // Ensure path is correct
                LOGGER.info("TestPacketListener registered successfully!");
            } else {
                LOGGER.error("Failed to fully load or initialize PacketEvents API. Loaded: {}, Initialized: {}",
                        api.isLoaded(), api.isInitialized());
            }
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred during PacketEvents initialization:", e);
        }
    }
}