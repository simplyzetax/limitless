package me.simplyzetax.limitless.client.features.damagenumbers.util;

import me.simplyzetax.limitless.client.shared.config.ClientConfig;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collection;

public class DamageFontManager {

    public enum FontOption {
        DEFAULT("default", "Default"),
        UNIFORM("uniform", "Uniform"),
        ALT("alt", "Alternative"),
        COMPACT("compact", "Compact"),
        BOLD("bold", "Bold"),
        CUSTOM("custom", "Custom");

        private final String key;
        private final String displayName;

        FontOption(String key, String displayName) {
            this.key = key;
            this.displayName = displayName;
        }

        public String getKey() {
            return key;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

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
     * Gets all font options for Cloth Config dropdown
     */
    public static Collection<FontOption> getAllFontOptions() {
        return Arrays.asList(FontOption.values());
    }

    /**
     * Gets the FontOption for a given key
     */
    public static FontOption getFontOption(String key) {
        for (FontOption option : FontOption.values()) {
            if (option.getKey().equals(key)) {
                return option;
            }
        }
        return FontOption.DEFAULT;
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
