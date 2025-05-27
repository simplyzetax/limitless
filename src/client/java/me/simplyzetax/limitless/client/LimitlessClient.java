package me.simplyzetax.limitless.client;

import me.simplyzetax.limitless.Limitless;
import me.simplyzetax.limitless.client.config.ClientConfig;
import me.simplyzetax.limitless.client.screens.SettingsGUI;
import me.simplyzetax.limitless.network.RefreshCreativePayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.SoundEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
    private static KeyBinding gBinding;

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

        gBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.limitless.g", // Translation key
                InputUtil.Type.KEYSYM, // Input type
                InputUtil.GLFW_KEY_G, // G key
                "category.limitless.general" // Category translation key
        ));

        registerNetworkHandlers();

        // Register the tick event to check for right shift presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (rshiftBinding.wasPressed()) {
                // This code runs when right shift is pressed
                LOGGER.info("Right shift pressed!");

                // Add your right shift handling logic here
                if (client.player != null) {

                    // Example: Do something with the player
                    MinecraftClient.getInstance().setScreen(new SettingsGUI());

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

    private void registerNetworkHandlers() {
        Limitless.LOGGER.info("DEBUG: Registering client network handlers");

        ClientPlayNetworking.registerGlobalReceiver(
                RefreshCreativePayload.ID,
                (payload, context) -> {
                    Limitless.LOGGER.info("DEBUG: Client received refresh payload. Action: {}, Extra: {}",
                            payload.action(), payload.extra());

                    context.client().execute(() -> {
                        Limitless.LOGGER.info("DEBUG: Executing client refresh task");

                        switch (payload.action()) {
                            case "refresh_creative" -> {
                                Limitless.LOGGER.info("DEBUG: Refreshing all creative inventory");
                                CreativeScreenManager.refreshCreativeInventoryIfOpen();
                            }
                            case "refresh_group" -> {
                                Limitless.LOGGER.info("DEBUG: Refreshing group: {}", payload.extra());
                                ItemGroup group = ItemGroupUtil.getItemGroupByName(payload.extra());
                                if (group != null) {
                                    Limitless.LOGGER.info("DEBUG: Found group, refreshing: {}", group);
                                    CreativeScreenManager.refreshCreativeInventoryForGroup(group);
                                } else {
                                    Limitless.LOGGER.warn("DEBUG: Group not found for name: {}", payload.extra());
                                }
                            }
                            default -> Limitless.LOGGER.warn("DEBUG: Unknown action: {}", payload.action());
                        }
                    });
                });

        Limitless.LOGGER.info("DEBUG: Client network handlers registered");
    }

}