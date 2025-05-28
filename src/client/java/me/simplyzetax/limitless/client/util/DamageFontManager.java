package me.simplyzetax.limitless.client.util;

import me.simplyzetax.limitless.client.config.ClientConfig;
import net.minecraft.util.Identifier;

public class DamageFontManager {

    /**
     * Gets the current font identifier based on the config setting
     */
    public static Identifier getCurrentFont() {
        return switch (ClientConfig.DamageNumberFont.toLowerCase()) {
            case "uniform" -> Identifier.of("limitless", "uniform_damage");
            case "alt" -> Identifier.of("limitless", "alt_damage");
            case "compact" -> Identifier.of("limitless", "compact_damage");
            case "bold" -> Identifier.of("limitless", "bold_damage");
            case "custom" -> Identifier.of("limitless", "damage_numbers"); // For custom fonts
            default -> null; // Use default Minecraft font
        };
    }

    /**
     * Gets available font options for UI selection
     */
    public static String[] getAvailableFonts() {
        return new String[] { "default", "uniform", "alt", "compact", "bold", "custom" };
    }

    /**
     * Cycles to the next font option
     */
    public static void cycleFont() {
        String[] fonts = getAvailableFonts();
        int currentIndex = 0;

        // Find current font index
        for (int i = 0; i < fonts.length; i++) {
            if (fonts[i].equals(ClientConfig.DamageNumberFont)) {
                currentIndex = i;
                break;
            }
        }

        // Move to next font (wrap around)
        currentIndex = (currentIndex + 1) % fonts.length;
        ClientConfig.DamageNumberFont = fonts[currentIndex];
    }
}
