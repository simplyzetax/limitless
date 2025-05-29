package me.simplyzetax.limitless.client.features.itemstealing.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import me.simplyzetax.limitless.Limitless;

/**
 * Manages storage and retrieval of original item data.
 * Maps display items to their original counterparts.
 */
public class OriginalItemStorage {

    // Map from display item hash to original ItemStack
    private static final Map<String, ItemStack> originalItems = new ConcurrentHashMap<>();

    // Custom NBT key to identify items that came from the stealer
    private static final String LIMITLESS_MARKER_KEY = "limitless_display_item";

    /**
     * Stores the original item and marks the display item.
     * 
     * @param originalItem The original item with all its NBT data
     * @param displayItem  The display item shown in the creative tab
     */
    public static void storeOriginalItem(ItemStack originalItem, ItemStack displayItem) {
        try {
            String itemKey = generateItemKey(displayItem);

            // Store the original item
            originalItems.put(itemKey, originalItem.copy());

            // Mark the display item with our custom NBT
            markAsDisplayItem(displayItem, itemKey);

            Limitless.LOGGER.debug("Stored original item for key: {}", itemKey);
        } catch (Exception e) {
            Limitless.LOGGER.error("Error storing original item", e);
        }
    }

    /**
     * Retrieves the original item for a given display item and adds the "Stolen
     * with Limitless" lore.
     * 
     * @param displayItem The display item from the creative tab
     * @return The original item with "Stolen with Limitless" lore, or the display
     *         item if no original found
     */
    public static ItemStack getCleanedOriginalItem(ItemStack displayItem) {
        try {
            String itemKey = getItemKey(displayItem);
            if (itemKey == null) {
                Limitless.LOGGER.debug("No item key found for display item");
                return displayItem;
            }

            ItemStack originalItem = originalItems.get(itemKey);
            if (originalItem == null) {
                Limitless.LOGGER.debug("No original item found for key: {}", itemKey);
                return displayItem;
            }

            // Create a copy of the original item
            ItemStack cleanedItem = originalItem.copy();
            cleanedItem.setCount(displayItem.getCount());

            // Add the "Stolen with Limitless" lore
            addStolenLore(cleanedItem);

            Limitless.LOGGER.info("Retrieved and cleaned original item for: {}", cleanedItem.getName().getString());
            return cleanedItem;

        } catch (Exception e) {
            Limitless.LOGGER.error("Error retrieving original item", e);
            return displayItem;
        }
    }

    /**
     * Checks if an item is a display item from the stealer.
     */
    public static boolean isDisplayItem(ItemStack stack) {
        return getItemKey(stack) != null;
    }

    /**
     * Clears all stored original items.
     */
    public static void clearAll() {
        originalItems.clear();
        Limitless.LOGGER.info("Cleared all stored original items");
    }

    /**
     * Gets the number of stored original items.
     */
    public static int getStoredItemCount() {
        return originalItems.size();
    }

    /**
     * Generates a unique key for an item based on its properties.
     */
    private static String generateItemKey(ItemStack stack) {
        // Create a key based on item type and basic properties
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(stack.getItem().toString());
        keyBuilder.append("_");
        keyBuilder.append(System.nanoTime()); // Add timestamp for uniqueness
        keyBuilder.append("_");
        keyBuilder.append(stack.hashCode());

        return keyBuilder.toString();
    }

    /**
     * Marks a display item with our custom NBT data.
     */
    private static void markAsDisplayItem(ItemStack displayItem, String itemKey) {
        try {
            // We'll store the key in a custom component or NBT
            // For now, we'll use a simple approach by storing it in the item's custom data
            // This is a simplified approach - in a real implementation you might want to
            // use custom components
            NbtCompound customData = new NbtCompound();
            customData.putString(LIMITLESS_MARKER_KEY, itemKey);

            // Store as hidden data that doesn't affect display
            // We'll use the item's existing NBT structure
            if (displayItem.contains(DataComponentTypes.CUSTOM_DATA)) {
                NbtCompound existingData = displayItem.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
                existingData.put("limitless_data", customData);
                displayItem.set(DataComponentTypes.CUSTOM_DATA,
                        net.minecraft.component.type.NbtComponent.of(existingData));
            } else {
                NbtCompound rootData = new NbtCompound();
                rootData.put("limitless_data", customData);
                displayItem.set(DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.of(rootData));
            }
        } catch (Exception e) {
            Limitless.LOGGER.error("Error marking display item", e);
        }
    }

    /**
     * Retrieves the item key from a display item.
     */
    private static String getItemKey(ItemStack stack) {
        try {
            if (!stack.contains(DataComponentTypes.CUSTOM_DATA)) {
                return null;
            }

            NbtCompound customData = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
            if (!customData.contains("limitless_data")) {
                return null;
            }

            // Handle Optional<NbtCompound> return type
            Optional<NbtCompound> limitlessDataOpt = customData.getCompound("limitless_data");
            if (limitlessDataOpt.isEmpty()) {
                return null;
            }

            NbtCompound limitlessData = limitlessDataOpt.get();
            if (!limitlessData.contains(LIMITLESS_MARKER_KEY)) {
                return null;
            }

            // getString returns Optional<String> in newer versions
            return limitlessData.getString(LIMITLESS_MARKER_KEY).orElse(null);
        } catch (Exception e) {
            Limitless.LOGGER.error("Error retrieving item key", e);
            return null;
        }
    }

    /**
     * Adds the "Stolen with Limitless" lore to an item while preserving existing
     * lore.
     */
    private static void addStolenLore(ItemStack stack) {
        try {
            List<Text> newLore = new ArrayList<>();

            // Keep any existing lore
            if (stack.contains(DataComponentTypes.LORE)) {
                LoreComponent currentLore = stack.get(DataComponentTypes.LORE);
                newLore.addAll(currentLore.lines());
            }

            // Add a blank line if we have existing lore
            if (!newLore.isEmpty()) {
                newLore.add(Text.literal(""));
            }

            // Add our "Stolen with Limitless" identifier
            newLore.add(Text.literal("Stolen with Limitless")
                    .setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));

            // Set the new lore
            stack.set(DataComponentTypes.LORE, new LoreComponent(newLore));
        } catch (Exception e) {
            Limitless.LOGGER.error("Error adding stolen lore", e);
        }
    }
}
