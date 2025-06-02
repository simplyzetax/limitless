package me.simplyzetax.limitless.client;

import me.simplyzetax.limitless.client.shared.config.ClientConfig;
import me.simplyzetax.limitless.client.features.core.FeatureManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.sound.SoundEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LimitlessClient implements ClientModInitializer {
    public static final String MOD_ID = "limitless";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static FeatureSet enabledFeatures = FeatureFlags.FEATURE_MANAGER.getFeatureSet();

    private static KeyBinding rshiftBinding;
    private static KeyBinding gBinding;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Limitless Client Initialized (client-side only)!");

        // Initialize all features using FeatureManager
        FeatureManager.initializeAllFeatures();

        // Register the right shift keybinding (now shows info message about Mod Menu)
        rshiftBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.limitless.rshift", // Translation key
                InputUtil.Type.KEYSYM, // Input type
                InputUtil.GLFW_KEY_RIGHT_SHIFT, // Right shift key
                "category.limitless.general" // Category translation key
        ));

        gBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.limitless.g", // Translation key
                InputUtil.Type.KEYSYM, // Input type
                InputUtil.GLFW_KEY_G, // G key
                "category.limitless.general" // Category translation key
        ));

        // Register the tick event to check for key presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (rshiftBinding.wasPressed()) {
                // This code runs when right shift is pressed
                LOGGER.info("Right shift pressed!");

                // Show message about Mod Menu instead of opening custom GUI
                if (client.player != null) {
                    client.player.sendMessage(net.minecraft.text.Text.literal(
                            "§6Limitless settings are now available through Mod Menu! §7(Install Mod Menu mod to access)"),
                            false);
                }
            }
            while (gBinding.wasPressed()) {
                // This code runs when g is pressed
                LOGGER.info("G pressed!");

                // Get player
                PlayerEntity player = MinecraftClient.getInstance().player;

                if (player != null) {
                    // play sound to player to indicate that it worked
                    player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM.value(), 1.0F, 1.0F);
                }

                ClientConfig.PlayersShouldGlow = !ClientConfig.PlayersShouldGlow;
            }
        });
    }
}