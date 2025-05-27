package me.simplyzetax.limitless;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.simplyzetax.limitless.network.RefreshCreativePayload;
import me.simplyzetax.limitless.stealer.LimitlessItemGroupManager;

public class Limitless implements ModInitializer {
    public static final String MOD_ID = "limitless";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static MinecraftServer currentServer;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Limitless mod...");

        LimitlessItemGroupManager.initialize();

        PayloadTypeRegistry.playS2C().register(
                RefreshCreativePayload.ID,
                RefreshCreativePayload.CODEC);

        // Register server lifecycle events
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            currentServer = server;
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            currentServer = null;
        });

        LOGGER.info("Limitless mod initialized successfully!");
    }

    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }

}
