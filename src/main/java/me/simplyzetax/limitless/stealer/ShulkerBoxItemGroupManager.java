package me.simplyzetax.limitless.stealer;

import me.simplyzetax.limitless.Limitless;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.LinkedHashSet;
import java.util.Set;

public class ShulkerBoxItemGroupManager {
    public static final Set<ItemStack> SHULKER_BOX_ITEMS = new LinkedHashSet<>();
    public static ItemGroup SHULKER_BOX_ITEM_GROUP;

    public static void initialize() {
        createItemGroup();
        registerItemGroup();
        Limitless.LOGGER.info("Shulker Box item group registered successfully!");
    }

    /**
     * Adds a shulker box to the collection.
     * Creative inventory refresh is handled client-side by the mixin.
     */
    public static void addShulkerBox(ItemStack shulkerBox) {
        boolean wasAdded = SHULKER_BOX_ITEMS.add(shulkerBox.copy());

        if (wasAdded) {
            Limitless.LOGGER.info("Added shulker box: {}", shulkerBox.getName().getString());
        }
    }

    private static void createItemGroup() {
        SHULKER_BOX_ITEM_GROUP = FabricItemGroup.builder()
                .icon(() -> new ItemStack(Items.BLUE_SHULKER_BOX))
                .displayName(Text.literal("§9Shulker Boxes"))
                .entries((context, entries) -> {
                    if (SHULKER_BOX_ITEMS.isEmpty()) {
                        ItemStack emptyIndicator = new ItemStack(Items.BARRIER);
                        emptyIndicator.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME,
                                Text.literal("§cNo shulker boxes captured yet"));
                        entries.add(emptyIndicator);
                        return;
                    }

                    ItemStackProcessor processor = new ItemStackProcessor();
                    processor.processItemsForDisplay(SHULKER_BOX_ITEMS, entries, context.lookup());
                })
                .build();
    }

    private static void registerItemGroup() {
        Registry.register(
                Registries.ITEM_GROUP,
                Identifier.of(Limitless.MOD_ID, "shulker_boxes"),
                SHULKER_BOX_ITEM_GROUP);
    }

    /**
     * Clears all shulker boxes.
     */
    public static void clearShulkerBoxes() {
        SHULKER_BOX_ITEMS.clear();
        Limitless.LOGGER.info("Cleared all shulker boxes");
    }

    /**
     * Removes a specific shulker box.
     */
    public static boolean removeShulkerBox(ItemStack shulkerBox) {
        boolean wasRemoved = SHULKER_BOX_ITEMS.remove(shulkerBox);

        if (wasRemoved) {
            Limitless.LOGGER.info("Removed shulker box: {}", shulkerBox.getName().getString());
        }

        return wasRemoved;
    }

    /**
     * Gets the count of shulker boxes.
     */
    public static int getShulkerBoxCount() {
        return SHULKER_BOX_ITEMS.size();
    }

    /**
     * Checks if a specific shulker box is already in the collection.
     */
    public static boolean containsShulkerBox(ItemStack shulkerBox) {
        return SHULKER_BOX_ITEMS.contains(shulkerBox);
    }
}
