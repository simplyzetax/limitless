package me.simplyzetax.limitless.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class LimitlessClient implements ClientModInitializer {

    public static final String MOD_ID = "limitless";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Limitless Client Initialized!");
    }
}