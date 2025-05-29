package me.simplyzetax.limitless.client.features.itemstealing.util;

import me.simplyzetax.limitless.Limitless;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for cleaning metadata from ItemStacks when they are transferred
 * to player inventory.
 */
public class ItemStackMetadataCleaner {

    /**
     * Cleans metadata from an item stack, removing Limitless-specific lore and
     * display name formatting.
     * Keeps the original item properties but adds a minimal "Limitless" text at the
     * bottom of lore.
     * 
     * @param stack The ItemStack to clean
     * @return The cleaned ItemStack (original item with "Stolen with Limitless"
     *         lore)
     */
    public static ItemStack cleanItemMetadata(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return stack;
        }

        try {
            // Check if this is a display item from the stealer
            if (OriginalItemStorage.isDisplayItem(stack)) {
                ItemStack cleanedItem = OriginalItemStorage.getCleanedOriginalItem(stack);
                Limitless.LOGGER.info("Cleaned metadata for item: {}", cleanedItem.getName().getString());
                return cleanedItem;
            } else {
                // This is not a display item, just add the lore to the existing item
                addLimitlessLore(stack);
                Limitless.LOGGER.info("Added Limitless lore to non-display item: {}", stack.getName().getString());
                return stack;
            }
        } catch (Exception e) {
            Limitless.LOGGER.error("Error cleaning item metadata", e);
            return stack;
        }
    }

    /**
     * Adds a minimal "Limitless" text at the bottom of the lore while preserving
     * any
     * original lore that wasn't added by Limitless.
     */
    private static void addLimitlessLore(ItemStack stack) {
        try {
            List<Text> newLore = new ArrayList<>();

            // Keep any existing lore that doesn't contain Limitless-specific text
            if (stack.contains(DataComponentTypes.LORE)) {
                LoreComponent currentLore = stack.get(DataComponentTypes.LORE);
                for (Text line : currentLore.lines()) {
                    String lineText = line.getString();
                    // Skip lines that are likely part of the Limitless display
                    if (!lineText.contains("Obtained from:") &&
                            !lineText.contains("Obtained time:") &&
                            !lineText.contains("reset on a game restart")) {
                        newLore.add(line);
                    }
                }
            }

            // Set the new lore
            stack.set(DataComponentTypes.LORE, new LoreComponent(newLore));
        } catch (Exception e) {
            Limitless.LOGGER.error("Error adding Limitless lore", e);
        }
    }

    /**
     * Cleans metadata from all items in a player's inventory.
     * 
     * @param playerInventory The player's inventory to clean
     */
    public static void cleanPlayerInventoryItems(PlayerInventory playerInventory) {
        if (playerInventory == null) {
            return;
        }

        try {
            // Clean all slots in the inventory (main inventory + equipment slots)
            for (int i = 0; i < playerInventory.size(); i++) {
                ItemStack stack = playerInventory.getStack(i);
                if (!stack.isEmpty()) {
                    ItemStack cleanedStack = cleanItemMetadata(stack.copy());
                    playerInventory.setStack(i, cleanedStack);
                }
            }

            Limitless.LOGGER.debug("Cleaned metadata for all items in player inventory");
        } catch (Exception e) {
            Limitless.LOGGER.error("Error cleaning player inventory items", e);
        }
    }
}
