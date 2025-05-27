package me.simplyzetax.limitless;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.simplyzetax.limitless.stealer.LimitlessItemGroupManager;

public class Limitless implements ModInitializer {
    public static final String MOD_ID = "limitless";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Limitless mod (client-side only)...");

        LimitlessItemGroupManager.initialize();

        LOGGER.info("Limitless mod initialized successfully!");
    }
}
