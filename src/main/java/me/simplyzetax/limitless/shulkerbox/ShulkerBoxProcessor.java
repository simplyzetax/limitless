package me.simplyzetax.limitless.shulkerbox;

import me.simplyzetax.limitless.Limitless;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class ShulkerBoxProcessor {

    /**
     * Processes shulker boxes for display in the creative tab.
     */
    public void processShulkerBoxesForDisplay(Set<ShulkerBoxData> shulkerBoxes, ItemGroup.Entries entries) {
        Set<String> addedKeys = new HashSet<>();

        for (ShulkerBoxData shulkerBoxData : shulkerBoxes) {
            try {
                addUniqueShulkerBox(shulkerBoxData, addedKeys, entries);
            } catch (Exception e) {
                Limitless.LOGGER.error("Error processing ShulkerBoxData: {}", shulkerBoxData, e);
            }
        }
    }

    /**
     * Adds a unique shulker box to the creative tab entries.
     */
    private void addUniqueShulkerBox(ShulkerBoxData shulkerBoxData, Set<String> addedKeys, ItemGroup.Entries entries) {
        String uniqueKey = shulkerBoxData.getUniqueKey();

        if (addedKeys.add(uniqueKey)) {
            ItemStack displayStack = createEnhancedDisplayStack(shulkerBoxData);
            entries.add(displayStack);
        }
    }

    /**
     * Creates an enhanced display stack with additional tooltip information.
     */
    private ItemStack createEnhancedDisplayStack(ShulkerBoxData shulkerBoxData) {
        ItemStack displayStack = shulkerBoxData.createDisplayStack();

        // Add enhanced lore with placement information
        ShulkerBoxDisplayFormatter formatter = new ShulkerBoxDisplayFormatter();
        return formatter.addPlacementLore(displayStack, shulkerBoxData);
    }
}
