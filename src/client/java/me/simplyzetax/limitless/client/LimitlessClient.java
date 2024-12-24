package me.simplyzetax.limitless.client;

import me.simplyzetax.limitless.client.screens.TestGUI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LimitlessClient implements ClientModInitializer {
    public static final String MOD_ID = "limitless";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyBinding rshiftBinding;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Limitless Client Initialized!");

        // Register the right shift keybinding
        rshiftBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.limitless.rshift", // Translation key
                InputUtil.Type.KEYSYM, // Input type
                InputUtil.GLFW_KEY_RIGHT_SHIFT, // Right shift key
                "category.limitless.general" // Category translation key
        ));

        // Register the tick event to check for right shift presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (rshiftBinding.wasPressed()) {
                // This code runs when right shift is pressed
                LOGGER.info("Right shift pressed!");

                // Add your right shift handling logic here
                if (client.player != null) {

                    // Example: Do something with the player
                    MinecraftClient.getInstance().setScreen(new TestGUI());

                }
            }
        });
    }
}